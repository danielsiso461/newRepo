package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.Park;

/**
 * DB connector for the park table.
 */
public class ParkConnection extends AbstractDBConnection {

    private final String PARK_ID = "park_id";
    private final String PARK_NAME = "park_name";
    private final String MAX_CAPACITY = "max_capacity";
    private final String PLACES_FOR_UNPLANNED_VISITORS = "places_for_unplanned_visitors";
    private final String ESTIMATED_VISIT_DURATION_HOURS = "estimated_visit_duration_hours";
    private final String FULL_ENTRY_PRICE = "full_entry_price";
    private final String IS_ACTIVE = "is_active";
    private final String PROMOTIONS = "promotions";

    private final int ACTIVE_TRUE = 1;

    private static ParkConnection instance;

    private ParkConnection() throws SQLException {
        connect();
    }

    public static ParkConnection getInstance() throws SQLException {
        if (instance == null || instance.conn == null || instance.conn.isClosed()) {
            instance = new ParkConnection();
        }

        return instance;
    }

    @Override
    protected String getTableName() {
        return ConstantsDBTableNames.PARK;
    }

    /**
     * Converts a database row into a Park object.
     */
    private Park convertResultSetToPark(ResultSet rs) throws SQLException {
        return new Park(
                rs.getInt(PARK_ID),
                rs.getString(PARK_NAME),
                rs.getInt(MAX_CAPACITY),
                rs.getInt(PLACES_FOR_UNPLANNED_VISITORS),
                rs.getDouble(ESTIMATED_VISIT_DURATION_HOURS),
                rs.getDouble(FULL_ENTRY_PRICE),
                rs.getInt(IS_ACTIVE) == 1,
                convertPromotionToBoolean(rs.getString(PROMOTIONS))
        );
    }

    /**
     * Converts the promotions column from varchar to boolean.
     * 
     * Empty, null, "0", "false", "no", and "none" are treated as false.
     * Any other value means that the park has some promotion.
     */
    private boolean convertPromotionToBoolean(String promotionValue) {
        if (promotionValue == null) {
            return false;
        }

        String value = promotionValue.trim().toLowerCase();

        if (value.isEmpty()) {
            return false;
        }

        return !value.equals("0")
                && !value.equals("false")
                && !value.equals("no")
                && !value.equals("none");
    }

