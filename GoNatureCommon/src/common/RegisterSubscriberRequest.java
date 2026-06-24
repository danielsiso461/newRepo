package common;

import java.io.Serializable;

/**
 * This class represents a request to register a new subscriber.
 * 
 * The request is sent from the client to the server when a service representative
 * registers a new family subscriber.
 */
public class RegisterSubscriberRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * the first name of the subscriber
	 */
	private String firstName;
	/**
	 * the last name of the subscriber
	 */
	private String lastName;
	/**
	 * the id of the subscriber
	 */
	private String idNumber;
	/**
	 * the username of the subscriber
	 */
	private String username;
	/**
	 * the password of the subscriber
	 */
	private String password;
	/**
	 * the phone number of the subscriber
	 */
	private String phone;
	/**
	 * the email of the subscriber
	 */
	private String email;
	/**
	 * the family member count of the subscriber
	 */
	private int familyMembersCount;
	/**
	 * the payment method of the subscriber
	 */
	private String paymentMethod;
	/**
	 *  the subscriber's credit card last 4 digits
	 */
	private String creditCardLast4;
	
	/**
	 * constructor to create a subscriber registration request
	 * @param firstName the first name of the subscriber
	 * @param lastName the last name of the subscriber
	 * @param idNumber the id of the subscriber
	 * @param username the username of the subscriber
	 * @param password the password of the subscriber
	 * @param phone the phone number of the subscriber
	 * @param email the email of the subscriber
	 * @param familyMembersCount the number of family members of the subscriber
	 * @param paymentMethod the payment method chosen by the subscriber
	 * @param creditCardLast4 the last 4 digits of the subscriber's credit card
	 */
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
	/**
	 * getter for the subscriber's first name
	 * @return the subscriber's first name 
	 */
	public String getFirstName() {
		return firstName;
	}
	/**
	 * getter for the subscriber's last name
	 * @return the subscriber's first name 
	 */
	public String getLastName() {
		return lastName;
	}
	/**
	 * getter for the subscriber's id
	 * @return the subscriber's id
	 */
	public String getIdNumber() {
		return idNumber;
	}
	/**
	 * getter for the subscriber's username
	 * @return the subscriber's username 
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * getter for the subscriber's password
	 * @return the subscriber's password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * getter for the subscriber's phone
	 * @return the subscriber's phone 
	 */
	public String getPhone() {
		return phone;
	}
	/**
	 * getter for the subscriber's email
	 * @return the subscriber's email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * getter for the subscriber's family member count
	 * @return the subscriber's family member count
	 */
	public int getFamilyMembersCount() {
		return familyMembersCount;
	}
	/**
	 * getter for the subscriber's payment method
	 * @return the subscriber's payment method
	 */
	public String getPaymentMethod() {
		return paymentMethod;
	}
	/**
	 * getter for the subscriber's credit card's last 4 digits 
	 * @return the subscriber's credit card's last 4 digits 
	 */
	public String getCreditCardLast4() {
		return creditCardLast4;
	}
}
