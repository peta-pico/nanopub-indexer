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
	public static final int TYPE_HEAD = 1;
	public static final int TYPE_ASSERTION = 2;
	public static final int TYPE_PROVENANCE = 3;
	public static final int TYPE_PUBINFO = 4;
	
	PrintStream out;

	public DataExtractor() {
		out = new PrintStream(System.out);
	}

	public void run() throws IOException, RDFHandlerException {
		//Loop through every server
		ServerIterator serverIterator = new ServerIterator();
		if (!serverIterator.hasNext()){
			System.out.println("ERROR: NO SERVERS FOUND");
			System.exit(0);
		}
		
		//Use first server only for testing purposes
		ServerInfo si = serverIterator.next();
		System.out.println("==========");
		System.out.println("Server: " + si.getPublicUrl() + "\n");
		
		//initialize values
		int page = 1;
		int coveredNanopubs = 0;
		int totalNanopubs = (int) (si.getNextNanopubNo()-1);
		
		//Loop through every page
		while (coveredNanopubs < totalNanopubs){
			//Q: Read all nanopubs from page -> Can this more efficiently? 
			//out.println("SERVERURI: " + si.getPublicUrl() + "nanopubs?page=" + page); 
			//Q: iS THERE A WAY TO GRAB ALL NP's as list instead of per page?
			long startTime = System.currentTimeMillis();
			List<String> nanopubsOnPage = NanopubServerUtils.loadNanopubUriList(si, page);
			long finishTime = System.currentTimeMillis();
			System.out.println("That took: "+(finishTime-startTime)+ " ms");
			try {
				startTime = System.currentTimeMillis();
				nanopubsOnPage = ServerFunctions.grabNanopubIdsFromPage("http://np.inn.ac/", page);
				finishTime = System.currentTimeMillis();
				System.out.println("That took: "+(finishTime-startTime)+ " ms");
			}
			catch (Exception E){
				
			}
			
			
			//Check if there are nanopubs on this page
			if (nanopubsOnPage.size() == 0){
				break; 
			}
			
			//Go through every nanopub from page
			for (String nanopubId : nanopubsOnPage) {
				out.println("id: " + nanopubId);
				//retrieve nanopub
				Nanopub np = GetNanopub.get(nanopubId);
				
				//INSERT NP INTO DATABASE
				insertNpInDatabase(np);
				
				String artifactCode = HelperFunctions.getArtifactCode(np);
				//insert head into database
				try {
					out.println("head");
					insertStatementsInDB(np.getHead(), artifactCode, TYPE_HEAD);
				}
				catch (Exception E){
					
				}
				//insert assertion into database
				try {
					out.println("assertion");
					insertStatementsInDB(np.getAssertion(), artifactCode, TYPE_ASSERTION);
				}
				catch (Exception E){
					
				}
				//insert provenance into database
				try {
					out.println("provenance");
					insertStatementsInDB(np.getProvenance(), artifactCode, TYPE_PROVENANCE);
				}
				catch (Exception E){
					
				}
				//insert pubinfo into database
				try {
					out.println("pubinfo");
					insertStatementsInDB(np.getPubinfo(), artifactCode, TYPE_PUBINFO);
				}
				catch (Exception E){
					
				}
				
				System.out.println("\n");
				coveredNanopubs ++;
				
				out.printf("nanopub: %d/%d\n", coveredNanopubs, totalNanopubs);
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
		System.out.println("artifact: " + artifactCode + "\ntime: " + timestamp + "\nurl: " + getUrl);
		
		DatabaseFunctions.executeGetRequest(getUrl);
	}
	
	
	

	public static void main(String[] args) throws IOException, RDFHandlerException {
		DataExtractor dataExtractor = new DataExtractor();
		dataExtractor.run();
	}


}
