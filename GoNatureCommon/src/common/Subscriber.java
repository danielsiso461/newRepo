package common;

import java.io.Serializable;

public class Subscriber implements Serializable {

	private static final long serialVersionUID = 1L;

	private int subscriberId;
	private String subscriberName;
	private String subscriberIdNumber;
	private String subscriberPhone;
	private String subscriberEmail;
	private int familyMembersCount;
	private String paymentMethod;
	private String creditCardLast4;

	public Subscriber(int subscriberId, String subscriberName, String subscriberIdNumber, String subscriberPhone,
			String subscriberEmail, int familyMembersCount, String paymentMethod, String creditCardLast4) {

		this.subscriberId = subscriberId;
		this.subscriberName = subscriberName;
		this.subscriberIdNumber = subscriberIdNumber;
		this.subscriberPhone = subscriberPhone;
		this.subscriberEmail = subscriberEmail;
		this.familyMembersCount = familyMembersCount;
		this.paymentMethod = paymentMethod;
		this.creditCardLast4 = creditCardLast4;
	}

	public int getSubscriberId() {
		return subscriberId;
	}

	public String getSubscriberName() {
		return subscriberName;
	}

	public String getSubscriberIdNumber() {
		return subscriberIdNumber;
	}

	public String getSubscriberPhone() {
		return subscriberPhone;
	}

	public String getSubscriberEmail() {
		return subscriberEmail;
	}

	public int getFamilyMembersCount() {
		return familyMembersCount;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public String getCreditCardLast4() {
		return creditCardLast4;
	}
}