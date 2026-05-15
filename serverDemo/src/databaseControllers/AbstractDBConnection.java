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
	private static final String PASSWORD = "Aa123456";

	/**
	 * Connects to the database.
	 * 
	 * @throws SQLException if the connection fails
	 */
	public void connect() throws SQLException {
		conn = DriverManager.getConnection(URL, USER, PASSWORD);
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
			sql.append(s + ", ");
		}

		sql.setLength(sql.length() - 2);
		sql.append(" FROM `" + getTableName() + "` WHERE ");

		for (String s : keyColumns) {
			sql.append(s + " = ?, ");
		}

		sql.setLength(sql.length() - 2);
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