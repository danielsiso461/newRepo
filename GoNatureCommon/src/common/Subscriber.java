package common;

import java.io.Serializable;

/**
 * This class represents a subscriber.
 */
public class Subscriber implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The subscriber's id.
     */
    private int subscriberId;

    /**
     * The subscriber's name.
     */
    private String subscriberName;

    /**
     * The subscriber's email.
     */
    private String subscriberEmail;

    /**
     * Constructs a subscriber object.
     * 
     * @param subscriberId the subscriber id
     * @param subscriberName the subscriber name
     * @param subscriberEmail the subscriber email
     */
    public Subscriber(int subscriberId, String subscriberName, String subscriberEmail) {
        this.subscriberId = subscriberId;
        this.subscriberName = subscriberName;
        this.subscriberEmail = subscriberEmail;
    }

    /**
     * Returns the subscriber id.
     * 
     * @return the subscriber id
     */
    public int getSubscriberId() {
        return subscriberId;
    }

    /**
     * Returns the subscriber name.
     * 
     * @return the subscriber name
     */
    public String getSubscriberName() {
        return subscriberName;
    }

    /**
     * Returns the subscriber email.
     * 
     * @return the subscriber email
     */
    public String getSubscriberEmail() {
        return subscriberEmail;
    }

    /**
     * Returns a string representation of the subscriber.
     */
    @Override
    public String toString() {
        return "Subscriber{" +
                "subscriberId=" + subscriberId +
                ", subscriberName='" + subscriberName + '\'' +
                ", subscriberEmail='" + subscriberEmail + '\'' +
                '}';
    }
}