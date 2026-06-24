package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is the DB connector used when working with report views.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for reports during runtime.
 * 
 * This class does not represent one physical table. Instead, it reads data from
 * different database views that were created for reports, such as visitor
 * reports, visit duration reports, cancellation reports, price calculation
 * reports, and notification reports.
 */
public class ReportConnection extends AbstractDBConnection {

	/**
	 * The single instance of ReportConnection.
	 */
	private static ReportConnection instance;

	/**
	 * Private constructor for Singleton.
	 * 
	 * It creates the database connection once.
	 * 
	 * @throws SQLException if the connection to the database fails
	 */
	private ReportConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of ReportConnection.
	 * 
	 * If no instance exists, or if the existing database connection is closed, a new
	 * instance is created.
	 * 
	 * @return the only ReportConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static ReportConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new ReportConnection();
		}
		return instance;
	}

	/**
	 * Returns the table name used by this DB connector.
	 * 
	 * This class works with several report views and not with one specific table,
	 * so an empty string is returned.
	 * 
	 * @return an empty string because this connector does not represent one table
	 */
	@Override
	protected String getTableName() {
		return "";
	}

	/**
	 * This method returns the visitor report by visitor/order type.
	 * 
	 * The report is based on the visitor_report_by_type view and includes summary
	 * information such as park ID, park name, order type, number of visits, and total
	 * number of visitors.
	 * 
	 * @return a ResultSet containing the visitor report by type
	 * @throws SQLException if the select query fails
	 */
	public ResultSet getVisitorReportByType() throws SQLException {
		String sql = "SELECT * FROM visitor_report_by_type;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		return pstmt.executeQuery();
	}

	/**
	 * This method returns the visit duration report.
	 * 
	 * The report is based on the visit_duration_report view and includes visit
	 * information such as order number, park name, subscriber name, entry time, exit
	 * time, and duration in minutes.
	 * 
	 * @return a ResultSet containing the visit duration report
	 * @throws SQLException if the select query fails
	 */
	public ResultSet getVisitDurationReport() throws SQLException {
		String sql = "SELECT * FROM visit_duration_report;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		return pstmt.executeQuery();
	}

	/**
	 * This method returns the cancellation report.
	 * 
	 * The report is based on the cancellation_report view and includes orders whose
	 * status changed to cancelled, expired, or no_show.
	 * 
	 * @return a ResultSet containing the cancellation report
	 * @throws SQLException if the select query fails
	 */
	public ResultSet getCancellationReport() throws SQLException {
		String sql = "SELECT * FROM cancellation_report;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		return pstmt.executeQuery();
	}

	/**
	 * This method returns the visit price calculation report.
	 * 
	 * The report is based on the visit_price_calculation view and shows how the
	 * final price of each visit is calculated according to the pricing model,
	 * including full price, number of paid visitors, discounts, and final price.
	 * 
	 * @return a ResultSet containing the visit price calculation report
	 * @throws SQLException if the select query fails
	 */
	public ResultSet getVisitPriceCalculation() throws SQLException {
		String sql = "SELECT * FROM visit_price_calculation;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		return pstmt.executeQuery();
	}

	/**
	 * This method returns the notification report.
	 * 
	 * The report is based on the notification_report view and includes simulated
	 * messages prepared by the system, such as order confirmations, reminders,
	 * cancellations, and waiting list offers.
	 * 
	 * @return a ResultSet containing the notification report
	 * @throws SQLException if the select query fails
	 */
	public ResultSet getNotificationReport() throws SQLException {
		String sql = "SELECT * FROM notification_report;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		return pstmt.executeQuery();
	}
}