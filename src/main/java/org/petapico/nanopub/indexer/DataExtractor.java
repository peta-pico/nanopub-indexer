package org.petapico.nanopub.indexer;

import java.io.IOException;

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
		ServerIterator serverIterator = new ServerIterator();
		if (!serverIterator.hasNext()){
			System.out.println("ERROR: NO SERVERS FOUND");
			System.exit(0);
		}
		ServerInfo si = serverIterator.next();
		System.out.println("==========");
		System.out.println("Server: " + si.getPublicUrl() + "\n");
		// This is just a test so we only check the first page of nanopubs:
		int page = 0;
		int nanopubNumber = 0;
		int totalNanopubs = (int) (si.getNextNanopubNo()-1);
		while (nanopubNumber < totalNanopubs){ //There should be a page left
			page += 1; //read from the next page
			System.out.println("page: " + page);
			for (String nanopubId : NanopubServerUtils.loadNanopubUriList(si, page)) {
				String ac = org.nanopub.extra.server.GetNanopub.getArtifactCode(nanopubId); //GET IDENTIFIER
				
				System.out.println("Nanopub ID: " + ac);
				
				nanopubNumber++;
				Nanopub np = GetNanopub.get(nanopubId);
				//System.out.println("Nanopub content:\n" + NanopubUtils.writeToString(np, RDFFormat.TRIG) + "\n");
				if (nanopubNumber > 3) {
					// This is just a test, so we stop after 5 nanopubs...
					break;
				}
			}
		}
		System.out.println("all pages visited: "+ page);
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
