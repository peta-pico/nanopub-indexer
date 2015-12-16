package org.petapico.nanopub.indexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ServerFunctions {

	public static List<String> grabNanopubIdsFromPage(String server, int page) throws IOException{
		List<String> nanopubIDs = new ArrayList<String>();
		String url = server +  "/nanopubs.txt?page=" + page;
		
		URL oracle = new URL(url);
        BufferedReader in = new BufferedReader(
        new InputStreamReader(oracle.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null)
        	nanopubIDs.add(inputLine);
	    in.close();
	    
		return nanopubIDs;
	}
}

