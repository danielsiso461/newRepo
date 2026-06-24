package common;

import java.io.Serializable;
/**
 * this class represents a subscriber
 */
public class Subscriber implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
	 * the subscriber's id
	 */
    private int subscriberId;
    /**
	 * the subscriber's name
	 */
    private String subscriberName;
    /**
	 * the subscriber's email
	 */
    private String subscriberEmail;
    
    /**
     * constructor that makes a subscriber object
     * @param subscriberId the id
     * @param subscriberName the name
     * @param subscriberEmail the email
     */
    public Subscriber(int subscriberId, String subscriberName, String subscriberEmail) {
        this.subscriberId = subscriberId;
        this.subscriberName = subscriberName;
        this.subscriberEmail = subscriberEmail;
    }
    
    /**
     * getter for the subscriber's id
     * @return the subscriber's id
     */
    public int getSubscriberId() {
        return subscriberId;
    }
    
    /**
     * getter for the subscriber's name
     * @return the subscriber's name
     */
    public String getSubscriberName() {
        return subscriberName;
    }

    /**
     * getter for the subscriber's email
     * @return the email
     */
    public String getSubscriberEmail() {
        return subscriberEmail;
    }
    
    /**
     * standard toString method
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
