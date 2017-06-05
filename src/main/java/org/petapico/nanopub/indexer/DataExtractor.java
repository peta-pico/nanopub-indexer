package org.petapico.nanopub.indexer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLEncoder;
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
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;

public class DataExtractor {
	public static final int CUR_AMOUNT_OF_NANOPUBS = 6370131;
	public static final int MILLISEC_TO_MINUTES = 1000 * 60;
	
	public static final int SECTION_HEAD = 1;
	public static final int SECTION_ASSERTION = 2;
	public static final int SECTION_PROVENANCE = 3;
	public static final int SECTION_PUBINFO = 4;
	
	String server = "http://np.inn.ac/";
	public static List<Nanopub> nanopubs;
	//temporary variable
	
	PrintStream out;

	public DataExtractor() {
		out = new PrintStream(System.out);
	}

	public void run(String dbuser, String dbpass) throws IOException, RDFHandlerException, SQLException {
		Connection conn = dbconnect(dbuser, dbpass);
		PreparedStatement stmt = conn.prepareStatement("INSERT INTO uris VALUES(?,?,?)");

		long startTime = System.currentTimeMillis();
		long currentTime;
		
		System.out.println("==========");
		System.out.println("Server: " + server + "\n");
		
		//initialize values
		int page = 1;
		int coveredNanopubs = 0;
		long coveredURIs = 0;
		
		//Loop until we encounter an empty page
		while (true){			
			try {
				getNanopubPackage(page);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (nanopubs.size() == 0)
				break;
			
			//Go through every nanopub from page
			for (Nanopub np : nanopubs) {
				
				//INSERT NP INTO DATABASE
				coveredURIs += insertNpInDatabase(np, stmt);	
		
				coveredNanopubs ++;
			}
			
			currentTime = System.currentTimeMillis();
			out.printf("(%d/%d)[%d/%d]\n", coveredNanopubs, CUR_AMOUNT_OF_NANOPUBS, coveredURIs, coveredURIs/coveredNanopubs*CUR_AMOUNT_OF_NANOPUBS);
			long estMinutes = (currentTime-startTime)/coveredNanopubs * CUR_AMOUNT_OF_NANOPUBS / MILLISEC_TO_MINUTES;
			long estMinutesLeft = (currentTime-startTime)/coveredNanopubs * (CUR_AMOUNT_OF_NANOPUBS-coveredNanopubs) / MILLISEC_TO_MINUTES;
			out.printf("Estimation time: %d-%d minutes (%d hours) \n", estMinutes, estMinutesLeft, estMinutes/60);
			
			page += 1;
		}
		System.out.println("Pages done: "+ page);
		System.out.println("Nanopubs done: "+ coveredNanopubs);
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int insertNpInDatabase(Nanopub np, PreparedStatement stmt) throws IOException{
		String artifactCode = np.getUri().toString(); //HelperFunctions.getArtifactCode(np);
		int totalURIs = 0;
		
		try {
			//totalURIs += insertStatementsInDB(np.getHead(), artifactCode, stmt, SECTION_HEAD);
		}
		catch (Exception E){
			E.printStackTrace();
		}
		
		//insert assertion into database
		try {
			totalURIs += insertStatementsInDB(np.getAssertion(), artifactCode, stmt, SECTION_ASSERTION);
		}
		catch (Exception E){
			
		}
		//insert provenance into database
		try {
			totalURIs += insertStatementsInDB(np.getProvenance(), artifactCode, stmt, SECTION_PROVENANCE);
		}
		catch (Exception E){
			
		}
		//insert pubinfo into database
		try {
			totalURIs += insertStatementsInDB(np.getPubinfo(), artifactCode, stmt, SECTION_PUBINFO);
		}
		catch (Exception E){
			
		}
		return totalURIs;
	}

	//insert a set of statements into a database
	//statements contain a object, predicate, subject and hashcode
	public int insertStatementsInDB(Set<Statement> statements, String artifactCode, PreparedStatement stmt, int section) throws IOException, SQLException{
		//go through every statement
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
		stmt.setString(2, artifactCode);
		stmt.setInt(3, section);

		//insert URIlist in database
		for (String uri : URIlist){
			stmt.setString(1, uri);
			stmt.executeUpdate();
		}
		
		return URIlist.size();
	}
	
	
	public Connection dbconnect(String dbuser, String dbpass){
		Connection conn = null;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost/nanopubs",dbuser, dbpass);
			System.out.print("Database is connected !");
		}
		catch(Exception e)
		{
			System.out.print("Do not connect to DB - Error:"+e);
		}
		return conn;
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
		if (args.length != 2){
			System.out.printf("Invalid arguments expected: dbusername, dbpassword\n");
			System.exit(1);
		}
		DataExtractor dataExtractor = new DataExtractor();
		dataExtractor.run(args[0], args[1]);
	}


}