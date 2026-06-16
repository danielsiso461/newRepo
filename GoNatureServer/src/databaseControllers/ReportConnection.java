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
 * DB connector for report data.
 */
public class ReportConnection extends AbstractDBConnection {

    private static ReportConnection instance;

    private ReportConnection() throws SQLException {
        connect();
    }

    public static ReportConnection getInstance() throws SQLException {
        if (instance == null || instance.conn == null || instance.conn.isClosed()) {
            instance = new ReportConnection();
        }

        return instance;
    }

    /**
     * ReportConnection does not represent one specific table.
     */
    @Override
    protected String getTableName() {
        return "";
    }

    /**
     * Returns visitor report data by visitor type.
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
     * Returns cancellation report data by cancellation status.
     */
    public List<CancellationReportRow> getCancellationReport(int parkId, int month, int year)
            throws SQLException {

        ensureConnection();

        String sql = """
                SELECT
                    park_id,
                    park_name,
                    new_status,
                    'All reasons' AS change_reason,
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
                    new_status
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
     * Returns average visit duration by visitor type.
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
     * Returns park usage report data.
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