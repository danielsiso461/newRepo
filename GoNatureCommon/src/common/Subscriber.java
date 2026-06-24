package common;

import java.io.Serializable;

public class Subscriber implements Serializable {

    private static final long serialVersionUID = 1L;

    private int subscriberId;
    private String subscriberName;
    private String subscriberEmail;

    public Subscriber(int subscriberId, String subscriberName, String subscriberEmail) {
        this.subscriberId = subscriberId;
        this.subscriberName = subscriberName;
        this.subscriberEmail = subscriberEmail;
    }

    public int getSubscriberId() {
        return subscriberId;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public String getSubscriberEmail() {
        return subscriberEmail;
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "subscriberId=" + subscriberId +
                ", subscriberName='" + subscriberName + '\'' +
                ", subscriberEmail='" + subscriberEmail + '\'' +
                '}';
    }
}