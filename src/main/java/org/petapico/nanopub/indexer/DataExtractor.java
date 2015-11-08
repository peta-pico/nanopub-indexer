package org.petapico.nanopub.indexer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.nanopub.Nanopub;
import org.nanopub.NanopubUtils;
import org.nanopub.extra.server.GetNanopub;
import org.nanopub.extra.server.NanopubServerUtils;
import org.nanopub.extra.server.ServerInfo;
import org.nanopub.extra.server.ServerIterator;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.model.URI;

public class DataExtractor {

	public static void main(String[] args) throws IOException, RDFHandlerException {
		DataExtractor dataExtractor = new DataExtractor();
		dataExtractor.run();
	}

	public DataExtractor() {
	}

	public void run() throws IOException, RDFHandlerException {
		//Server iterator goes through every server
		ServerIterator serverIterator = new ServerIterator();
		if (!serverIterator.hasNext()){
			System.out.println("ERROR: NO SERVERS FOUND");
			System.exit(0);
		}
		
		//for testing purposes we only use the first server
		ServerInfo si = serverIterator.next();
		System.out.println("==========");
		System.out.println("Server: " + si.getPublicUrl() + "\n");
		
		int page = 0;
		int currentNanopub = 0;
		int totalNanopubs = 5; //(int) (si.getNextNanopubNo()-1);
		while (currentNanopub < totalNanopubs){ //There should be a page left
			page += 1; //Next page
			List<String> nanopubsOnPage = NanopubServerUtils.loadNanopubUriList(si, page); //Load np's from page
			
			if (nanopubsOnPage.size() == 0){
				break; //There are no nanopubs on this page
			}
			
			for (String nanopubId : nanopubsOnPage) { //Go through np list of page
				currentNanopub++;
				
				System.out.println(nanopubId);
				nanopubId = "http://www.tkuhn.ch/bel2nanopub/RAZ-uo0MZIWA8XjZ1LtVPl0E8locJ9KT5EM1Ml8cMJoKE";
				Nanopub np = GetNanopub.get(nanopubId);
				//printInsertStatement(np);
				insertNpInDatabase(np);
			}
			
		}
		System.out.println("Pages done: "+ page);
		System.out.println("Nanopubs done: "+ currentNanopub);
	}
	
	public String getNanopubURI(Nanopub np){
		String nanopubURI;
		try {
			nanopubURI = np.getUri().toString();
		}
		catch (Exception e){
			nanopubURI = "unknown";
		}
		return nanopubURI;
	}
	
	public String getArtifactCode(String nanopubURI){
		String artifactCode;
		try {
			artifactCode = org.nanopub.extra.server.GetNanopub.getArtifactCode(nanopubURI);
		}
		catch (Exception e){
			artifactCode = "unknown";
		}
		return artifactCode;
	}
	
	public String getAssertionURI(Nanopub np){
		String assertionURI;
		try {
			assertionURI = np.getAssertionUri().toString();
		}
		catch (Exception e){
			assertionURI = "unknown";
		}
		return assertionURI;
	}
	
	public String getHeadURI(Nanopub np){
		String headURI;
		try {
			headURI = np.getHeadUri().toString();
		}
		catch (Exception e){
			headURI = "unknown";
		}
		return headURI;
	}
	
	public String getProvenanceURI(Nanopub np){
		String provenanceURI;
		try {
			provenanceURI = np.getProvenanceUri().toString();
		}
		catch (Exception e){
			provenanceURI = "unknown";
		}
		return provenanceURI;
	}
	
	public String getPubinfoURI(Nanopub np){
		String pubinfoURI;
		try {
			pubinfoURI = np.getPubinfoUri().toString();
		}
		catch (Exception e){
			pubinfoURI = "unknown";
		}
		return pubinfoURI;
	}
	
	public long getCreationTime(Nanopub np){
		long creationTime;
		try {
			creationTime = np.getCreationTime().getTimeInMillis();
		}
		catch (Exception e){
			creationTime = 0;
		}
		return creationTime;
	}
	
