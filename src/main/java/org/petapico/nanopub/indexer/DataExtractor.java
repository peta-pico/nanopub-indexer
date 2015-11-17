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
				
				//Get essential values of nanopub:
				/*
				* Singular values: nanopubURI, artifactCode, creationTime
				* Insert into database
				* Sets: Creators, Authors
				* Insert into corresponding tables
				 */
				
				
				Set<Statement> head = np.getHead();
				for (Statement headitem : head){
					URI predicate = headitem.getPredicate();
					Value object = headitem.getObject();
					Resource subject = headitem.getSubject();
					int hashcode = headitem.hashCode();
					
					String predicateStr = predicate.toString();
					
				}
				
				
				//insertNpInDatabase(np);
			}

			coveredNanopubs += nanopubsOnPage.size();
			page += 1;
		}
		System.out.println("Pages done: "+ page);
		System.out.println("Nanopubs done: "+ coveredNanopubs);
	}
	
	
	
	public void insertNpInDatabase(Nanopub np) throws IOException{
		String nanopubURI = HelperFunctions.getNanopubURI(np);
		String artifactCode = HelperFunctions.getArtifactCode(nanopubURI);
		String assertionURI = HelperFunctions.getAssertionURI(np);
		String headURI = HelperFunctions.getHeadURI(np);
		String provenanceURI = HelperFunctions.getProvenanceURI(np);
		String pubinfoURI = HelperFunctions.getPubinfoURI(np);
		long creationTime = HelperFunctions.getCreationTime(np);
		long timestamp = creationTime / 1000;
		
		String getUrl = "http://localhost/nanopubs/database/api.php"
				+ "?table=nanopubs"
				+ "&function=insertNanopub"
				+ "&data[]="+nanopubURI
				+ "&data[]="+artifactCode
				+ "&data[]="+assertionURI
				+ "&data[]="+headURI
				+ "&data[]="+provenanceURI
				+ "&data[]="+pubinfoURI
				+ "&data[]="+timestamp;
		
		URL url = new URL(getUrl);
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		if (conn.getResponseCode() != 200) {
			throw new IOException(conn.getResponseMessage());
		}
		
		conn.disconnect();
		
		System.out.println(getUrl);
	}
	
	
	

	public static void main(String[] args) throws IOException, RDFHandlerException {
		DataExtractor dataExtractor = new DataExtractor();
		dataExtractor.run();
	}


}
