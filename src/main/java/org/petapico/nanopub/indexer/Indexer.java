package org.petapico.nanopub.indexer;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.nanopub.MultiNanopubRdfHandler;
import org.nanopub.Nanopub;
import org.nanopub.NanopubUtils;
import org.nanopub.MultiNanopubRdfHandler.NanopubHandler;
import org.nanopub.extra.server.GetNanopub;
import org.nanopub.extra.server.NanopubServerUtils;
import org.nanopub.extra.server.ServerInfo;
import org.nanopub.extra.server.ServerIterator;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;

public class Indexer {
	public static final int SECTION_HEAD = 1;
	public static final int SECTION_ASSERTION = 2;
	public static final int SECTION_PROVENANCE = 3;
	public static final int SECTION_PUBINFO = 4;

	NanopubDatabase db = null;
	
	public static List<Nanopub> nanopubs; //used by the callback function of the MultiNanopubRdfHandler class -> can we do this better?
	
	public static void main(String[] args) {
		/*
		args = new String[3];
		args[0] = "root";
		args[1] = "admin";
		args[2] = "true";
		*/
		
		if (args.length < 2){
			System.out.printf("Invalid arguments expected: dbusername, dbpassword\n");
			System.exit(1);
		}
		
		while (true){
			try {
				Indexer indexer = new Indexer(args[0], args[1]);
				indexer.run();
			}
			catch (Exception E){
				System.out.println("Run error");
				System.out.println(E.toString());
			}
			// sleep ?
		}
	}

	public Indexer(String dbusername, String dbpassword) throws ClassNotFoundException, SQLException {
		db = new NanopubDatabase(dbusername, dbpassword);
	}

	public void run() throws IOException, RDFHandlerException, Exception {
		ServerIterator serverIterator = new ServerIterator();

		while (serverIterator.hasNext()) {
			ServerInfo si = serverIterator.next();
			String serverName = si.getPublicUrl();
			System.out.println("==========");
			System.out.println("Server: " + serverName + "\n");
			
			//retrieve server information (from server)
			int peerPageSize = si.getPageSize(); 		//nanopubs per page
			long peerNanopubNo = si.getNextNanopubNo(); //number of np's stored on the server
			long peerJid = si.getJournalId(); 			//journal identifier of the server
			
			System.out.printf("Info:\n %d peerpagesize\n %d peernanopubno\n %d peerjid\n\n", peerPageSize, peerNanopubNo, peerJid);

			//retrieve stored server information (from database)
			long dbNanopubNo = db.getNextNanopubNo(serverName); //number of np's stored according to db
			long dbJid = db.getJournalId(serverName); 			//journal identifier according to db
			
			System.out.printf("Db:\n %d dbnanopubno\n %d dbjid\n", dbNanopubNo, dbJid);
			System.out.println("==========\n");
			
			//Start from the beginning
			if (dbJid != peerJid) {
				dbNanopubNo = 0; 
				db.updateJournalId(serverName, peerJid);
			}
			
			long currentNanopub = dbNanopubNo;
			System.out.printf("Starting from: %d\n", currentNanopub);
			
			long start = System.currentTimeMillis();
			try {
				while (currentNanopub < peerNanopubNo){
					int page = (int) (currentNanopub / peerPageSize) + 1; 	// compute the starting page
					int addedNanopubs = insertNanopubsFromPage(page, serverName); // add all nanopubs from page
					currentNanopub += addedNanopubs;
					if (addedNanopubs < peerPageSize && currentNanopub < peerNanopubNo) {
						System.out.println("ERROR  not enough nanopubs found on page: " + page); 
						break;
					}
					System.out.println("page: " + page);
				}
			}
			catch (Exception E){
				System.out.println( "ERROR" );
				System.out.println(E.getMessage());
			}
			finally {
				System.out.println( "Updating database ") ;
				//db.updateNextNanopubNo(serverName, currentNanopub);
			}
			long end = System.currentTimeMillis();
			System.out.printf("performance estimate: %d hours\n", ((end-start) * 637)/(1000 * 60 * 24));
		}
	}
	
