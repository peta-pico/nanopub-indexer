package org.petapico.nanopub.indexer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DatabaseFunctions {
	
	/*
	 * 
	 */
	public static void insertNpIntoDatabase(){
		
	}
	
	/*
	 * 
	 */
	public static int insertNpAuthorIntoDatabase(){
		return 0;
	}
	
	public static void executeGetRequest(String str_url) throws IOException{
		URL url = new URL(str_url);
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		if (conn.getResponseCode() != 200) {
			throw new IOException(conn.getResponseMessage());
		}
		
		conn.disconnect();
	}
}
