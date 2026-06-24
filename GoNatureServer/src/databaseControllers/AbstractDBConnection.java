package databaseControllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Base class for database connector classes.
 */
public abstract class AbstractDBConnection {

	protected Connection conn;

	/**
	 * Connects to the database by taking a connection from the connection pool.
	 * 
	 * @throws SQLException if the connection to the database fails
	 */
	public void connect() throws SQLException {
		conn = DBConnectionPool.getInstance().getConnection();
	}

	/**
	 * Makes sure the database connection is open.
	 * 
	 * @throws SQLException if reconnecting to the database fails
	 */
	protected void ensureConnection() throws SQLException {
		if (conn == null || conn.isClosed()) {
			connect();
		}
	}

	/**
	 * Saves the DB password entered by the user.
	 * 
	 * @param dbPassword the database password
	 */
	public static void setPassword(String dbPassword) {
		DBConnectionPool.getInstance().setPassword(dbPassword);
	}

	/**
	 * Checks if the entered DB password is correct.
	 * 
	 * @param dbPassword the database password to test
	 * @return true if the connection succeeds, otherwise false
	 */
	public static boolean testConnection(String dbPassword) {
		return DBConnectionPool.testConnection(dbPassword);
	}

	/**
	 * Returns the table name used by the specific subclass.
	 * 
	 * @return the database table name
	 */
	protected abstract String getTableName();

	/**
	 * General UPDATE query for the table used by the specific subclass.
	 * 
	 * @param columnNames the columns to update
	 * @param newValues the new values for the updated columns
	 * @param keyColumns the columns used in the WHERE clause
	 * @param keyValues the values used in the WHERE clause
	 * @return true if at least one row was updated, otherwise false
	 * @throws SQLException if the update query fails
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
	 * General UPDATE query for a specific database table.
	 * 
	 * This method is used when a connector needs to update a table that is different
	 * from the table returned by getTableName().
	 * 
	 * @param tableName the table name to update
	 * @param columnNames the columns to update
	 * @param newValues the new values for the updated columns
	 * @param keyColumns the columns used in the WHERE clause
	 * @param keyValues the values used in the WHERE clause
	 * @return true if at least one row was updated, otherwise false
	 * @throws SQLException if the update query fails
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
	 * Executes a general UPDATE query for the given table.
	 * 
	 * @param tableName the table name to update
	 * @param columnNames the columns to update
	 * @param newValues the new values for the updated columns
	 * @param keyColumns the columns used in the WHERE clause
	 * @param keyValues the values used in the WHERE clause
	 * @return true if at least one row was updated, otherwise false
	 * @throws SQLException if the update query fails
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
	 * General INSERT query for the table used by the specific subclass.
	 * 
	 * @param columnNames the columns that appear in the INSERT query
	 * @param values the values corresponding to columnNames
	 * @throws SQLException if the insert query fails
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
	 * General INSERT query for a specific database table.
	 * 
	 * This method is used when a connector needs to insert into a table that is
	 * different from the table returned by getTableName().
	 * 
	 * @param tableName the table name to insert into
	 * @param columnNames the columns that appear in the INSERT query
	 * @param values the values corresponding to columnNames
	 * @throws SQLException if the insert query fails
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
	 * Executes a general INSERT query for the given table.
	 * 
	 * @param tableName the table name to insert into
	 * @param columnNames the columns that appear in the INSERT query
	 * @param values the values corresponding to columnNames
	 * @throws SQLException if the insert query fails
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
	 * General INSERT query that returns the generated primary key.
	 * 
	 * This method is useful when inserting a row into a table with an auto-increment
	 * primary key.
	 * 
	 * @param columnNames the columns that appear in the INSERT query
	 * @param values the values corresponding to columnNames
	 * @return the generated key, or -1 if the insert failed
	 * @throws SQLException if the insert query fails
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
	 * General INSERT query into a specific table that returns the generated primary
	 * key.
	 * 
	 * @param tableName the table name to insert into
	 * @param columnNames the columns that appear in the INSERT query
	 * @param values the values corresponding to columnNames
	 * @return the generated key, or -1 if the insert failed
	 * @throws SQLException if the insert query fails
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
	 * Executes an INSERT query for the given table and returns the generated key.
	 * 
	 * @param tableName the table name to insert into
	 * @param columnNames the columns that appear in the INSERT query
	 * @param values the values corresponding to columnNames
	 * @return the generated key, or -1 if the insert failed
	 * @throws SQLException if the insert query fails
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
	 * Builds a SELECT query for the table used by the specific subclass.
	 * 
	 * If keyColumns is null or empty, the query will not contain a WHERE clause.
	 * If keyColumns has values, the conditions will be connected with AND.
	 * 
	 * @param columnNames the columns to select
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
	 * This method is kept for compatibility with older code.
	 * 
	 * @param columnNames the columns to select
	 * @param keyColumns the columns used in the WHERE clause
	 * @return the generated SELECT query
	 */
	public String selectByFieldsAND(String[] columnNames, String[] keyColumns) {
		return selectByFields(columnNames, keyColumns);
	}

	/**
	 * Builds a SELECT query with FOR UPDATE.
	 * 
	 * This method is used inside transactions when the selected row must be locked
	 * until commit or rollback.
	 * 
	 * @param columnNames the columns to select
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
	 * Returns the DB connection to the connection pool.
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