	public int insertNanopubsFromPage(int page, String server) throws Exception{
		int coveredNanopubs = 0;
		getNanopubPackage(page, server);	// fills the nanopub list
		if (nanopubs.size() == 0) {
			System.out.println("ERROR no nanopubs found on page " + page);
			return -1;
		}

		for (Nanopub np : nanopubs) {
			String artifactCode = np.getUri().toString();
			int insertStatus = db.npInserted(artifactCode);
			if (insertStatus == -1){		// not inserted
				insertNpInDatabase(np, artifactCode, false);	
			}
			else if (insertStatus == 0){	// started but not finished
				//System.out.printf("ignore insert: %s\n", artifactCode);
				insertNpInDatabase(np, artifactCode, true);
			}
			coveredNanopubs ++;
		}
		return coveredNanopubs;
	}
	
	public int insertNpInDatabase(Nanopub np, String artifactCode, boolean ignore) throws IOException, SQLException{
		int totalURIs = 0;
		
		if (!ignore){
			db.insertNp(artifactCode);
		}
		//totalURIs += insertStatementsInDB(np.getHead(), artifactCode, stmt, SECTION_HEAD);
		totalURIs += insertStatementsInDB(np.getAssertion(), artifactCode, SECTION_ASSERTION, ignore);
		totalURIs += insertStatementsInDB(np.getProvenance(), artifactCode, SECTION_PROVENANCE, ignore);
		totalURIs += insertStatementsInDB(np.getPubinfo(), artifactCode, SECTION_PUBINFO, ignore);
		db.updateNpFinished(artifactCode);
		
		return totalURIs;
	}
	
	public int insertStatementsInDB(Set<Statement> statements, String artifactCode, int sectionID, boolean ignore) throws IOException, SQLException{
		Set<String> URIlist = new HashSet<String>();
		for (Statement statement : statements){
			Value object = statement.getObject();
			URI predicate = statement.getPredicate();
			Resource subject = statement.getSubject();

			if (object instanceof URI){
				String objectStr = object.stringValue();
				URIlist.add(objectStr);
			}
			
			if (subject instanceof URI){
				String subjectStr = subject.stringValue();
				URIlist.add(subjectStr);
			}
			
			String predicateStr = predicate.toString();
			URIlist.add(predicateStr);

		}
		PreparedStatement stmt;
		if (ignore){
			stmt = db.getInsertIgnoreStmt();
		}
		else {
			stmt = db.getInsertStmt();
		}
		
		stmt.setString(2, artifactCode);
		stmt.setInt(3, sectionID);

		//insert URIlist in database
		for (String uri : URIlist){
			stmt.setString(1, uri);
			stmt.executeUpdate();
		}
		
		return URIlist.size();
	}
	
	public void getNanopubPackage(int page, String server) throws Exception{
		nanopubs = new ArrayList<Nanopub>();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5 * 1000).build();
		HttpClient c = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		String nanopubPackage = server + "package.gz?page=" + page;
		HttpGet get = new HttpGet(nanopubPackage);
		get.setHeader("Accept", "application/x-gzip");
		HttpResponse resp = c.execute(get);
		InputStream in = null;
		try {
			if (wasSuccessful(resp)) {
				in = new GZIPInputStream(resp.getEntity().getContent());
			} else {
				System.out.println("Failed. Trying uncompressed package...");
				// This is for compability with older versions; to be removed at some point...
				get = new HttpGet(server + "package.gz?page=" + page);
				get.setHeader("Accept", "application/trig");
				resp = c.execute(get);
				if (!wasSuccessful(resp)) {
					System.out.println("HTTP request failed: " + resp.getStatusLine().getReasonPhrase());
					throw new RuntimeException(resp.getStatusLine().getReasonPhrase());
				}
				in = resp.getEntity().getContent();
			}
			MultiNanopubRdfHandler.process(RDFFormat.TRIG, in, new NanopubHandler() {
				@Override
				public void handleNanopub(Nanopub np) {
					try {
						Indexer.nanopubs.add(np);
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				}
			});
		} finally {
			if (in != null){
				in.close();
			}
		}
	}
		
	private boolean wasSuccessful(HttpResponse resp) {
		int c = resp.getStatusLine().getStatusCode();
		return c >= 200 && c < 300;
	}

}
