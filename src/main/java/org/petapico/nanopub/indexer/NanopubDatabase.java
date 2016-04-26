package org.petapico.nanopub.indexer;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NanopubDatabase {

	private Connection conn;
	PreparedStatement insertStmt;
	PreparedStatement insertIgnoreStmt;
	PreparedStatement selectNpStmt;
	PreparedStatement updateNpStmt;
	PreparedStatement insertNpStmt;
	
	public NanopubDatabase (String dbuser, String dbpass) throws SQLException, ClassNotFoundException{
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://localhost/nanopubs",dbuser, dbpass);
		insertStmt = conn.prepareStatement("INSERT INTO uris VALUES(?,?,?)");
		insertIgnoreStmt = conn.prepareStatement("INSERT IGNORE INTO uris VALUES(?,?,?)");
		
		selectNpStmt = conn.prepareStatement("SELECT finished FROM nanopubs WHERE artifactCode = ?");
		updateNpStmt = conn.prepareStatement("UPDATE nanopubs SET finished = 1 WHERE artifactCode = ?");
		insertNpStmt = conn.prepareStatement("INSERT INTO nanopubs VALUES (?, 0)");
	}
	
	private boolean insertServer(String serverName){
		try {
			String query = "INSERT INTO servers (serverName) VALUES (?)";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, serverName);
			stmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public long getNextNanopubNo(String serverName) throws SQLException{
		long result = 0;
		String query = "SELECT nextNanopubNo FROM servers WHERE serverName = ? LIMIT 1";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, serverName);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()){
			result = rs.getLong(1);
		}
		else {
			insertServer(serverName);
		}
		
		return result;
	}
	
	public void insertNp(String artifactCode) throws SQLException{
		insertNpStmt.setString(1, artifactCode);
		insertNpStmt.executeUpdate();
	}
	
	public void updateNpFinished(String artifactCode) throws SQLException {
		updateNpStmt.setString(1, artifactCode);
		updateNpStmt.executeUpdate();
	}
	
	public int npInserted(String artifactCode) throws SQLException{
		selectNpStmt.setString(1, artifactCode);
		ResultSet rs = selectNpStmt.executeQuery();
		if (rs.next()){
			return rs.getInt("finished");
			
		}
		return -1;
	}
	
	public long getJournalId(String serverName) throws SQLException{
		long result = 0;
		String query = "SELECT journalId FROM servers WHERE serverName = ? LIMIT 1";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, serverName);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()){
			result = rs.getLong(1);
		}
		else {
			insertServer(serverName);
		}
		return result;
	}
	
	public void updateJournalId(String serverName, long Jid) throws SQLException{
		String query = "UPDATE servers SET journalId = ? WHERE serverName = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setLong(1, Jid);
		stmt.setString(2, serverName);
		//String rs = stmt.toString();
		//System.out.printf("%s %d %s\n", query, Jid, serverName);
		//System.out.printf("%s\n", rs);
		stmt.executeUpdate();
	}
	
	public void updateNextNanopubNo(String serverName, long NextNanopubNo) throws SQLException{
		String query = "UPDATE servers SET nextNanopubNo = ? WHERE serverName = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setLong(1, NextNanopubNo);
		stmt.setString(2, serverName);
		String rs = stmt.toString();
		System.out.printf("%s %d %s\n", query, NextNanopubNo, serverName);
		System.out.printf("%s\n", rs);
		stmt.executeUpdate();
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
	
	public PreparedStatement getInsertIgnoreStmt(){
		return insertIgnoreStmt;
	}
	
	public PreparedStatement getInsertStmt(){
		return insertStmt;
	}
}
