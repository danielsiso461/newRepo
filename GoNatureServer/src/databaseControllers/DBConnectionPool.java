package databaseControllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class manages a pool of database connections.
 * 
 * Instead of opening a new database connection every time a connector needs to
 * work with the database, this class creates and reuses a limited number of
 * connections.
 * 
 * The connection pool improves performance and prevents creating too many open
 * connections to the database.
 */
public class DBConnectionPool {
	/**
	 * the connection pool instance
	 */
	private static DBConnectionPool instance;
	/**
	 * the DB's url
	 */
	private static final String URL = "jdbc:mysql://localhost:3306/gonature?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";
	/**
	 * the DB's username
	 */
	private static final String USER = "root";
	/**
	 * the connection pool's initial size
	 */
	private static final int INITIAL_POOL_SIZE = 3;
	/**
	 * the connection pool's max size
	 */
	private static final int MAX_POOL_SIZE = 10;
	/**
	 * the DB's password
	 */
	private String password;
	/**
	 * the queue of available connections
	 */
	private Queue<Connection> availableConnections;
	/**
	 * the number of created connections
	 */
	private int createdConnections;

	/**
	 * Private constructor for Singleton.
	 * 
	 * The pool itself is created only once during runtime.
	 */
	private DBConnectionPool() {
		availableConnections = new LinkedList<>();
		createdConnections = 0;
	}

	/**
	 * Returns the single instance of DBConnectionPool.
	 * 
	 * @return the single DBConnectionPool instance
	 */
	public static DBConnectionPool getInstance() {
		if (instance == null) {
			instance = new DBConnectionPool();
		}

		return instance;
	}

	/**
	 * Saves the database password that was entered by the user.
	 * 
	 * @param dbPassword the database password
	 */
	public void setPassword(String dbPassword) {
		this.password = dbPassword;
	}

	/**
	 * Checks whether the entered database password is correct.
	 * 
	 * This method opens a temporary connection only for testing and closes it
	 * immediately afterwards.
	 * 
	 * @param dbPassword the database password entered by the user
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
	 * Initializes the connection pool.
	 * 
	 * This method creates a few database connections in advance and stores them in
	 * the available connections queue.
	 * 
	 * @throws SQLException if creating a database connection fails
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
	 * Creates a new database connection.
	 * 
	 * @return a new database connection
	 * @throws SQLException if creating the connection fails
	 */
	private Connection createNewConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, password);
	}

	/**
	 * Gives a database connection from the pool.
	 * 
	 * If the pool has available connections, one of them is returned.
	 * If there are no available connections and the maximum pool size was not
	 * reached, a new connection is created.
	 * If the maximum pool size was reached, the method waits until another
	 * connection is released back to the pool.
	 * 
	 * @return an available database connection
	 * @throws SQLException if getting a connection fails
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
	 * Returns a database connection back to the pool.
	 * 
	 * The connection is not closed immediately. It is saved for reuse by another
	 * database operation.
	 * 
	 * @param connection the connection to return to the pool
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
	 * Closes all available connections in the pool.
	 * 
	 * This method should be called when the server is closed.
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