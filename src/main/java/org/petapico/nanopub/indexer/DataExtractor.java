package org.petapico.nanopub.indexer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
import org.nanopub.MultiNanopubRdfHandler.NanopubHandler;
import org.nanopub.Nanopub;
import org.nanopub.NanopubUtils;
import org.nanopub.extra.server.GetNanopub;
import org.nanopub.extra.server.NanopubServerUtils;
import org.nanopub.extra.server.ServerInfo;
import org.nanopub.extra.server.ServerIterator;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataExtractor {
	public static final int TOTAL_AMOUNT_OF_NANOPUBS = 6370131;
	public static final int MILLISEC_TO_DAYS = 1000 * 60 * 60 * 24; 
	public static final int MILLISEC_TO_HOURS = 1000 * 60 * 60; 
	
	String server = "http://np.inn.ac/";
	public static List<Nanopub> nanopubs;
	//temporary variable
	
	PrintStream out;

	public DataExtractor() {
		out = new PrintStream(System.out);
	}

	public void run() throws IOException, RDFHandlerException, SQLException {
		Connection conn = dbconnect();
		if (conn == null){
			System.exit(1);
		}
		
		// prepare the insert query for uri's
		PreparedStatement insertURI = conn.prepareStatement("INSERT IGNORE INTO uris VALUES(?,?)");

		long startTime = System.currentTimeMillis();
		long currentTime;
		
		System.out.println("==========");
		System.out.println("Server: " + server + "\n");
		
		//initialize values
		int page = 1;
		int coveredNanopubs = 0;
		int coveredURIs = 0;
		
		while (true){
			try {
				getNanopubPackage(page);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
			// If there are no nanopubs on this page
			if (nanopubs.size() == 0){
				break;
			}
			
			//Go through every nanopub from page
			for (Nanopub np : nanopubs) {
				
				//INSERT NP INTO DATABASE
				coveredURIs += insertNpInDatabase(np, insertURI);				
				coveredNanopubs ++;
				
				currentTime = System.currentTimeMillis();
				System.out.printf("(%d/%d)[%d/%d] done\n", coveredNanopubs, TOTAL_AMOUNT_OF_NANOPUBS, coveredURIs, coveredURIs/coveredNanopubs*TOTAL_AMOUNT_OF_NANOPUBS);
				System.out.printf("Total time: %d hours\n", ((currentTime-startTime)/coveredNanopubs) * TOTAL_AMOUNT_OF_NANOPUBS / MILLISEC_TO_HOURS);
				System.out.printf("Time remaining: %d hours\n\n", ((currentTime-startTime)/coveredNanopubs) * (TOTAL_AMOUNT_OF_NANOPUBS-coveredNanopubs) / MILLISEC_TO_HOURS);
			}

			page += 1;
		}
		
		System.out.println("Pages done: "+ page);
		System.out.println("Nanopubs done: "+ coveredNanopubs);
		
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Connection dbconnect(){
		Connection conn = null;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost/nanopubs","root", "admin");
			System.out.printf("Database connection success\n");
				
		}
		catch(Exception e)
		{
			System.out.print("Do not connect to DB - Error:"+e);
		}
		return conn;
	}
	
	public int insertNpInDatabase(Nanopub np, PreparedStatement stmt) throws IOException, SQLException{
		String artifactCode = HelperFunctions.getArtifactCode(np);
		Set<Statement> URIlist = new HashSet<Statement>();
		
		//get head URI's
		try {
			URIlist.addAll(np.getHead());
		}
		catch (Exception E){
			E.printStackTrace();
		}
		
		//get assertion URI's
		try {
			URIlist.addAll(np.getAssertion());
		}
		catch (Exception E){
			E.printStackTrace();
		}

		//get provenance URI's
		try {
			URIlist.addAll(np.getProvenance());
		}
		catch (Exception E){
			E.printStackTrace();
		}

		//get pubinfo URI's
		try {
			URIlist.addAll(np.getPubinfo());
		}
		catch (Exception E){
			E.printStackTrace();
		}
		
		stmt.setString(2, artifactCode);
		insertStatementsInDB(URIlist, artifactCode, stmt);
		return URIlist.size();
	}
	
	//insert a set of statements into a database
	//statements contain a object, predicate, subject and hashcode
	public void insertStatementsInDB(Set<Statement> statements, String artifactCode, PreparedStatement stmt) throws IOException, SQLException{
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

		//insert URIlist in database
		for (String uri : URIlist){
			stmt.setString(1, uri);
			stmt.executeUpdate();
		}
	}	
	
	
	
	public void getNanopubPackage(int page) throws Exception{
		nanopubs = new ArrayList<Nanopub>();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5 * 1000).build();
		HttpClient c = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpGet get = new HttpGet(server + "package.gz?page=" + page);
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
						String artifactCode = HelperFunctions.getArtifactCode(np);
						DataExtractor.nanopubs.add(np);
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

	public static void main(String[] args) throws IOException, RDFHandlerException, SQLException {
		DataExtractor dataExtractor = new DataExtractor();
		dataExtractor.run();
	}


}
