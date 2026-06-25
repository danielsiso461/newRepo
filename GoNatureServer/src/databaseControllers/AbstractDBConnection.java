package databaseControllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Provides a common base for all database connector classes.
 * 
 * This class manages the database connection and supplies reusable helper
 * methods for executing general INSERT and UPDATE operations, as well as
 * building SELECT queries for subclasses.
 */
public abstract class AbstractDBConnection {

	protected Connection conn;

	/**
	 * Opens a database connection by retrieving one from the connection pool.
	 * 
	 * @throws SQLException if the connection cannot be obtained
	 */
	public void connect() throws SQLException {
		conn = DBConnectionPool.getInstance().getConnection();
	}

	/**
	 * Ensures that the current database connection is valid and open.
	 * 
	 * If no connection exists, or if the existing connection is closed, a new
	 * connection is opened from the connection pool.
	 * 
	 * @throws SQLException if reconnecting to the database fails
	 */
	protected void ensureConnection() throws SQLException {
		if (conn == null || conn.isClosed()) {
			connect();
		}
	}

	/**
	 * Stores the database password entered by the user in the connection pool.
	 * 
	 * @param dbPassword the database password to store
	 */
	public static void setPassword(String dbPassword) {
		DBConnectionPool.getInstance().setPassword(dbPassword);
	}

	/**
	 * Tests whether the provided database password can establish a connection.
	 * 
	 * @param dbPassword the database password to test
	 * @return true if the connection test succeeds, otherwise false
	 */
	public static boolean testConnection(String dbPassword) {
		return DBConnectionPool.testConnection(dbPassword);
	}

	/**
	 * Returns the database table name handled by the specific subclass.
	 * 
	 * @return the table name used by the connector
	 */
	protected abstract String getTableName();

	/**
	 * Updates fields in the table associated with the current subclass.
	 * 
	 * @param columnNames the names of the columns to update
	 * @param newValues the new values for the updated columns
	 * @param keyColumns the columns used in the WHERE clause
	 * @param keyValues the values used for the WHERE clause conditions
	 * @return true if at least one row was updated, otherwise false
	 * @throws SQLException if the update operation fails
	 */
	public boolean updateFields(String[] columnNames, List<Object> newValues,
			String[] keyColumns, List<Object> keyValues) throws SQLException {

		return updateFieldsInSpecificTable(
				getTableName(),
				columnNames,
				newValues,
				keyColumns,
				keyValues
		);
	}

	/**
	 * Updates fields in a specific database table.
	 * 
	 * This method is used when a connector needs to update a table other than the
	 * one returned by getTableName().
	 * 
	 * @param tableName the name of the table to update
	 * @param columnNames the names of the columns to update
	 * @param newValues the new values for the updated columns
	 * @param keyColumns the columns used in the WHERE clause
	 * @param keyValues the values used for the WHERE clause conditions
	 * @return true if at least one row was updated, otherwise false
	 * @throws SQLException if the update operation fails
	 */
	protected boolean updateFieldsInTable(String tableName, String[] columnNames,
			List<Object> newValues, String[] keyColumns, List<Object> keyValues)
			throws SQLException {

		return updateFieldsInSpecificTable(
				tableName,
				columnNames,
				newValues,
				keyColumns,
				keyValues
		);
	}

	/**
	 * Executes a general UPDATE statement for the given table.
	 * 
	 * The method builds the SQL statement dynamically according to the columns and
	 * key fields received as parameters.
	 * 
	 * @param tableName the name of the table to update
	 * @param columnNames the names of the columns to update
	 * @param newValues the new values for the updated columns
	 * @param keyColumns the columns used in the WHERE clause
	 * @param keyValues the values used for the WHERE clause conditions
	 * @return true if at least one row was updated, otherwise false
	 * @throws SQLException if the update operation fails
	 */
	private boolean updateFieldsInSpecificTable(String tableName,
			String[] columnNames, List<Object> newValues,
			String[] keyColumns, List<Object> keyValues) throws SQLException {

		ensureConnection();

		if (tableName == null || tableName.isBlank()
				|| columnNames == null || newValues == null
				|| keyColumns == null || keyValues == null) {
			System.out.println("bad sql update request");
			return false;
		}

		if (columnNames.length != newValues.size()
				|| keyColumns.length != keyValues.size()) {
			System.out.println("bad sql update request");
			return false;
		}

		if (columnNames.length == 0 || keyColumns.length == 0) {
			System.out.println("bad sql update request");
			return false;
		}

		StringBuilder sql = new StringBuilder("UPDATE `");
		sql.append(tableName).append("` SET ");

		for (String column : columnNames) {
			sql.append("`").append(column).append("` = ?, ");
		}

		sql.setLength(sql.length() - 2);
		sql.append(" WHERE ");

		for (String key : keyColumns) {
			sql.append("`").append(key).append("` = ? AND ");
		}

		sql.setLength(sql.length() - 5);
		sql.append(";");

		try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < newValues.size(); i++) {
				pstmt.setObject(i + 1, newValues.get(i));
			}

