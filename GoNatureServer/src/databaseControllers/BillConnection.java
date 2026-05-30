package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is the DB connector used when working with the bill table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for bills during runtime.
 * 
 * The bill table stores the payment calculation for actual visits, including
 * the full price, number of paid visitors, discounts, and final price.
 */
public class BillConnection extends AbstractDBConnection {

	/**
	 * The single instance of BillConnection.
	 */
	private static BillConnection instance;

	/**
	 * Private constructor for Singleton.
	 * 
	 * It creates the database connection once.
	 * 
	 * @throws SQLException if the connection to the database fails
	 */
	private BillConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of BillConnection.
	 * 
	 * If no instance exists, or if the existing database connection is closed, a new
	 * instance is created.
	 * 
	 * @return the only BillConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static BillConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new BillConnection();
		}
		return instance;
	}

	/**
	 * Returns the table name used by this DB connector.
	 * 
	 * @return the bill table name
	 */
	@Override
	protected String getTableName() {
		return ConstantsDBTableNames.BILL;
	}

	/**
	 * This method creates a bill for an existing visit.
	 * 
	 * The bill values are calculated by selecting the matching visit from the
	 * visit_price_calculation view. The calculation includes the bill type, full
	 * price, number of paid visitors, base discount, prepaid discount, subscriber
	 * extra discount, promotion discount, final price, and bill date.
	 * 
	 * @param visitId the ID of the visit for which the bill should be created
	 * @return true if the bill was created successfully, false otherwise
	 * @throws SQLException if the insert query fails
	 */
	public boolean createBillFromVisit(int visitId) throws SQLException {
		String sql = "INSERT INTO bill "
				+ "(visit_id, bill_type, full_price, number_of_paid_visitors, discount_percent, "
				+ "base_discount_percent, prepaid_discount_percent, subscriber_extra_discount_percent, "
				+ "promotion_discount_percent, final_price, bill_date) "
				+ "SELECT visit_id, calculated_bill_type, full_price, number_of_paid_visitors, "
				+ "base_discount_percent + prepaid_discount_percent, "
				+ "base_discount_percent, prepaid_discount_percent, subscriber_extra_discount_percent, "
				+ "promotion_discount_percent, final_price, NOW() "
				+ "FROM visit_price_calculation "
				+ "WHERE visit_id = ?;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, visitId);

		return pstmt.executeUpdate() > 0;
	}

	/**
	 * This method returns the bill that belongs to a specific visit.
	 * 
	 * @param visitId the ID of the visit whose bill should be retrieved
	 * @return a ResultSet containing the bill data for the given visit
	 * @throws SQLException if the select query fails
	 */
	public ResultSet getBillByVisitId(int visitId) throws SQLException {
		String sql = "SELECT * FROM bill WHERE visit_id = ?;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, visitId);

		return pstmt.executeQuery();
	}
}