    /**
     * Returns all active parks.
     */
    public List<Park> getAllActiveParks() throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { "*" },
                new String[] { IS_ACTIVE }
        );

        List<Park> parks = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ACTIVE_TRUE);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    parks.add(convertResultSetToPark(rs));
                }
            }
        }

        return parks;
    }

    /**
     * Returns all parks.
     */
    public List<Park> getAllFullParks() throws SQLException {
        ensureConnection();

        String sql = "SELECT * FROM `" + getTableName() + "`;";

        List<Park> parks = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                parks.add(convertResultSetToPark(rs));
            }
        }

        return parks;
    }

    /**
     * Returns a park by id.
     */
    public Park getFullParkById(int parkId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { "*" },
                new String[] { PARK_ID }
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parkId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return convertResultSetToPark(rs);
                }
            }
        }

        return null;
    }

    /**
     * Returns active parks for client display.
     */
    public List<Park> getAllActiveParksInfo() throws SQLException {
        return getAllActiveParks();
    }

    /**
     * Returns a park for client display.
     */
    public Park getParkById(int parkId) throws SQLException {
        return getFullParkById(parkId);
    }

    /**
     * Returns the full entry price of a park.
     */
    public double getFullEntryPrice(int parkId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { FULL_ENTRY_PRICE },
                new String[] { PARK_ID }
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parkId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(FULL_ENTRY_PRICE);
                }
            }
        }

        return -1;
    }

    /**
     * Checks whether a park has a promotion.
     */
    public boolean hasPromotion(int parkId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { PROMOTIONS },
                new String[] { PARK_ID }
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parkId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return convertPromotionToBoolean(rs.getString(PROMOTIONS));
                }
            }
        }

        return false;
    }

    /**
     * Checks available capacity for visitors entering now.
     */
    public boolean hasAvailableCapacity(int parkId, int requestedVisitors) throws SQLException {
        ensureConnection();

        String sql = """
                SELECT
                    p.max_capacity,
                    COALESCE(SUM(v.actual_number_of_visitors), 0) AS current_visitors
                FROM park p
                LEFT JOIN visit v
                    ON p.park_id = v.park_id
                    AND v.exit_time IS NULL
                WHERE p.park_id = ?
                GROUP BY p.max_capacity;
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parkId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                int maxCapacity = rs.getInt("max_capacity");
                int currentVisitors = rs.getInt("current_visitors");

                return currentVisitors + requestedVisitors <= maxCapacity;
            }
        }
    }

    /**
     * Checks available capacity for future orders.
     */
    public boolean hasAvailableOrderCapacity(int parkId, java.time.LocalDate orderDate,
            int requestedVisitors) throws SQLException {

        ensureConnection();

        String sql = """
                SELECT
                    p.max_capacity,
                    p.places_for_unplanned_visitors,
                    COALESCE(SUM(o.number_of_visitors), 0) AS ordered_visitors
                FROM park p
                LEFT JOIN `order` o
                    ON p.park_id = o.park_id
                    AND o.order_status = 'approved'
                    AND o.order_date = ?
                WHERE p.park_id = ?
                GROUP BY p.max_capacity, p.places_for_unplanned_visitors;
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(orderDate));
            pstmt.setInt(2, parkId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                int maxCapacity = rs.getInt("max_capacity");
                int reservedForUnplanned = rs.getInt("places_for_unplanned_visitors");
                int orderedVisitors = rs.getInt("ordered_visitors");

                int allowedOrderedVisitors = maxCapacity - reservedForUnplanned;

                return orderedVisitors + requestedVisitors <= allowedOrderedVisitors;
            }
        }
    }

    /**
     * Checks whether a park is active.
     */
    public boolean isParkActive(int parkId) throws SQLException {
        Park park = getFullParkById(parkId);

        return park != null && park.isActive();
    }

    /**
     * Returns names of all active parks.
     */
    public List<String> getActiveParksNames() throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { PARK_NAME },
                new String[] { IS_ACTIVE }
        );

        List<String> activeParkNames = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ACTIVE_TRUE);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    activeParkNames.add(rs.getString(PARK_NAME));
                }
            }
        }

        return activeParkNames;
    }

    /**
     * Returns the id of a park by its name.
     */
    public int getParkIdByName(String parkName) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { PARK_ID },
                new String[] { PARK_NAME }
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, parkName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(PARK_ID);
                }
            }
        }

        return -1;
    }

    /**
     * Updates one park parameter after a department manager approves a request.
     */
    public boolean updateParkParameter(int parkId, String parameterName, String newValue)
            throws SQLException {

        ensureConnection();

        if (parameterName == null || parameterName.isBlank()
                || newValue == null || newValue.isBlank()) {
            throw new SQLException("Invalid park parameter update request.");
        }

        String columnName = getParkColumnByParameterName(parameterName);

        if (columnName == null) {
            throw new SQLException("Unknown park parameter: " + parameterName);
        }

        Object convertedValue = convertParkParameterValue(parameterName, newValue);

        return updateFields(
                new String[] { columnName },
                List.of(convertedValue),
                new String[] { PARK_ID },
                List.of(parkId)
        );
    }

    /**
     * Converts a request parameter name to a real park table column name.
     */
    private String getParkColumnByParameterName(String parameterName) {
        switch (parameterName) {

        case MAX_CAPACITY:
            return MAX_CAPACITY;

        case PLACES_FOR_UNPLANNED_VISITORS:
            return PLACES_FOR_UNPLANNED_VISITORS;

        case ESTIMATED_VISIT_DURATION_HOURS:
            return ESTIMATED_VISIT_DURATION_HOURS;

        case PROMOTIONS:
            return PROMOTIONS;

        default:
            return null;
        }
    }

    /**
     * Converts the new value from String to the correct DB value type.
     */
    private Object convertParkParameterValue(String parameterName, String newValue) {
        switch (parameterName) {

        case MAX_CAPACITY:
        case PLACES_FOR_UNPLANNED_VISITORS:
        case ESTIMATED_VISIT_DURATION_HOURS:
            return Integer.parseInt(newValue);

        case PROMOTIONS:
            return newValue;

        default:
            return newValue;
        }
    }
}