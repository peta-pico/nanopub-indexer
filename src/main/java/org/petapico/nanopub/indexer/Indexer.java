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

public class Indexer {

	public static void main(String[] args) throws IOException, RDFHandlerException {
		Indexer indexer = new Indexer();
		indexer.run();
	}

	public Indexer() {
	}

	public void run() throws IOException, RDFHandlerException {
		ServerIterator serverIterator = new ServerIterator();
		while (serverIterator.hasNext()) {
			ServerInfo si = serverIterator.next();
			System.out.println("==========");
			System.out.println("Server: " + si.getPublicUrl());
			// This is just a test so we only check the first page of nanopubs:
			int page = 1;
			int nanopubNumber = 0;
			for (String nanopubId : NanopubServerUtils.loadNanopubUriList(si, page)) {
				System.out.println("Nanopub ID: " + nanopubId);
				nanopubNumber++;
				Nanopub np = GetNanopub.get(nanopubId);
				System.out.println("Nanopub content: " + NanopubUtils.writeToString(np, RDFFormat.TRIG));
				if (nanopubNumber > 5) {
					// This is just a test, so we stop after 5 nanopubs...
					break;
				}
			}
		}
	}

}
