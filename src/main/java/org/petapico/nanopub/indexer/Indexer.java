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
	
	public static void main(String[] args) throws IOException, RDFHandlerException, Exception {
		Indexer indexer = new Indexer();
		indexer.run();
	}

	public Indexer() throws ClassNotFoundException, SQLException {
		db = new NanopubDatabase("root", "admin");
	}

	public void run() throws IOException, RDFHandlerException, Exception {
		ServerIterator serverIterator = new ServerIterator();
		while (serverIterator.hasNext()) {
			ServerInfo si = serverIterator.next();
			String serverName = si.getPublicUrl();
			System.out.println("==========");
			System.out.println("Server: " + serverName + "\n");
			
			//retrieve server information
			int peerPageSize = si.getPageSize(); //nanopubs per page
			long peerNanopubNo = si.getNextNanopubNo(); //number of np's stored on the server
			long peerJid = si.getJournalId(); //journal identifier of the server
			
			System.out.printf("Info:\n %d peerpagesize\n %d peernanopubno\n %d peerjid\n\n", peerPageSize, peerNanopubNo, peerJid);

			//retrieve stored server information
			long dbNanopubNo = db.getNextNanopubNo(serverName); //number of np's stored according to db
			long dbJid = db.getJournalId(serverName); //journal identifier according to db
			
			System.out.printf("Db:\n %d dbnanopubno\n %d dbjid\n", dbNanopubNo, dbJid);
			System.out.println("==========\n");
			
			if (dbJid != peerJid){
				dbNanopubNo = 0;
			}
			
			long currentNanopub = dbNanopubNo;
			try {
				while (currentNanopub < peerNanopubNo){
					int page = (int) (currentNanopub / peerPageSize) + 1;
					int addedNanopubs= insertNanopubsFromPage(page, serverName);
					if (addedNanopubs == -1) break; //something must have gone wrong
					currentNanopub += addedNanopubs;
				}
			}
			finally {
				db.updateJournalId(serverName, peerJid);
				db.updateNextNanopubNo(serverName, currentNanopub);
			}
		}
	}
	
	public int insertNanopubsFromPage(int page, String server) throws Exception{
		int coveredNanopubs = 0;
		getNanopubPackage(page, server); //fills the nanopub list
		if (nanopubs.size() == 0) {return -1;} //this is no good..

		for (Nanopub np : nanopubs) {
			String artifactCode = np.getUri().toString();
			if (!db.npInserted(artifactCode)){
				//INSERT NP INTO DATABASE
				insertNpInDatabase(np, artifactCode);	
			}
			else {
				System.out.printf("skip: %s\n", artifactCode);
			}
			coveredNanopubs ++;
		}
		return coveredNanopubs;
	}
	
	public int insertNpInDatabase(Nanopub np, String artifactCode) throws IOException, SQLException{
		db.insertNp(artifactCode);
		int totalURIs = 0;
		
		//totalURIs += insertStatementsInDB(np.getHead(), artifactCode, stmt, SECTION_HEAD);
		totalURIs += insertStatementsInDB(np.getAssertion(), artifactCode, SECTION_ASSERTION);
		totalURIs += insertStatementsInDB(np.getProvenance(), artifactCode, SECTION_PROVENANCE);
		totalURIs += insertStatementsInDB(np.getPubinfo(), artifactCode, SECTION_PUBINFO);
		
		return totalURIs;
	}
	
	public int insertStatementsInDB(Set<Statement> statements, String artifactCode, int sectionID) throws IOException, SQLException{
		Set<String> URIlist = new HashSet<String>();
		for (Statement statement : statements){
			Value object = statement.getObject();
			URI predicate = statement.getPredicate();
			Resource subject = statement.getSubject();

			String objectStr = object.stringValue();
			String predicateStr = predicate.toString();
			String subjectStr = subject.stringValue();
			
			URIlist.add(objectStr);
			URIlist.add(predicateStr);
			URIlist.add(subjectStr);
		}
		db.setArtifactCodeInsertPs(artifactCode);
		db.setSectionInsertPs(sectionID);

		//insert URIlist in database
		for (String uri : URIlist){
			if (uri.length() < 760){
				db.setUriInsertPs(uri);
				db.runInsertPs();
			}
			else {
				System.out.printf("URI to big: %s\n", uri);
			}
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
				System.out.println(in.toString());
				in.close();
			}
		}
	}
	
	private boolean wasSuccessful(HttpResponse resp) {
		int c = resp.getStatusLine().getStatusCode();
		return c >= 200 && c < 300;
	}

}
