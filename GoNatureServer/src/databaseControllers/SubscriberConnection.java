package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.Subscriber;

/**
 * DB connector for the subscriber table.
 */
public class SubscriberConnection extends AbstractDBConnection {

    private static SubscriberConnection instance;

    private final String SUBSCRIBER_ID = "subscriber_id";
    private final String SUBSCRIBER_NAME = "subscriber_name";
    private final String SUBSCRIBER_ID_NUMBER = "subscriber_id_number";
    private final String PHONE_NUMBER = "subscriber_phone";
    private final String SUBSCRIBER_EMAIL = "subscriber_email";
    private final String FAMILY_MEMBERS_COUNT = "family_members_count";
    private final String PAYMENT_METHOD = "payment_method";
    private final String CREDIT_CARD_LAST4 = "credit_card_last4";

    private SubscriberConnection() throws SQLException {
        connect();
    }

    public static SubscriberConnection getInstance() throws SQLException {
        if (instance == null || instance.conn == null || instance.conn.isClosed()) {
            instance = new SubscriberConnection();
        }

        return instance;
    }

    @Override
    protected String getTableName() {
        return ConstantsDBTableNames.SUBSCRIBER;
    }

    private Subscriber convertResultSetToSubscriber(ResultSet rs) throws SQLException {
        return new Subscriber(
                rs.getInt(SUBSCRIBER_ID),
                rs.getString(SUBSCRIBER_NAME),
                rs.getString(SUBSCRIBER_EMAIL)
        );
    }

    /**
     * Returns a subscriber by id.
     */
    public Subscriber getSubscriberById(int subscriberId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { SUBSCRIBER_ID, SUBSCRIBER_NAME, SUBSCRIBER_EMAIL },
                new String[] { SUBSCRIBER_ID }
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subscriberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return convertResultSetToSubscriber(rs);
                }
            }
        }

        return null;
    }

    /**
     * Checks whether a subscriber exists.
     */
    public boolean subscriberExists(int subscriberId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { SUBSCRIBER_ID },
                new String[] { SUBSCRIBER_ID }
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subscriberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Adds a new subscriber.
     */
    public boolean addSubscriber(int subscriberId, String subscriberName, String idNumber,
            String phone, String email, int familyMembersCount,
            String paymentMethod, String creditCardLast4) throws SQLException {

        ensureConnection();

        List<Object> values = new ArrayList<>();

        values.add(subscriberId);
        values.add(subscriberName);
        values.add(idNumber);
        values.add(phone);
        values.add(email);
        values.add(familyMembersCount);
        values.add(paymentMethod);
        values.add(creditCardLast4);

        insertFields(
                new String[] {
                        SUBSCRIBER_ID,
                        SUBSCRIBER_NAME,
                        SUBSCRIBER_ID_NUMBER,
                        PHONE_NUMBER,
                        SUBSCRIBER_EMAIL,
                        FAMILY_MEMBERS_COUNT,
                        PAYMENT_METHOD,
                        CREDIT_CARD_LAST4
                },
                values
        );

        return true;
    }

    /**
     * Returns the subscriber phone number by id.
     */
    public String getPhoneNumberById(int subscriberId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { PHONE_NUMBER },
                new String[] { SUBSCRIBER_ID }
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subscriberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(PHONE_NUMBER);
                }
            }
        }

        return null;
    }
}