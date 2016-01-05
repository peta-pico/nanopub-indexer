package org.petapico.nanopub.indexer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
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
import java.sql.SQLException;

public class DataExtractor {
	public static final int CUR_AMOUNT_OF_NANOPUBS = 6370131;
	public static final int MILLISEC_TO_DAYS = 1000 * 60 * 60 * 24; 
	
	public static final int TYPE_HEAD = 1;
	public static final int TYPE_ASSERTION = 2;
	public static final int TYPE_PROVENANCE = 3;
	public static final int TYPE_PUBINFO = 4;
	
	PrintStream out;

	public DataExtractor() {
		out = new PrintStream(System.out);
	}

	public void run() throws IOException, RDFHandlerException {
		long startTime = System.currentTimeMillis();
		long currentTime;
		String server = "http://np.inn.ac/";
		System.out.println("==========");
		System.out.println("Server: " + server + "\n");
		
		//initialize values
		int page = 1;
		int coveredNanopubs = 0;
		
		//Loop until we encounter an empty page
		while (true){
			//Q: iS THERE A WAY TO GRAB ALL NP's as list instead of per page?
			List<String> nanopubsOnPage;
			try {
				nanopubsOnPage = ServerFunctions.grabNanopubIdsFromPage(server, page);
			}
			catch (Exception E){
				nanopubsOnPage = NanopubServerUtils.loadNanopubUriList(server, page);	
			}
			
			
			//Check if there are nanopubs on this page
			if (nanopubsOnPage.size() == 0){
				break; 
			}
			
			try {
				getNanopubPackage(page);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Go through every nanopub from page
			for (String nanopubId : nanopubsOnPage) {
				
				//retrieve nanopub
				long tb = System.currentTimeMillis();
				out.println(nanopubId);
				Nanopub np = GetNanopub.get(nanopubId);
				
				long te = System.currentTimeMillis();
				out.println("\t Get nanopub: " + ((te-tb) * CUR_AMOUNT_OF_NANOPUBS) / MILLISEC_TO_DAYS + " days");
				
				//INSERT NP INTO DATABASE
				tb = System.currentTimeMillis();
				insertNpInDatabase(np);
				te = System.currentTimeMillis();
				out.println("\t Insert nanopub: " + ((te-tb) * CUR_AMOUNT_OF_NANOPUBS) / MILLISEC_TO_DAYS + " days");
				
				coveredNanopubs ++;
				currentTime = System.currentTimeMillis();
				System.out.println("("+coveredNanopubs+") Estimation time: "+((currentTime-startTime)/coveredNanopubs) * CUR_AMOUNT_OF_NANOPUBS / MILLISEC_TO_DAYS + " days");
			}

			page += 1;
		}
		System.out.println("Pages done: "+ page);
		System.out.println("Nanopubs done: "+ coveredNanopubs);
	}
	
	
	//insert a set of statements into a database
	//statements contain a object, predicate, subject and hashcode
	public void insertStatementsInDB(Set<Statement> statements, String artifactCode, int type) throws IOException{
		//go through every statement
		List<String> URIlist = new ArrayList<String>();
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
		
	}	
	
	public void insertNpInDatabase(Nanopub np) throws IOException{
		String artifactCode = HelperFunctions.getArtifactCode(np);
		
		double te,tb;
		
		try {
			//out.println("head");
			tb = System.currentTimeMillis();
			insertStatementsInDB(np.getHead(), artifactCode, TYPE_HEAD);
			te = System.currentTimeMillis();
			out.println("\t Head statements: " + ((te-tb) * CUR_AMOUNT_OF_NANOPUBS) / MILLISEC_TO_DAYS + " days");
		}
		catch (Exception E){
			E.printStackTrace();
		}
		
		//insert assertion into database
		try {
			//out.println("assertion");
			tb = System.currentTimeMillis();
			insertStatementsInDB(np.getAssertion(), artifactCode, TYPE_ASSERTION);
			te = System.currentTimeMillis();
			out.println("\t Assertion statements: " + ((te-tb) * CUR_AMOUNT_OF_NANOPUBS) / MILLISEC_TO_DAYS + " days");
		}
		catch (Exception E){
			
		}
		//insert provenance into database
		try {
			//out.println("provenance");
			tb = System.currentTimeMillis();
			insertStatementsInDB(np.getProvenance(), artifactCode, TYPE_PROVENANCE);
			te = System.currentTimeMillis();
			out.println("\t Provenance statements: " + ((te-tb) * CUR_AMOUNT_OF_NANOPUBS) / MILLISEC_TO_DAYS + " days");
		}
		catch (Exception E){
			
		}
		//insert pubinfo into database
		try {
			//out.println("pubinfo");
			tb = System.currentTimeMillis();
			insertStatementsInDB(np.getPubinfo(), artifactCode, TYPE_PUBINFO);
			te = System.currentTimeMillis();
			out.println("\t Pubinfo statements: " + ((te-tb) * CUR_AMOUNT_OF_NANOPUBS) / MILLISEC_TO_DAYS + " days");
		}
		catch (Exception E){
			
		}
	}
	
	public void getNanopubPackage(int page) throws Exception{
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5 * 1000).build();
		HttpClient c = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		HttpGet get = new HttpGet("http://np.inn.ac/package.gz?page=" + page);
		get.setHeader("Accept", "application/x-gzip");
		HttpResponse resp = c.execute(get);
		InputStream in = null;
		try {
			if (wasSuccessful(resp)) {
				in = new GZIPInputStream(resp.getEntity().getContent());
			} else {
				System.out.println("Failed. Trying uncompressed package...");
				// This is for compability with older versions; to be removed at some point...
				get = new HttpGet("http://np.inn.ac/package.gz?page=" + page);
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
						//loadNanopub(np);
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

	public static void main(String[] args) throws IOException, RDFHandlerException {
		DataExtractor dataExtractor = new DataExtractor();
		dataExtractor.run();
	}


}
