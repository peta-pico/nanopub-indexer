package org.petapico.nanopub.indexer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

	public DataExtractor() {
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
			//Read all nanopubs from page -> Can this more efficiently? 
			List<String> nanopubsOnPage = NanopubServerUtils.loadNanopubUriList(si, page);
			
			//Check if there are nanopubs on this page
			if (nanopubsOnPage.size() == 0){
				break; 
			}
			
			//Go through every nanopub from page
			for (String nanopubId : nanopubsOnPage) {
				//retrieve nanopub
				Nanopub np = GetNanopub.get(nanopubId);
				
				/*Set<Statement> statements = np.getHead();
				for (Statement statement : statements){
					insertStatementInDB(statement);
				}
				*/
				
				//INSERT NP INTO DATABASE
				insertNpInDatabase(np);
				try {
					insertStatementsInDB(np.getHead(), HelperFunctions.getArtifactCode(np), TYPE_HEAD);
				}
				catch (Exception E){
					
				}
				System.out.println("\n");
			}

			coveredNanopubs += nanopubsOnPage.size();
			page += 1;
		}
		System.out.println("Pages done: "+ page);
		System.out.println("Nanopubs done: "+ coveredNanopubs);
	}
	
	public void insertStatementsInDB(Set<Statement> statements, String artifactCode, int type) throws IOException{
		for (Statement statement : statements){
			int hashCode = statement.hashCode();
			
			insertHashInDB(hashCode, artifactCode, type);
		}
		
	}
	
	public void insertHashInDB(int hashCode, String artifactCode, int type) throws IOException{
		String getUrl = "http://localhost/nanopubs/database/api.php"
				+ "?table=hashes"
				+ "&function=insert"
				+ "&data[]="+hashCode
				+ "&data[]="+artifactCode
				+ "&data[]="+type;
				
		DatabaseFunctions.executeGetRequest(getUrl);
		System.out.println("URL: " + getUrl);
	}
	
	public void insertStatementInDB(Statement statement){
		URI predicate = statement.getPredicate();
		Value object = statement.getObject();
		Resource subject = statement.getSubject();
		int hashcode = statement.hashCode();
		String predicateStr = predicate.toString();
		
	}
	
	public void insertNpInDatabase(Nanopub np) throws IOException{
		String artifactCode = HelperFunctions.getArtifactCode(np);
		long creationTime = HelperFunctions.getTimeStamp(np);
		try {
			np.getCreationTime().getTimeInMillis();
		}
		catch (Exception E){
			
		}
		long timestamp = creationTime / 1000;
		String getUrl = "http://localhost/nanopubs/database/api.php"
				+ "?table=nanopubs"
				+ "&function=insertNanopub"
				+ "&data[]="+artifactCode
				+ "&data[]="+timestamp;
		DatabaseFunctions.executeGetRequest(getUrl);
		System.out.println("artifact: " + artifactCode + "\ntime: " + timestamp + "\nurl: " + getUrl);
	}
	
	
	

	public static void main(String[] args) throws IOException, RDFHandlerException {
		DataExtractor dataExtractor = new DataExtractor();
		dataExtractor.run();
	}


}
