package common;

import java.io.Serializable;

/*
 * This class represents a request to register a new subscriber.
 * 
 * The request is sent from the client to the server when a service representative
 * registers a new family subscriber.
 */
public class RegisterSubscriberRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String firstName;
	private String lastName;
	private String idNumber;
	private String username;
	private String password;
	private String phone;
	private String email;
	private int familyMembersCount;
	private String paymentMethod;
	private String creditCardLast4;

	public RegisterSubscriberRequest(String firstName, String lastName, String idNumber,
			String username, String password, String phone, String email,
			int familyMembersCount, String paymentMethod, String creditCardLast4) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.idNumber = idNumber;
		this.username = username;
		this.password = password;
		this.phone = phone;
		this.email = email;
		this.familyMembersCount = familyMembersCount;
		this.paymentMethod = paymentMethod;
		this.creditCardLast4 = creditCardLast4;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getIdNumber() {
		return idNumber;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
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
