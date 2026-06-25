
package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.CancellationReportRow;
import common.ParkUsageReportRow;
import common.VisitDurationReportRow;
import common.VisitorReportRow;

/**
 * Handles database operations related to report data.
 * 
 * This connector retrieves different report types from the database, including
 * visitor reports, cancellation reports, visit duration reports, and park usage
 * reports. Each method converts the query results into report row objects that
 * can be used by the client side.
 */
public class ReportConnection extends AbstractDBConnection {

	/**
	 * The single instance of ReportConnection.
	 */
	private static ReportConnection instance;

	/**
	 * Creates a new ReportConnection instance.
	 * 
	 * The constructor is private because this class is implemented as a singleton.
	 * 
	 * @throws SQLException if connecting to the database fails
	 */
	private ReportConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of ReportConnection.
	 * 
	 * If no instance exists, or if the current database connection is closed, a new
	 * instance is created.
	 * 
	 * @return the active ReportConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static ReportConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new ReportConnection();
		}

		return instance;
	}

	/**
	 * Returns the table name used by this connector.
	 * 
	 * This connector works with several report queries and views, so it does not
	 * represent one specific database table.
	 * 
	 * @return an empty string because this connector is not bound to one table
	 */
	@Override
	public String getTableName() {
		return "";
	}

	/**
	 * Retrieves the visitor report for a specific park and month.
	 * 
	 * The report groups visits by visitor type and includes the number of visits
	 * and the total number of visitors for each type.
	 * 
	 * @param parkId the park ID to filter by
	 * @param month the month to filter by
	 * @param year the year to filter by
	 * @return a list of visitor report rows
	 * @throws SQLException if the select query fails
	 */
	public List<VisitorReportRow> getVisitorReport(int parkId, int month, int year)
			throws SQLException {

		ensureConnection();

		String sql = """
				SELECT
				    p.park_id,
				    p.park_name,
				    CASE
				        WHEN v.visit_type = 'unplanned' THEN 'Unplanned'
				        WHEN o.order_type = 'organized_group' THEN 'Organized Group'
				        ELSE 'Private'
				    END AS visitor_type,
				    COUNT(v.visit_id) AS number_of_visits,
				    COALESCE(SUM(v.actual_number_of_visitors), 0) AS total_visitors
				FROM visit v
				JOIN park p
				    ON p.park_id = v.park_id
				LEFT JOIN `order` o
				    ON o.order_number = v.order_number
				WHERE v.park_id = ?
				  AND MONTH(v.entry_time) = ?
				  AND YEAR(v.entry_time) = ?
				GROUP BY
				    p.park_id,
				    p.park_name,
				    CASE
				        WHEN v.visit_type = 'unplanned' THEN 'Unplanned'
				        WHEN o.order_type = 'organized_group' THEN 'Organized Group'
				        ELSE 'Private'
				    END
				ORDER BY visitor_type;
				""";

		List<VisitorReportRow> rows = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);
			pstmt.setInt(2, month);
			pstmt.setInt(3, year);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					rows.add(new VisitorReportRow(
							rs.getInt("park_id"),
							rs.getString("park_name"),
							rs.getString("visitor_type"),
							rs.getInt("number_of_visits"),
							rs.getInt("total_visitors")
					));
				}
			}
		}

		return rows;
	}

	/**
	 * Retrieves the cancellation report for a specific park and month.
	 * 
	 * The report includes cancelled, expired, and no-show orders, grouped by status
	 * and change reason.
	 * 
	 * @param parkId the park ID to filter by
	 * @param month the month to filter by
	 * @param year the year to filter by
	 * @return a list of cancellation report rows
	 * @throws SQLException if the select query fails
	 */
	public List<CancellationReportRow> getCancellationReport(int parkId, int month, int year)
			throws SQLException {

		ensureConnection();

		String sql = """
				SELECT
				    park_id,
				    park_name,
				    new_status,
				    change_reason,
				    COUNT(*) AS total_cancellations,
				    ROUND(AVG(DATEDIFF(order_date, DATE(changed_at))), 2)
				        AS average_days_before_visit
				FROM cancellation_report
				WHERE park_id = ?
				  AND MONTH(changed_at) = ?
				  AND YEAR(changed_at) = ?
				  AND new_status IN ('cancelled', 'expired', 'no_show')
				GROUP BY
				    park_id,
				    park_name,
				    new_status,
				    change_reason
				ORDER BY new_status;
				""";

		List<CancellationReportRow> rows = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);
			pstmt.setInt(2, month);
			pstmt.setInt(3, year);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					rows.add(new CancellationReportRow(
							rs.getInt("park_id"),
							rs.getString("park_name"),
							rs.getString("new_status"),
							rs.getString("change_reason"),
							rs.getInt("total_cancellations"),
							rs.getDouble("average_days_before_visit")
					));
				}
			}
		}

		return rows;
	}

	/**
	 * Retrieves the visit duration report for a specific park and month.
	 * 
	 * The report groups completed visits by visitor type and calculates the average
	 * visit duration in minutes.
	 * 
	 * @param parkId the park ID to filter by
	 * @param month the month to filter by
	 * @param year the year to filter by
	 * @return a list of visit duration report rows
	 * @throws SQLException if the select query fails
	 */
	public List<VisitDurationReportRow> getVisitDurationReport(int parkId, int month, int year)
			throws SQLException {

		ensureConnection();

		String sql = """
				SELECT
				    p.park_name,
				    CASE
				        WHEN v.visit_type = 'unplanned' THEN 'Unplanned'
				        WHEN o.order_type = 'organized_group' THEN 'Organized Group'
				        ELSE 'Private'
				    END AS visitor_type,
				    COUNT(v.visit_id) AS number_of_visits,
				    ROUND(AVG(TIMESTAMPDIFF(MINUTE, v.entry_time, v.exit_time)), 2)
				        AS average_duration_minutes
				FROM visit v
				JOIN park p
				    ON p.park_id = v.park_id
				LEFT JOIN `order` o
				    ON o.order_number = v.order_number
				WHERE v.park_id = ?
				  AND MONTH(v.entry_time) = ?
				  AND YEAR(v.entry_time) = ?
				  AND v.exit_time IS NOT NULL
				GROUP BY
				    p.park_name,
				    CASE
				        WHEN v.visit_type = 'unplanned' THEN 'Unplanned'
				        WHEN o.order_type = 'organized_group' THEN 'Organized Group'
				        ELSE 'Private'
				    END
				ORDER BY visitor_type;
				""";

		List<VisitDurationReportRow> rows = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);
			pstmt.setInt(2, month);
			pstmt.setInt(3, year);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					rows.add(new VisitDurationReportRow(
							rs.getString("park_name"),
							rs.getString("visitor_type"),
							rs.getInt("number_of_visits"),
							rs.getDouble("average_duration_minutes")
					));
				}
			}
		}

		return rows;
	}

	/**
	 * Retrieves the park usage report for a specific park and month.
	 * 
	 * The report includes the number of visits, the average occupancy percentage,
	 * and the maximum occupancy percentage during the selected period.
	 * 
	 * @param parkId the park ID to filter by
	 * @param month the month to filter by
	 * @param year the year to filter by
	 * @return a list of park usage report rows
	 * @throws SQLException if the select query fails
	 */
	public List<ParkUsageReportRow> getParkUsageReport(int parkId, int month, int year)
			throws SQLException {

		ensureConnection();

		String sql = """
				SELECT
				    park_name,
				    COUNT(visit_id) AS number_of_visits,
				    ROUND(AVG(occupancy_percent), 2) AS average_occupancy_percent,
				    ROUND(MAX(occupancy_percent), 2) AS max_occupancy_percent
				FROM park_usage_report
				WHERE park_id = ?
				  AND MONTH(entry_time) = ?
				  AND YEAR(entry_time) = ?
				GROUP BY park_name;
				""";

		List<ParkUsageReportRow> rows = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);
			pstmt.setInt(2, month);
			pstmt.setInt(3, year);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					rows.add(new ParkUsageReportRow(
							rs.getString("park_name"),
							rs.getInt("number_of_visits"),
							rs.getDouble("average_occupancy_percent"),
							rs.getDouble("max_occupancy_percent")
					));
				}
			}
		}

		return rows;
	}
}