			for (int i = 0; i < keyValues.size(); i++) {
				pstmt.setObject(newValues.size() + i + 1, keyValues.get(i));
			}

			int rows = pstmt.executeUpdate();

			if (rows > 0) {
				System.out.println("Update completed successfully!");
				return true;
			}

			System.out.println("Update failed: record not found.");
			return false;
		}
	}

	/**
	 * Inserts a new record into the table associated with the current subclass.
	 * 
	 * @param columnNames the names of the columns included in the INSERT statement
	 * @param values the values to insert into the matching columns
	 * @throws SQLException if the insert operation fails
	 */
	public void insertFields(String[] columnNames, List<Object> values)
			throws SQLException {

		insertFieldsInSpecificTable(
				getTableName(),
				columnNames,
				values
		);
	}

	/**
	 * Inserts a new record into a specific database table.
	 * 
	 * This method is used when a connector needs to insert into a table other than
	 * the one returned by getTableName().
	 * 
	 * @param tableName the name of the table to insert into
	 * @param columnNames the names of the columns included in the INSERT statement
	 * @param values the values to insert into the matching columns
	 * @throws SQLException if the insert operation fails
	 */
	protected void insertFieldsInTable(String tableName, String[] columnNames,
			List<Object> values) throws SQLException {

		insertFieldsInSpecificTable(
				tableName,
				columnNames,
				values
		);
	}

	/**
	 * Executes a general INSERT statement for the given table.
	 * 
	 * The method builds the SQL statement dynamically according to the provided
	 * column names and values.
	 * 
	 * @param tableName the name of the table to insert into
	 * @param columnNames the names of the columns included in the INSERT statement
	 * @param values the values to insert into the matching columns
	 * @throws SQLException if the insert operation fails
	 */
	private void insertFieldsInSpecificTable(String tableName, String[] columnNames,
			List<Object> values) throws SQLException {

		ensureConnection();

		if (tableName == null || tableName.isBlank()
				|| columnNames == null || values == null
				|| columnNames.length != values.size()) {
			System.out.println("bad sql insert request");
			return;
		}

		if (columnNames.length == 0) {
			System.out.println("bad sql insert request");
			return;
		}

		StringBuilder sql = new StringBuilder("INSERT INTO `");
		sql.append(tableName).append("` (");

		for (String column : columnNames) {
			sql.append("`").append(column).append("`, ");
		}

		sql.setLength(sql.length() - 2);
		sql.append(") VALUES (");

		for (int i = 0; i < values.size(); i++) {
			sql.append("?, ");
		}

		sql.setLength(sql.length() - 2);
		sql.append(");");

		try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < values.size(); i++) {
				pstmt.setObject(i + 1, values.get(i));
			}

			int rows = pstmt.executeUpdate();

			if (rows > 0) {
				System.out.println("Insert completed successfully!");
			} else {
				System.out.println("Insert failed.");
			}
		}
	}

	/**
	 * Inserts a new record into the subclass table and returns the generated key.
	 * 
	 * This method is mainly used for tables that contain an auto-increment primary
	 * key.
	 * 
	 * @param columnNames the names of the columns included in the INSERT statement
	 * @param values the values to insert into the matching columns
	 * @return the generated key, or -1 if the insert failed
	 * @throws SQLException if the insert operation fails
	 */
	public int insertFieldsAndReturnGeneratedKey(String[] columnNames,
			List<Object> values) throws SQLException {

		return insertFieldsAndReturnGeneratedKeyInSpecificTable(
				getTableName(),
				columnNames,
				values
		);
	}

	/**
	 * Inserts a new record into a specific table and returns the generated key.
	 * 
	 * @param tableName the name of the table to insert into
	 * @param columnNames the names of the columns included in the INSERT statement
	 * @param values the values to insert into the matching columns
	 * @return the generated key, or -1 if the insert failed
	 * @throws SQLException if the insert operation fails
	 */
	protected int insertFieldsAndReturnGeneratedKeyInTable(String tableName,
			String[] columnNames, List<Object> values) throws SQLException {

		return insertFieldsAndReturnGeneratedKeyInSpecificTable(
				tableName,
				columnNames,
				values
		);
	}

	/**
	 * Executes an INSERT statement for the given table and returns the generated key.
	 * 
	 * The generated key is retrieved from the database after a successful insert
	 * operation.
	 * 
	 * @param tableName the name of the table to insert into
	 * @param columnNames the names of the columns included in the INSERT statement
	 * @param values the values to insert into the matching columns
	 * @return the generated key, or -1 if the insert failed or no key was returned
	 * @throws SQLException if the insert operation fails
	 */
	private int insertFieldsAndReturnGeneratedKeyInSpecificTable(String tableName,
			String[] columnNames, List<Object> values) throws SQLException {

		ensureConnection();

		if (tableName == null || tableName.isBlank()
				|| columnNames == null || values == null
				|| columnNames.length != values.size()) {
			System.out.println("bad sql insert request");
			return -1;
		}

		if (columnNames.length == 0) {
			System.out.println("bad sql insert request");
			return -1;
		}

		StringBuilder sql = new StringBuilder("INSERT INTO `");
		sql.append(tableName).append("` (");

		for (String column : columnNames) {
			sql.append("`").append(column).append("`, ");
		}

		sql.setLength(sql.length() - 2);
		sql.append(") VALUES (");

		for (int i = 0; i < values.size(); i++) {
			sql.append("?, ");
		}

		sql.setLength(sql.length() - 2);
		sql.append(");");

		try (PreparedStatement pstmt = conn.prepareStatement(
				sql.toString(),
				Statement.RETURN_GENERATED_KEYS)) {

			for (int i = 0; i < values.size(); i++) {
				pstmt.setObject(i + 1, values.get(i));
			}

			int rows = pstmt.executeUpdate();

			if (rows == 0) {
				return -1;
			}

			try (ResultSet keys = pstmt.getGeneratedKeys()) {
				if (keys.next()) {
					return keys.getInt(1);
				}
			}
		}

		return -1;
	}

	/**
	 * Builds a SELECT query for the table associated with the current subclass.
	 * 
	 * If no columns are provided, all columns are selected. If key columns are
	 * provided, they are added as WHERE conditions connected with AND.
	 * 
	 * @param columnNames the names of the columns to select, or null to select all
	 * @param keyColumns the columns used in the WHERE clause
	 * @return the generated SELECT query
	 */
	public String selectByFields(String[] columnNames, String[] keyColumns) {
		StringBuilder sql = new StringBuilder("SELECT ");

		if (columnNames == null || columnNames.length == 0) {
			sql.append("*");
		} else {
			for (String column : columnNames) {
				sql.append(column).append(", ");
			}

			sql.setLength(sql.length() - 2);
		}

		sql.append(" FROM `").append(getTableName()).append("`");

		if (keyColumns != null && keyColumns.length > 0) {
			sql.append(" WHERE ");

			for (String key : keyColumns) {
				sql.append(key).append(" = ? AND ");
			}

			sql.setLength(sql.length() - 5);
		}

		sql.append(";");

		return sql.toString();
	}

	/**
	 * Builds a SELECT query with WHERE conditions connected by AND.
	 * 
	 * This method delegates to selectByFields and is kept for compatibility with
	 * older code.
	 * 
	 * @param columnNames the names of the columns to select, or null to select all
	 * @param keyColumns the columns used in the WHERE clause
	 * @return the generated SELECT query
	 */
	public String selectByFieldsAND(String[] columnNames, String[] keyColumns) {
		return selectByFields(columnNames, keyColumns);
	}

	/**
	 * Builds a SELECT query that locks the selected rows for update.
	 * 
	 * This method is intended for transaction-based operations where selected rows
	 * must remain locked until the transaction is committed or rolled back.
	 * 
	 * @param columnNames the names of the columns to select, or null to select all
	 * @param keyColumns the columns used in the WHERE clause
	 * @return the generated SELECT FOR UPDATE query
	 */
	protected String selectByFieldsForUpdate(String[] columnNames,
			String[] keyColumns) {

		String sql = selectByFields(columnNames, keyColumns);

		if (sql.endsWith(";")) {
			sql = sql.substring(0, sql.length() - 1);
		}

		return sql + " FOR UPDATE;";
	}

	/**
	 * Releases the current database connection back to the connection pool.
	 * 
	 * After the connection is released, the local connection reference is cleared.
	 * 
	 * @throws SQLException if releasing the connection fails
	 */
	public void close() throws SQLException {
		if (conn != null) {
			DBConnectionPool.getInstance().releaseConnection(conn);
			conn = null;
		}
	}
}