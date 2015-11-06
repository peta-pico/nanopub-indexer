package org.petapico.nanopub.indexer;

import java.io.IOException;
import java.util.List;

import org.nanopub.Nanopub;
import org.nanopub.NanopubUtils;
import org.nanopub.extra.server.GetNanopub;
import org.nanopub.extra.server.NanopubServerUtils;
import org.nanopub.extra.server.ServerInfo;
import org.nanopub.extra.server.ServerIterator;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;

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
		int totalNanopubs = 1; //(int) (si.getNextNanopubNo()-1);
		while (currentNanopub < totalNanopubs){ //There should be a page left
			//read from the next page
			page += 1; 
			List<String> nanopubsOnPage = NanopubServerUtils.loadNanopubUriList(si, page);
			
			if (nanopubsOnPage.size() == 0){
				break; //There are no nanopubs on this page
			}
			
			for (String nanopubId : nanopubsOnPage) {
				currentNanopub++;
				nanopubId = "http://liddi.stanford.edu/LIDDI_resource:SID4081_SID1091_EID4966_nanopub.RA12X7AcvLKucG7Y5ygnf57SiGGj_QXgLfgW-BjqNk7G0";
				
				System.out.println("NanopubID: " + nanopubId);
				
				String ac = org.nanopub.extra.server.GetNanopub.getArtifactCode(nanopubId); //GET IDENTIFIER
				System.out.println("ArtifactCode: " + ac);
				
				Nanopub np = GetNanopub.get(nanopubId);
				System.out.println("Authors:\n" + np.getAuthors().toString());
				System.out.println("Creators:\n" + np.getCreators().toString());
				System.out.println("Creationtime:\n" + np.getCreationTime().getTimeInMillis());
				//System.out.println("Nanopub content:\n" + NanopubUtils.writeToString(np, RDFFormat.TRIG) + "\n");
				
				//np 
				break; //break after 1 nanopub
				
			}
		}
		//System.out.println("all pages visited: "+ page);
	}
	
	/* TODO
	 * create database tuples containing: nanopubID, nanopubURL
	 * 
	 */
	
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
