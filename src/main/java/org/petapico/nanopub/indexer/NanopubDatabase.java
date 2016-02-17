package org.petapico.nanopub.indexer;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NanopubDatabase {

	private Connection conn;
	PreparedStatement insertStmt;
	
	public NanopubDatabase (String dbuser, String dbpass) throws SQLException, ClassNotFoundException{
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://localhost/nanopubs",dbuser, dbpass);
	}
	
	public long getNextNanopubNo(String server){
		return 0;
	}
	
	public long getJournalId(String server){
		return 0;
		
	}
	
	public void updateJournalId(long Jid){
		
	}
	
	public void updateNextNanopubNo(long NextNanopubNo){
		
	}
	
	public boolean prepareInsertPs(String query){
		 try {
			 insertStmt = conn.prepareStatement(query);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean runInsertPs(){
		try {
			insertStmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean setUriInsertPs(String uri){
		try {
			insertStmt.setString(1, uri);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean setArtifactCodeInsertPs(String artifactCode){
		try {
			insertStmt.setString(2, artifactCode);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean setSectionInsertPs(int sectionID){
		try {
			insertStmt.setInt(3, sectionID);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
