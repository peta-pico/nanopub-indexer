package org.petapico.nanopub.indexer;

import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

import org.nanopub.Nanopub;
import org.nanopub.NanopubUtils;
import org.nanopub.extra.server.GetNanopub;
import org.nanopub.extra.server.NanopubServerUtils;
import org.nanopub.extra.server.ServerInfo;
import org.nanopub.extra.server.ServerIterator;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

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
			
			//Go through every nanopub from page
			for (String nanopubId : nanopubsOnPage) {
				
				//retrieve nanopub
				long tb = System.currentTimeMillis();
				Nanopub np = GetNanopub.get(nanopubId);
				long te = System.currentTimeMillis();
				out.println("\t Get nanopub: " + ((te-tb) * CUR_AMOUNT_OF_NANOPUBS) / MILLISEC_TO_DAYS + " days");
				
				//INSERT NP INTO DATABASE
				tb = System.currentTimeMillis();
				insertNpInDatabase(np);
				te = System.currentTimeMillis();
				out.println("\t Insert nanopub: " + ((te-tb) * CUR_AMOUNT_OF_NANOPUBS) / MILLISEC_TO_DAYS + " days");
				
				String artifactCode = HelperFunctions.getArtifactCode(np);
				//insert head into database
				try {
					//out.println("head");
					tb = System.currentTimeMillis();
					insertStatementsInDB(np.getHead(), artifactCode, TYPE_HEAD);
					te = System.currentTimeMillis();
					out.println("\t Head statements: " + ((te-tb) * CUR_AMOUNT_OF_NANOPUBS) / MILLISEC_TO_DAYS + " days");
				}
				catch (Exception E){
					
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
		for (Statement statement : statements){
			insertStatementInDB(statement, artifactCode, type);
		}
		
	}	
	
	//retrieve all values of a statement: predicate, subject, object, and hashcode and insert them into the database
	public void insertStatementInDB(Statement statement, String artifactCode, int type) throws IOException{
		Value object = statement.getObject();
		URI predicate = statement.getPredicate();
		Resource subject = statement.getSubject();
		int hashCode = statement.hashCode(); //retrieve the hashcode -> hashvalue of the whole statement (unique)

		String objectStr = object.stringValue();
		String predicateStr = predicate.toString();
		String subjectStr = subject.stringValue();
		
		String getUrl = "http://localhost/nanopubs/database/api.php"
				+ "?table=statements"
				+ "&function=insertStatement"
				+ "&data[]="+artifactCode
				+ "&data[]="+hashCode
				+ "&data[]="+URLEncoder.encode(objectStr, "UTF-8")
				+ "&data[]="+URLEncoder.encode(predicateStr, "UTF-8")
				+ "&data[]="+URLEncoder.encode(subjectStr, "UTF-8")
				+ "&data[]="+type;
				
		DatabaseFunctions.executeGetRequest(getUrl);
		//System.out.println("statmentURL: " + getUrl);
	}
	
	public void insertNpInDatabase(Nanopub np) throws IOException{
		String artifactCode = HelperFunctions.getArtifactCode(np);
		long creationTime = HelperFunctions.getTimeStamp(np);
		long timestamp = creationTime / 1000;
		String getUrl = "http://localhost/nanopubs/database/api.php"
				+ "?table=nanopubs"
				+ "&function=insertNanopub"
				+ "&data[]="+artifactCode
				+ "&data[]="+timestamp;
		
		//System.out.println("artifact: " + artifactCode + "\ntime: " + timestamp + "\nurl: " + getUrl);
		
		DatabaseFunctions.executeGetRequest(getUrl);
	}
	
	
	

	public static void main(String[] args) throws IOException, RDFHandlerException {
		DataExtractor dataExtractor = new DataExtractor();
		dataExtractor.run();
	}


}
