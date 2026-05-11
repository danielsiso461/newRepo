package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class AbstractDBConnection {
    protected Connection conn;

    // Connection details - SAME for all subclasses
    private static final String URL =
        "jdbc:mysql://localhost:3306/gonature?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "Aa123456";

    // Connect once - no need for abstract methods
    public void connect() throws SQLException {
        conn = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Only thing that changes between subclasses
    protected abstract String getTableName();
    
    public void updateFields(String[] columnNames, List<Object> newValues,
    		String[] keyColumns, List<Object> keyValues) throws SQLException {
    	
    	if(columnNames.length != newValues.size() || 
    			keyColumns.length != keyValues.size()) {
    		System.out.println("bad sql update request");
    		return;
    	}
    		
    	StringBuilder sql = new StringBuilder("UPDATE `" + getTableName() + "` SET ");
    	for(String s : columnNames) {
    		sql.append(s + " = ?, ");
    	}
    	//remove comma
    	sql.setLength(sql.length() - 2);
    	
    	sql.append(" WHERE ");
    	
    	for(String s : keyColumns) {
    		sql.append(s + " = ?, ");
    	}
    	//remove comma
    	sql.setLength(sql.length() - 2);
    	//close the query
    	sql.append(";");
    	
        PreparedStatement pstmt = conn.prepareStatement(sql.toString());
        
        // set parameters
	    for (int i = 0; i < newValues.size(); i++) {
	        pstmt.setObject(i + 1, newValues.get(i));
	    }
	    
	    for (int i = newValues.size(); i < newValues.size() + keyValues.size(); i++) {
	        pstmt.setObject(i + 1, keyValues.get(i - newValues.size()));
	    }
        
        int rows = pstmt.executeUpdate();

        if (rows > 0) {
            System.out.println("Update completed successfully!");
        } else {
            System.out.println("Update failed: record not found.");
        }

        pstmt.close();
    }
    
    public String selectByFields(String[] columnNames, String[] keyColumns) {
    	StringBuilder sql = new StringBuilder("SELECT ");
    	for(String s : columnNames) {
    		sql.append(s + ", ");
    	}
    	//remove comma
    	sql.setLength(sql.length() - 2);
    	
    	sql.append(" FROM `" + getTableName() + "` WHERE ");
    	
    	for(String s : keyColumns) {
    		sql.append(s + " = ?, ");
    	}
    	//remove comma
    	sql.setLength(sql.length() - 2);
    	//close the query
    	sql.append(";");
    	
    	return sql.toString();
    }

    public void close() throws SQLException {
        if (conn != null) conn.close();
    }
}