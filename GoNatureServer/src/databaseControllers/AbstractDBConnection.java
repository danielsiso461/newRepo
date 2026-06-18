package databaseControllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * This class sets up methods for general use by different SQL table connectors.
 */
public abstract class AbstractDBConnection {

	/**
	 * The database connection used by the subclasses.
	 */
	protected Connection conn;

	// Connection details - SAME for all subclasses
	private static final String URL = "jdbc:mysql://localhost:3306/gonature?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";
	private static final String USER = "root";
	private static String password = "";

	/**
	 * Connects to the database.
	 * 
	 * @throws SQLException if the connection fails
	 */
	public void connect() throws SQLException {
		if (password == null || password.isEmpty()) {
			throw new SQLException("DB password was not entered.");
		}

		conn = DriverManager.getConnection(URL, USER, password);
	}
	
	/*
	 * this function saves the DB password entered by the user
	 */
	public static void setPassword(String dbPassword) {
		password = dbPassword;
	}

	/*
	 * this function checks if the entered DB password is correct
	 * 
	 * @param dbPassword the password entered by the user
	 * @return true if the connection succeeds, false otherwise
	 */
	public static boolean testConnection(String dbPassword) {
		try {
			Connection testConn = DriverManager.getConnection(URL, USER, dbPassword);
			testConn.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	/**
	 * Returns the table name used by the specific subclass.
	 * 
	 * @return the table name
	 */
	protected abstract String getTableName();

	/**
	 * This method is a general update query.
	 * 
	 * @param columnNames the columns that appear after SET in an update query
	 * @param newValues   the values corresponding to columnNames
	 * @param keyColumns  the columns that appear after WHERE in an update query
	 * @param keyValues   the values corresponding to keyColumns
	 * @throws SQLException if the update query fails
	 */
	public void updateFields(String[] columnNames, List<Object> newValues, String[] keyColumns, List<Object> keyValues)
			throws SQLException {

		if (columnNames.length != newValues.size() || keyColumns.length != keyValues.size()) {
			System.out.println("bad sql update request");
			return;
		}

		StringBuilder sql = new StringBuilder("UPDATE `" + getTableName() + "` SET ");

		for (String s : columnNames) {
			sql.append(s + " = ?, ");
		}

		sql.setLength(sql.length() - 2);
		sql.append(" WHERE ");

		for (String s : keyColumns) {
			sql.append(s + " = ?, ");
		}

		sql.setLength(sql.length() - 2);
		sql.append(";");

		PreparedStatement pstmt = conn.prepareStatement(sql.toString());

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
	
	/**
	 * This method constructs a general SELECT query into a string in the format of a
	 * PreparedStatement and returns it.
	 * 
	 * @param columnNames the columns that appear after SELECT in a SELECT query
	 * @param keyColumns  the columns that appear after WHERE in a SELECT query
	 * @return the SELECT query as a String
	 */
	public String selectByFields(String[] columnNames, String[] keyColumns) {
		StringBuilder sql = new StringBuilder("SELECT ");

		for (String s : columnNames) {
			sql.append(s).append(", ");
		}

		sql.setLength(sql.length() - 2);
		sql.append(" FROM `").append(getTableName()).append("`");

		if (keyColumns != null && keyColumns.length > 0) {
			sql.append(" WHERE ");

			for (String s : keyColumns) {
				sql.append(s).append(" = ? AND ");
			}

			sql.setLength(sql.length() - 5);
		}

		sql.append(";");

		return sql.toString();
	}

	/**
	 * This method closes the DB connection.
	 * 
	 * @throws SQLException if closing the connection fails
	 */
	public void close() throws SQLException {
		if (conn != null && !conn.isClosed()) {
			conn.close();
		}
	}
}