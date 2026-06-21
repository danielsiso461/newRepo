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
     */
    public void connect() throws SQLException {
        conn = DBConnectionPool.getInstance().getConnection();
    }

    /**
     * Makes sure the database connection is open.
     */
    protected void ensureConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            connect();
        }
    }

    /**
     * Saves the DB password entered by the user.
     */
    public static void setPassword(String dbPassword) {
        DBConnectionPool.getInstance().setPassword(dbPassword);
    }

    /**
     * Checks if the entered DB password is correct.
     */
    public static boolean testConnection(String dbPassword) {
        return DBConnectionPool.testConnection(dbPassword);
    }

    /**
     * Returns the table name used by the specific subclass.
     */
    protected abstract String getTableName();

    /**
     * General UPDATE query.
     */
    public boolean updateFields(String[] columnNames, List<Object> newValues,
            String[] keyColumns, List<Object> keyValues) throws SQLException {

        ensureConnection();

        if (columnNames == null || newValues == null
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

        StringBuilder sql = new StringBuilder("UPDATE `" + getTableName() + "` SET ");

        for (String column : columnNames) {
            sql.append(column).append(" = ?, ");
        }

        sql.setLength(sql.length() - 2);
        sql.append(" WHERE ");

        for (String key : keyColumns) {
            sql.append(key).append(" = ? AND ");
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
     * General INSERT query.
     */
    public void insertFields(String[] columnNames, List<Object> values) throws SQLException {
        ensureConnection();

        if (columnNames.length != values.size()) {
            System.out.println("bad sql insert request");
            return;
        }

        StringBuilder sql = new StringBuilder("INSERT INTO `" + getTableName() + "` (");

        for (String column : columnNames) {
            sql.append(column).append(", ");
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
     */
    public int insertFieldsAndReturnGeneratedKey(String[] columnNames, List<Object> values)
            throws SQLException {

        ensureConnection();

        if (columnNames.length != values.size()) {
            System.out.println("bad sql insert request");
            return -1;
        }

        StringBuilder sql = new StringBuilder("INSERT INTO `" + getTableName() + "` (");

        for (String column : columnNames) {
            sql.append(column).append(", ");
        }

        sql.setLength(sql.length() - 2);
        sql.append(") VALUES (");

        for (int i = 0; i < values.size(); i++) {
            sql.append("?, ");
        }

        sql.setLength(sql.length() - 2);
        sql.append(");");

        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
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
     * Builds a SELECT query with WHERE conditions.
     */
    public String selectByFields(String[] columnNames, String[] keyColumns) {
        StringBuilder sql = new StringBuilder("SELECT ");

        for (String column : columnNames) {
            sql.append(column).append(", ");
        }

        sql.setLength(sql.length() - 2);
        sql.append(" FROM `" + getTableName() + "` WHERE ");

        for (String key : keyColumns) {
            sql.append(key).append(" = ? AND ");
        }

        sql.setLength(sql.length() - 5);
        sql.append(";");

        return sql.toString();
    }

    /**
     * Builds a SELECT query with WHERE conditions connected by AND.
     */
    public String selectByFieldsAND(String[] columnNames, String[] keyColumns) {
        return selectByFields(columnNames, keyColumns);
    }

    /**
     * Returns the DB connection to the connection pool.
     */
    public void close() throws SQLException {
        if (conn != null) {
            DBConnectionPool.getInstance().releaseConnection(conn);
            conn = null;
        }
    }
}