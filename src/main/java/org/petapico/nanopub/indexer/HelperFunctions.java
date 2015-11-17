package org.petapico.nanopub.indexer;

import org.nanopub.Nanopub;
import org.nanopub.extra.server.ServerInfo;
import org.nanopub.extra.server.ServerIterator;

public final class HelperFunctions {
	public static String getNanopubURI(Nanopub np){
		String nanopubURI;
		try {
			nanopubURI = np.getUri().toString();
		}
		catch (Exception e){
			nanopubURI = "unknown";
		}
		return nanopubURI;
	}
	
	public static String getArtifactCode(String nanopubURI){
		String artifactCode;
		try {
			artifactCode = org.nanopub.extra.server.GetNanopub.getArtifactCode(nanopubURI);
		}
		catch (Exception e){
			artifactCode = "unknown";
		}
		return artifactCode;
	}
	
	public static String getAssertionURI(Nanopub np){
		String assertionURI;
		try {
			assertionURI = np.getAssertionUri().toString();
		}
		catch (Exception e){
			assertionURI = "unknown";
		}
		return assertionURI;
	}
	
	public static String getHeadURI(Nanopub np){
		String headURI;
		try {
			headURI = np.getHeadUri().toString();
		}
		catch (Exception e){
			headURI = "unknown";
		}
		return headURI;
	}
	
	public static String getProvenanceURI(Nanopub np){
		String provenanceURI;
		try {
			provenanceURI = np.getProvenanceUri().toString();
		}
		catch (Exception e){
			provenanceURI = "unknown";
		}
		return provenanceURI;
	}
	
	public static String getPubinfoURI(Nanopub np){
		String pubinfoURI;
		try {
			pubinfoURI = np.getPubinfoUri().toString();
		}
		catch (Exception e){
			pubinfoURI = "unknown";
		}
		return pubinfoURI;
	}
	
	public static long getCreationTime(Nanopub np){
		long creationTime;
		try {
			creationTime = np.getCreationTime().getTimeInMillis();
		}
		catch (Exception e){
			creationTime = 0;
		}
		return creationTime;
	}
	
	public static void printInsertStatement(Nanopub np){
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
	
	public static void printNanopubInfo(Nanopub np){
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
	
	public static void printServerList(){
		ServerIterator serverIterator = new ServerIterator();
		while (serverIterator.hasNext()) {
			ServerInfo si = serverIterator.next();
			System.out.println("==========");
			System.out.println("Server: " + si.getPublicUrl() + "\n");
		}
	}
	
	public static void printServerInfo(ServerInfo si){
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