	public void insertNpInDatabase(Nanopub np) throws IOException{
		String nanopubURI = getNanopubURI(np);
		String artifactCode = getArtifactCode(nanopubURI);
		String assertionURI = getAssertionURI(np);
		String headURI = getHeadURI(np);
		String provenanceURI = getProvenanceURI(np);
		String pubinfoURI = getPubinfoURI(np);
		long creationTime = getCreationTime(np);
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
	
	public void printInsertStatement(Nanopub np){
		String nanopubURI = np.getUri().toString();
		String artifactCode = org.nanopub.extra.server.GetNanopub.getArtifactCode(nanopubURI);
		String assertionURI = np.getAssertionUri().toString();
		String headURI = np.getHeadUri().toString();
		String provenanceURI = np.getProvenanceUri().toString();
		String pubinfoURI = np.getPubinfoUri().toString();
		long creationTime = np.getCreationTime().getTimeInMillis();
		long timestamp = creationTime / 1000;
		
		String query = "INSERT INTO nanopubs "
				+ "(nanopubURI, artifactCode, assertionURI, headURI, provenanceURI, pubinfoURI, creationTime) "
				+ "VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", %d);\n";
		System.out.printf(query, nanopubURI, artifactCode, assertionURI, headURI, provenanceURI, pubinfoURI, timestamp);
	}
	
	public void printNanopubInfo(Nanopub np){
		System.out.println("Authors:        " + np.getAuthors().toString());
		System.out.println("Creators:       " + np.getCreators().toString());
		System.out.println("Assertion:      " + np.getAssertion().toString());
		System.out.println("AssertionURI:   " + np.getAssertionUri().toString());
		System.out.println("GraphURI:       " + np.getGraphUris().toString());
		System.out.println("Head:           " + np.getHead().toString());
		System.out.println("HeadURI:        " + np.getHeadUri().toString());
		System.out.println("Provenance:     " + np.getProvenance().toString());
		System.out.println("ProvenanceURI:  " + np.getProvenanceUri().toString());
		System.out.println("Pubinfo:        " + np.getPubinfo().toString());
		System.out.println("PubinfoURI:     " + np.getPubinfoUri().toString());
		System.out.println("URI:            " + np.getUri().toString());
		System.out.println("Creationtime:   " + np.getCreationTime().getTimeInMillis());
		//System.out.println("Nanopub content:" + NanopubUtils.writeToString(np, RDFFormat.TRIG) + "\n");
	}
	
	public void printServerList(){
		ServerIterator serverIterator = new ServerIterator();
		while (serverIterator.hasNext()) {
			ServerInfo si = serverIterator.next();
			System.out.println("==========");
			System.out.println("Server: " + si.getPublicUrl() + "\n");
		}
	}
	
	public void printServerInfo(ServerInfo si){
		System.out.println("Server URL:          " + si.getPublicUrl());
		System.out.println("Protocol version:    " + si.getProtocolVersion());
		System.out.println("Description:         " + si.getDescription());
		String ad = si.getAdmin();
		System.out.println("Admin:               " + (ad == null || ad.isEmpty() ? "(unknown)" : ad));
		System.out.println("Journal ID:          " + si.getJournalId());
		System.out.println("Page size:           " + si.getPageSize());
		System.out.println("Post peers:          " + (si.isPostPeersEnabled() ? "enabled" : "disabled"));
		System.out.println("Post nanopubs:       " + (si.isPostNanopubsEnabled() ? "enabled" : "disabled"));
		System.out.println("Nanopub count:       " + (si.getNextNanopubNo()-1));
		System.out.println("Max nanopubs:        " + (si.getMaxNanopubs() == null ? "unrestricted" : si.getMaxNanopubs()));
		System.out.println("Max triples/nanopub: " + (si.getMaxNanopubTriples() == null ? "unrestricted" : si.getMaxNanopubTriples()));
		System.out.println("Max bytes/nanopub:   " + (si.getMaxNanopubBytes() == null ? "unrestricted" : si.getMaxNanopubBytes()));
		System.out.println();
	}

}
