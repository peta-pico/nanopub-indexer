package org.petapico.nanopub.indexer;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.nanopub.Nanopub;
import org.nanopub.extra.server.GetNanopub;
import org.nanopub.extra.server.NanopubServerUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class Indexes {

	Connection conn;
	PreparedStatement insertIndexStmt;
	
	public Indexes (String dbuser, String dbpass) throws ClassNotFoundException, SQLException{
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://localhost/nanopubs",dbuser, dbpass);
		insertIndexStmt = conn.prepareStatement("INSERT IGNORE INTO indexes VALUES(?,?,?)");
	}
	
	public void start(){
		
		String url = "http://petapico.d2s.labs.vu.nl/api/database/api.php?search-uri=http%3A%2F%2Fpurl.org%2Fnanopub%2Fx%2FincludesElement%0D%0A%0D%0A&page=0&head=on&assertion=on&provenance=on&pubinfo=on&format=text";
		
		// read all index nanopubs
		List<String> nanopubIndexes = null;
		try {
			nanopubIndexes = NanopubServerUtils.loadList(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.printf("server error\n");
		}

		// loop through all index nanopub
		System.out.printf("Going through: %s\n", nanopubIndexes.size());
		
		for (String artifactCode : nanopubIndexes){
			Nanopub np = GetNanopub.get(artifactCode);
			
			boolean isInserted = false;
			if (!isInserted){
				int childNodes = 0;
				System.out.printf("%s \n", artifactCode);
				
				Set<Statement> statements = null;
				try {
					statements = np.getAssertion();
					for (Statement statement : statements){
						try {
							Value object = statement.getObject();
							URI predicate = statement.getPredicate();
							Resource subject = statement.getSubject();
							
							String predicateStr = predicate.toString();
							if (predicateStr.equals("http://purl.org/nanopub/x/includesElement")){
								childNodes ++;//System.out.printf("%s %s %s\n", object.toString(), predicate.toString(), subject.toString());
							}
						}
						catch (Exception E){
							System.out.printf("statement error\n");
						}	
					}
				}
				catch (Exception E){
					System.out.printf("Error no assertions found\n");
				}
				
				System.out.printf("children: %d\n", childNodes);
				String title = null;
				try {
					statements = np.getPubinfo();
					for (Statement statement : statements){
						try {
							Value object = statement.getObject();
							URI predicate = statement.getPredicate();
							Resource subject = statement.getSubject();
							
							String predicateStr = predicate.toString();
							String subjectStr = subject.toString();
							if (predicateStr.equals("http://purl.org/dc/elements/1.1/title") && subjectStr.equals(artifactCode)){
								// System.out.printf("1:%s\n2:%s\n3:%s\n\n", object.toString(), predicate.toString(), subject.toString());
								title = object.toString();
							}
						}
						catch (Exception E){
							System.out.printf("statement2 error\n");
						}
						
					}
				}
				catch (Exception E){
					System.out.printf("Error no pubinfo found\n");
				}
				
				
				try {
					insertIndex(artifactCode, title, childNodes);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					System.out.printf("insert error\n");
				}
			}
		}
	}
	
	public void insertIndex(String artifactCode, String title, int children) throws SQLException{
		insertIndexStmt.setString(1, artifactCode);
		insertIndexStmt.setString(2, title);
		insertIndexStmt.setInt(3, children);
		insertIndexStmt.executeUpdate();
	}
	
	public static void main(String[] args){
		
		/*
		args = new String[3];
		args[0] = "root";
		args[1] = "admin";
		args[2] = "true";
		*/
		
		if (args.length < 2){
			System.out.printf("Invalid arguments expected: dbusername, dbpassword\n");
			System.exit(1);
		}
		
		try {
			new Indexes(args[0], args[1]).start();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.printf("object error\n");
		}
	}
}
