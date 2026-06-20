package databaseControllers;

import java.sql.Connection;
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

	/**
	 * Connects to the database by taking a connection from the connection pool.
	 * 
	 * @throws SQLException if getting a connection from the pool fails
	 */
	public void connect() throws SQLException {
		conn = DBConnectionPool.getInstance().getConnection();
	}

	/**
	 * Saves the DB password entered by the user.
	 * 
	 * @param dbPassword the database password entered by the user
	 */
	public static void setPassword(String dbPassword) {
		DBConnectionPool.getInstance().setPassword(dbPassword);
	}

	/**
	 * Checks if the entered DB password is correct.
	 * 
	 * @param dbPassword the password entered by the user
	 * @return true if the connection succeeds, false otherwise
	 */
	public static boolean testConnection(String dbPassword) {
		return DBConnectionPool.testConnection(dbPassword);
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
	 * This method is a general insert query.
	 * It inserts a new record into the table using the given columns and values.
	 * 
	 * @param columnNames the columns that appear in the INSERT query
	 * @param values      the values corresponding to columnNames
	 * @throws SQLException if the insert query fails
	 */
	public void insertFields(String[] columnNames, List<Object> values) throws SQLException {

		if (columnNames.length != values.size()) {
			System.out.println("bad sql insert request");
			return;
		}

		StringBuilder sql = new StringBuilder("INSERT INTO `" + getTableName() + "` (");

		for (String s : columnNames) {
			sql.append(s + ", ");
		}

		sql.setLength(sql.length() - 2);
		sql.append(") VALUES (");

		for (int i = 0; i < values.size(); i++) {
			sql.append("?, ");
		}

		sql.setLength(sql.length() - 2);
		sql.append(");");

		PreparedStatement pstmt = conn.prepareStatement(sql.toString());

		for (int i = 0; i < values.size(); i++) {
			pstmt.setObject(i + 1, values.get(i));
		}

		int rows = pstmt.executeUpdate();

		if (rows > 0) {
			System.out.println("Insert completed successfully!");
		} else {
			System.out.println("Insert failed.");
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
	 * This method constructs a general SELECT query where the conditions are connected by a logical AND
	 * into a string in the format of a
	 * PreparedStatement and returns it.
	 * 
	 * @param columnNames the columns that appear after SELECT in a SELECT query
	 * @param keyColumns  the columns that appear after WHERE in a SELECT query
	 * @return the SELECT query as a String
	 */
	public String selectByFieldsAND(String[] columnNames, String[] keyColumns) {
		StringBuilder sql = new StringBuilder("SELECT ");

		for (String s : columnNames) {
			sql.append(s + ", ");
		}

		sql.setLength(sql.length() - 2);
		sql.append(" FROM `" + getTableName() + "` WHERE ");

		for (String s : keyColumns) {
			sql.append(s + " = ? AND ");
		}

		sql.setLength(sql.length() - 5);
		sql.append(";");

		return sql.toString();
	}
	
	/**
	 * Returns the DB connection to the connection pool.
	 * 
	 * The connection is not closed immediately. It is returned to the pool so other
	 * database connector classes can reuse it.
	 * 
	 * @throws SQLException if returning the connection fails
	 */
	public void close() throws SQLException {
		if (conn != null) {
			DBConnectionPool.getInstance().releaseConnection(conn);
			conn = null;
		}
	}
}