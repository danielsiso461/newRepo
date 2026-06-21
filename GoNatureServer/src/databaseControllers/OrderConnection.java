package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.Message;
import common.Order;
import common.UpdateMessage;

/**
 * Database connector for the order table.
 */
public final class OrderConnection extends AbstractDBConnection {

    private static final OrderConnection INSTANCE = new OrderConnection();

    private final String ORDER_NUMBER = "order_number";
    private final String ORDER_DATE = "order_date";
    private final String VISITOR_NUMBER = "number_of_visitors";
    private final String CONF_CODE = "confirmation_code";
    private final String CUSTOMER_ID = "customer_id";
    private final String SUBSCRIBER_ID = "subscriber_id";
    private final String PLACEMENT_DATE = "date_of_placing_order";
    private final String ORDER_HOUR = "order_hour";
    private final String ORDER_EMAIL = "email";
    private final String PARK_ID = "park_id";
    private final String GUIDE_ID = "guide_id";
    private final String ORDER_STATUS = "order_status";
    private final String ORDER_TYPE = "order_type";

    private final int CONF_CODE_OFFSET = 100000;

    private OrderConnection() {
        super();

        try {
            connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static OrderConnection getInstance() {
        return INSTANCE;
    }

    @Override
    public String getTableName() {
        return ConstantsDBTableNames.ORDER;
    }

    private Order convertResultSetToOrderRow(int index, ResultSet rs) throws SQLException {
        Integer guideId = rs.getObject(GUIDE_ID) == null ? null : rs.getInt(GUIDE_ID);

        return new Order(
                index,
                rs.getInt(ORDER_NUMBER),
                rs.getDate(ORDER_DATE).toLocalDate(),
                rs.getInt(VISITOR_NUMBER),
                rs.getInt(CONF_CODE),
                rs.getInt(CUSTOMER_ID),
                rs.getDate(PLACEMENT_DATE).toLocalDate(),
                rs.getInt(PARK_ID),
                guideId,
                rs.getString(ORDER_STATUS),
                rs.getString(ORDER_TYPE)
        );
    }

    public void updateOrder(UpdateMessage um) throws SQLException {
        ensureConnection();

        List<Object> newValues = new ArrayList<>();
        List<Object> keyValues = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        List<String> keyColumns = new ArrayList<>();

        if (um.getUpdateDate() != null) {
            columnNames.add(ORDER_DATE);
            newValues.add(java.sql.Date.valueOf(um.getUpdateDate()));
        }

        if (um.getNumberOfVisitors() > 0) {
            columnNames.add(VISITOR_NUMBER);
            newValues.add(um.getNumberOfVisitors());
        }

        if (columnNames.isEmpty()) {
            throw new SQLException("No order fields were selected for update.");
        }

        keyColumns.add(ORDER_NUMBER);
        keyValues.add(um.getOrderId());

        boolean updated = updateFields(
                columnNames.toArray(new String[0]),
                newValues,
                keyColumns.toArray(new String[0]),
                keyValues
        );

        if (!updated) {
            throw new SQLException("Failed to update order.");
        }
    }

    public List<Order> getUserOrders(Message m) throws SQLException {
        ensureConnection();

        String sql = selectByFields(new String[] { "*" }, new String[] { CUSTOMER_ID });

        List<Order> orders = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt((String) m.getData()));

            try (ResultSet rs = pstmt.executeQuery()) {
                int index = 1;

                while (rs.next()) {
                    orders.add(convertResultSetToOrderRow(index++, rs));
                }
            }
        }

        return orders;
    }

    public int createOrder(java.time.LocalDate orderDate, int numberOfVisitors, int confirmationCode,
            int subscriberId, int parkId, Integer guideId, String orderType) throws SQLException {

        ensureConnection();

        List<Object> values = new ArrayList<>();

        values.add(java.sql.Date.valueOf(orderDate));
        values.add(numberOfVisitors);
        values.add(confirmationCode);
        values.add(subscriberId);
        values.add(parkId);
        values.add(guideId);
        values.add(java.sql.Date.valueOf(java.time.LocalDate.now()));
        values.add("pending");
        values.add(orderType);

        return insertFieldsAndReturnGeneratedKey(
                new String[] {
                        ORDER_DATE,
                        VISITOR_NUMBER,
                        CONF_CODE,
                        SUBSCRIBER_ID,
                        PARK_ID,
                        GUIDE_ID,
                        PLACEMENT_DATE,
                        ORDER_STATUS,
                        ORDER_TYPE
                },
                values
        );
    }

