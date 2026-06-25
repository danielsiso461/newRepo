
package databaseControllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Manages a reusable pool of database connections.
 * 
 * Instead of creating a new database connection for every database operation,
 * this class keeps a limited number of open connections and reuses them when
 * needed. This improves performance and helps prevent exceeding the allowed
 * number of open database connections.
 */
public class DBConnectionPool {

	private static DBConnectionPool instance;

	private static final String URL = "jdbc:mysql://localhost:3306/gonature?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";
	private static final String USER = "root";

	private static final int INITIAL_POOL_SIZE = 3;
	private static final int MAX_POOL_SIZE = 10;

	private String password;

	private Queue<Connection> availableConnections;
	private int createdConnections;

	/**
	 * Creates an empty connection pool.
	 * 
	 * The constructor is private because this class is implemented as a singleton.
	 * The actual database connections are created only when the pool is first used.
	 */
	private DBConnectionPool() {
		availableConnections = new LinkedList<>();
		createdConnections = 0;
	}

	/**
	 * Returns the single instance of the connection pool.
	 * 
	 * @return the singleton DBConnectionPool instance
	 */
	public static DBConnectionPool getInstance() {
		if (instance == null) { 
			instance = new DBConnectionPool();
		}

		return instance;
	}

	/**
	 * Stores the database password entered by the user.
	 * 
	 * The password is later used when creating new database connections.
	 * 
	 * @param dbPassword the database password to store
	 */
	public void setPassword(String dbPassword) {
		this.password = dbPassword;
	}

	/**
	 * Tests whether the provided database password is valid.
	 * 
	 * This method creates a temporary database connection only for validation and
	 * closes it immediately after the test.
	 * 
	 * @param dbPassword the database password to test
	 * @return true if the connection succeeds, otherwise false
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
	 * Initializes the connection pool with a predefined number of connections.
	 * 
	 * The method creates the initial database connections and stores them in the
	 * queue of available connections.
	 * 
	 * @throws SQLException if the password is missing or if a connection cannot be
	 *         created
	 */
	private void initializePool() throws SQLException {
		if (password == null || password.isEmpty()) {
			throw new SQLException("DB password was not entered.");
		}

		while (createdConnections < INITIAL_POOL_SIZE) {
			availableConnections.add(createNewConnection());
			createdConnections++;
		}
	}

	/**
	 * Creates a new database connection using the configured URL, username, and
	 * password.
	 * 
	 * @return a new database connection
	 * @throws SQLException if the connection cannot be created
	 */
	private Connection createNewConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, password);
	}

	/**
	 * Provides an available database connection from the pool.
	 * 
	 * If an available connection exists, it is returned. If the pool is empty and
	 * the maximum pool size has not been reached, a new connection is created. If
	 * the maximum size has been reached, the method waits until another connection
	 * is released back to the pool.
	 * 
	 * @return an open database connection
	 * @throws SQLException if a connection cannot be provided or if the waiting
	 *         thread is interrupted
	 */
	public synchronized Connection getConnection() throws SQLException {
		if (availableConnections.isEmpty() && createdConnections == 0) {
			initializePool();
		}

		while (availableConnections.isEmpty() && createdConnections >= MAX_POOL_SIZE) {
			try {
				wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new SQLException("Interrupted while waiting for a database connection.", e);
			}
		}

		if (!availableConnections.isEmpty()) {
			Connection connection = availableConnections.poll();

			if (connection != null && !connection.isClosed()) {
				return connection;
			}

			createdConnections--;
			return getConnection();
		}

		Connection connection = createNewConnection();
		createdConnections++;

		return connection;
	}

	/**
	 * Releases a database connection back to the pool.
	 * 
	 * Open connections are stored for reuse by future database operations. Closed
	 * connections are removed from the pool count.
	 * 
	 * @param connection the connection to release back to the pool
	 */
	public synchronized void releaseConnection(Connection connection) {
		if (connection == null) {
			return;
		}

		try {
			if (!connection.isClosed()) {
				availableConnections.add(connection);
				notifyAll();
			} else {
				createdConnections--;
			}
		} catch (SQLException e) {
			createdConnections--;
		}
	}

	/**
	 * Closes all currently available connections in the pool.
	 * 
	 * This method is intended to be called when the server shuts down, in order to
	 * release database resources properly.
	 */
	public synchronized void closeAllConnections() {
		while (!availableConnections.isEmpty()) {
			Connection connection = availableConnections.poll();

			try {
				if (connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		createdConnections = 0;
	}
}