    public Order getOrderByNumber(int orderNumber) throws SQLException {
        ensureConnection();

        String sql = selectByFields(new String[] { "*" }, new String[] { ORDER_NUMBER });

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return convertResultSetToOrderRow(1, rs);
                }
            }
        }

        return null;
    }

    public List<Order> getOrdersByPark(int parkId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(new String[] { "*" }, new String[] { PARK_ID });

        List<Order> orders = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parkId);

            try (ResultSet rs = pstmt.executeQuery()) {
                int index = 1;

                while (rs.next()) {
                    orders.add(convertResultSetToOrderRow(index++, rs));
                }
            }
        }

        return orders;
    }

    public List<Order> getOrdersByStatus(String orderStatus) throws SQLException {
        ensureConnection();

        String sql = selectByFields(new String[] { "*" }, new String[] { ORDER_STATUS });

        List<Order> orders = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderStatus);

            try (ResultSet rs = pstmt.executeQuery()) {
                int index = 1;

                while (rs.next()) {
                    orders.add(convertResultSetToOrderRow(index++, rs));
                }
            }
        }

        return orders;
    }

    public List<Order> getOrdersByType(String orderType) throws SQLException {
        ensureConnection();

        String sql = selectByFields(new String[] { "*" }, new String[] { ORDER_TYPE });

        List<Order> orders = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderType);

            try (ResultSet rs = pstmt.executeQuery()) {
                int index = 1;

                while (rs.next()) {
                    orders.add(convertResultSetToOrderRow(index++, rs));
                }
            }
        }

        return orders;
    }

    public boolean orderExists(int orderNumber) throws SQLException {
        ensureConnection();

        String sql = selectByFields(new String[] { ORDER_NUMBER }, new String[] { ORDER_NUMBER });

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean updateOrderStatus(int orderNumber, String newStatus,
            int changedByEmployeeId, String reason) throws SQLException {

        ensureConnection();

        String oldStatusSql = selectByFields(
                new String[] { ORDER_STATUS },
                new String[] { ORDER_NUMBER }
        );

        String oldStatus;

        try (PreparedStatement oldStatusStmt = conn.prepareStatement(oldStatusSql)) {
            oldStatusStmt.setInt(1, orderNumber);

            try (ResultSet rs = oldStatusStmt.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                oldStatus = rs.getString(ORDER_STATUS);
            }
        }

        List<Object> newValues = new ArrayList<>();
        List<Object> keyValues = new ArrayList<>();

        newValues.add(newStatus);
        keyValues.add(orderNumber);

        boolean updated = updateFields(
                new String[] { ORDER_STATUS },
                newValues,
                new String[] { ORDER_NUMBER },
                keyValues
        );

        if (!updated) {
            return false;
        }

        OrderStatusHistoryConnection.getInstance().addHistory(
                orderNumber,
                oldStatus,
                newStatus,
                changedByEmployeeId,
                reason
        );

        return true;
    }

    public boolean approveOrder(int orderNumber, int changedByEmployeeId) throws SQLException {
        return updateOrderStatus(orderNumber, "approved", changedByEmployeeId,
                "Order approved after availability check");
    }

    public boolean cancelOrder(int orderNumber, int changedByEmployeeId, String reason) throws SQLException {
        return updateOrderStatus(orderNumber, "cancelled", changedByEmployeeId, reason);
    }

    public boolean expireOrder(int orderNumber, int changedByEmployeeId) throws SQLException {
        return updateOrderStatus(orderNumber, "expired", changedByEmployeeId,
                "Order expired because the visitor did not confirm in time");
    }

    public boolean completeOrder(int orderNumber, int changedByEmployeeId) throws SQLException {
        return updateOrderStatus(orderNumber, "completed", changedByEmployeeId,
                "Order completed after visit");
    }

    public boolean markOrderAsNoShow(int orderNumber, int changedByEmployeeId) throws SQLException {
        return updateOrderStatus(orderNumber, "no_show", changedByEmployeeId,
                "Visitor did not arrive to the park");
    }

    public void updateOrderPark(int orderNumber, int parkId) throws SQLException {
        ensureConnection();

        List<Object> newValues = new ArrayList<>();
        List<Object> keyValues = new ArrayList<>();

        newValues.add(parkId);
        keyValues.add(orderNumber);

        boolean updated = updateFields(
                new String[] { PARK_ID },
                newValues,
                new String[] { ORDER_NUMBER },
                keyValues
        );

        if (!updated) {
            throw new SQLException("Failed to update order park.");
        }
    }

    public void updateOrderGuide(int orderNumber, Integer guideId) throws SQLException {
        ensureConnection();

        List<Object> newValues = new ArrayList<>();
        List<Object> keyValues = new ArrayList<>();

        newValues.add(guideId);
        keyValues.add(orderNumber);

        boolean updated = updateFields(
                new String[] { GUIDE_ID },
                newValues,
                new String[] { ORDER_NUMBER },
                keyValues
        );

        if (!updated) {
            throw new SQLException("Failed to update order guide.");
        }
    }

    public void updateOrderType(int orderNumber, String orderType) throws SQLException {
        ensureConnection();

        List<Object> newValues = new ArrayList<>();
        List<Object> keyValues = new ArrayList<>();

        newValues.add(orderType);
        keyValues.add(orderNumber);

        boolean updated = updateFields(
                new String[] { ORDER_TYPE },
                newValues,
                new String[] { ORDER_NUMBER },
                keyValues
        );

        if (!updated) {
            throw new SQLException("Failed to update order type.");
        }
    }

    public List<Order> getApprovedOrdersByParkAndDate(int parkId, java.time.LocalDate orderDate)
            throws SQLException {

        ensureConnection();

        String sql = """
                SELECT *
                FROM `order`
                WHERE park_id = ?
                  AND order_date = ?
                  AND order_status = 'approved'
                ORDER BY order_number;
                """;

        List<Order> orders = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parkId);
            pstmt.setDate(2, java.sql.Date.valueOf(orderDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                int index = 1;

                while (rs.next()) {
                    orders.add(convertResultSetToOrderRow(index++, rs));
                }
            }
        }

        return orders;
    }

    public int getTotalApprovedVisitorsByParkAndDate(int parkId, java.time.LocalDate orderDate)
            throws SQLException {

        ensureConnection();

        String sql = """
                SELECT COALESCE(SUM(number_of_visitors), 0) AS total_visitors
                FROM `order`
                WHERE park_id = ?
                  AND order_date = ?
                  AND order_status = 'approved';
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parkId);
            pstmt.setDate(2, java.sql.Date.valueOf(orderDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_visitors");
                }
            }
        }

        return 0;
    }

    public Order bookOrder(Order o) throws SQLException {
        ensureConnection();

        List<String> columnNames = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        columnNames.add(ORDER_DATE);
        values.add(java.sql.Date.valueOf(o.getOrderDate()));

        columnNames.add(VISITOR_NUMBER);
        values.add(o.getVisitorNumber());

        columnNames.add(CUSTOMER_ID);
        values.add(o.getUserId());

        columnNames.add(PLACEMENT_DATE);
        values.add(java.sql.Date.valueOf(o.getPlacementDate()));

        columnNames.add(PARK_ID);
        values.add(o.getParkId());

        columnNames.add(GUIDE_ID);
        values.add(o.getGuideId());

        columnNames.add(ORDER_STATUS);
        values.add(o.getOrderStatus());

        columnNames.add(ORDER_TYPE);
        values.add(o.getOrderType());

        columnNames.add(ORDER_HOUR);
        values.add(o.getOrderHour());

        columnNames.add(ORDER_EMAIL);
        values.add(o.getEmail());

        if (o.getIsSubscribed()) {
            columnNames.add(SUBSCRIBER_ID);
            values.add(o.getUserId());
        }

        int generatedOrderId = insertFieldsAndReturnGeneratedKey(
                columnNames.toArray(new String[0]),
                values
        );

        if (generatedOrderId == -1) {
            throw new SQLException("Failed to insert order.");
        }

        o.setOrderId(generatedOrderId);

        int code = o.getOrderId() % CONF_CODE_OFFSET + CONF_CODE_OFFSET;
        o.setConfirmationCode(code);

        List<Object> newValues = new ArrayList<>();
        List<Object> keyValues = new ArrayList<>();

        newValues.add(code);
        keyValues.add(o.getOrderId());

        boolean updated = updateFields(
                new String[] { CONF_CODE },
                newValues,
                new String[] { ORDER_NUMBER },
                keyValues
        );

        if (!updated) {
            throw new SQLException("Failed to update order confirmation code.");
        }

        return o;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}