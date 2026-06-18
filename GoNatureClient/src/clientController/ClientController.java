package clientController;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import common.ParkInfo;

import client.Client;
import common.ChatIF;
import common.Message;
import common.OrderRow;
import common.Protocol;
import common.UpdateMessage;
import common.EmployeeLoginRequest;
import common.OperationResponse;
import clientCommon.*;
import javafx.application.Platform;
import common.OperationResponse;
import common.ExistingCustomerLoginRequest;
import clientCommon.RegisterSubscriberObserver;
import common.RegisterSubscriberRequest;
/*
 * this class is the controller that connects the client networking side to the UI side
 * it is also taking care of the logic between the two components
 */
@SuppressWarnings("deprecation")
public class ClientController implements ChatIF {
	// Class variables *************************************************

	// Instance variables **********************************************
	// The instance of the client that created this ConsoleChat.
	private Client client;
	private String id = null;
	private boolean userIssuedDisconnect = false;
	
	// Observer pattern addition
		private List<OrderObserver> observers = new ArrayList<>();
	/*
	 * Observer pattern addition for park screens
	 */
	private List<ParkObserver> parkObservers = new ArrayList<>();
	/*
	 * Observer pattern addition for occasional customer access screen.
	 */
	private List<OccasionalCustomerAccessObserver> occasionalCustomerAccessObservers = new ArrayList<>();
	/*
	 * Observer pattern addition for employee login screen.
	 */
	private List<EmployeeLoginObserver> employeeLoginObservers = new ArrayList<>();
	
	/*
	 * Observer pattern addition for existing customer login screen.
	 */
	private List<ExistingCustomerLoginObserver> existingCustomerLoginObservers = new ArrayList<>();
	
	/*
	 * Observer pattern addition for register subscriber screen.
	 */
	private List<RegisterSubscriberObserver> registerSubscriberObservers = new ArrayList<>();
	

	// Constructors ****************************************************

	/**
	 * Constructs an instance of the ClientController. also get the user ID from
	 * console
	 *
	 * @param host The host to connect to.
	 * @param port The port to connect on.
	 */
	public ClientController(String host, int port, String id) throws IOException {
		try {
			client = new Client(host, port, this);
		} catch (IOException exception) {
			throw exception;
			//System.out.println("Error: Can't setup connection!" + " Terminating client.");
			//System.exit(1);
		}
		this.id = id;
	}

	/*
	 * Observer pattern addition
	 * adding observer to observer list
	 * 
	 * @param observer observer to add
	 */
	public void addObserver(OrderObserver observer) {
		if (observer != null && !observers.contains(observer)) {
			observers.add(observer);
		}
	}

	/*
	 * Observer pattern addition
	 * removing observer from observer list
	 * 
	 * @param observer observer to remove
	 */
	public void removeObserver(OrderObserver observer) {
		observers.remove(observer);
	}

	/*
	 * Observer pattern addition
	 * notifies the observers of the received orders and sent them said orders
	 * 
	 * @param rows the received orders
	 */
	private void notifyOrdersReceived(List<OrderRow> rows) {
		for (OrderObserver observer : observers) {
			observer.onOrdersReceived(rows);
		}
	}

	/*
	 * Observer pattern addition
	 * notifies the observers the update request's success
	 * 
	 * @param success true if the update succeeded, false otherwise
	 * @param updateMessage the data of the update
	 */
	private void notifyUpdateResult(boolean success, UpdateMessage updateMessage) {
		for (OrderObserver observer : observers) {
			observer.onUpdateResult(success, updateMessage);
		}
	}
	
	/*
	 * Adds an occasional customer access observer to the observer list.
	 * 
	 * @param observer the observer to add
	 */
	public void addOccasionalCustomerAccessObserver(OccasionalCustomerAccessObserver observer) {
		if (observer != null && !occasionalCustomerAccessObservers.contains(observer)) {
			occasionalCustomerAccessObservers.add(observer);
		}
	}

	/*
	 * Removes an occasional customer access observer from the observer list.
	 * 
	 * @param observer the observer to remove
	 */
	public void removeOccasionalCustomerAccessObserver(OccasionalCustomerAccessObserver observer) {
		occasionalCustomerAccessObservers.remove(observer);
	}

	/*
	 * Notifies all occasional customer access observers about the server response.
	 * 
	 * @param response the response received from the server
	 */
	private void notifyOccasionalCustomerAccessResult(OperationResponse response) {
		for (OccasionalCustomerAccessObserver observer : occasionalCustomerAccessObservers) {
			observer.onOccasionalCustomerAccessResult(response);
		}
	}
	
	public void addExistingCustomerLoginObserver(ExistingCustomerLoginObserver observer) {
		if (observer != null && !existingCustomerLoginObservers.contains(observer)) {
			existingCustomerLoginObservers.add(observer);
		}
	}

	public void removeExistingCustomerLoginObserver(ExistingCustomerLoginObserver observer) {
		existingCustomerLoginObservers.remove(observer);
	}

	private void notifyExistingCustomerLoginResult(OperationResponse response) {
		for (ExistingCustomerLoginObserver observer : existingCustomerLoginObservers) {
			observer.onExistingCustomerLoginResult(response);
		}
	}
	
	/*
	 * sends the server a request for all orders of the user
	 */
	public void requestOrders() {
		client.handleMessageFromClientUI(new Message(id, Protocol.RETURN_ORDER));
	}
	
	/*
	 * sends the server a request to update a specific order
	 * 
	 * @param um the data to update
	 */
	public void requestUpdate(UpdateMessage um) {
		client.handleMessageFromClientUI(new Message(um, Protocol.UPDATE_ORDER));
	}
	
	/*
	 * Sends the server an existing customer login request.
	 * 
	 * @param username the username entered by the customer
	 * @param password the password entered by the customer
	 */
	public void requestExistingCustomerLogin(String username, String password) {
		ExistingCustomerLoginRequest request = new ExistingCustomerLoginRequest(username, password);

		client.handleMessageFromClientUI(
				new Message(request, Protocol.EXISTING_CUSTOMER_LOGIN_REQUEST)
		);
	}
	
	/*
	 * Adds a register subscriber observer to the observer list.
	 * 
	 * @param observer the observer to add
	 */
	public void addRegisterSubscriberObserver(RegisterSubscriberObserver observer) {
		if (observer != null && !registerSubscriberObservers.contains(observer)) {
			registerSubscriberObservers.add(observer);
		}
	}

	/*
	 * Removes a register subscriber observer from the observer list.
	 * 
	 * @param observer the observer to remove
	 */
	public void removeRegisterSubscriberObserver(RegisterSubscriberObserver observer) {
		registerSubscriberObservers.remove(observer);
	}

	/*
	 * Notifies all register subscriber observers about the server response.
	 * 
	 * @param response the response received from the server
	 */
	private void notifyRegisterSubscriberResult(OperationResponse response) {
		for (RegisterSubscriberObserver observer : registerSubscriberObservers) {
			observer.onRegisterSubscriberResult(response);
		}
	}

	// Instance methods ************************************************

	/**
	 * This method overrides the method in the ChatIF interface. 
	 * it handles updating the UI according to the message received by the server
	 *
	 * @param message the message received from the server
	 */
	public void display(Message m) {
		Protocol type = m.getType();

		Platform.runLater(() -> {
			switch (type) {
			case CLIENT_DISCONNECT_SERVER:
				handleServerIssuedDisconnect();
				break;

			case UPDATE_ORDER_SUCCESS:
				notifyUpdateResult(true, (UpdateMessage) m.getData());
				break;

			case UPDATE_ORDER_FAILURE:
				notifyUpdateResult(false, (UpdateMessage) m.getData());
				break;

			case RETURN_ORDER:
				List<OrderRow> rows = parseOrderMessage(m.getData());

				if (rows == null) {
					break;
				}

				notifyOrdersReceived(rows);
				break;

			case ACTIVE_PARKS_RESULT:
			case PARKS_UPDATED:
				List<ParkInfo> parks = parseParkMessage(m.getData());

				if (parks == null) {
					break;
				}

				notifyParksReceived(parks);
				break;
			case OCCASIONAL_CUSTOMER_ACCESS_RESPONSE:
				OperationResponse response = (OperationResponse) m.getData();
				notifyOccasionalCustomerAccessResult(response);
				break;	
			case EMPLOYEE_LOGIN_RESPONSE:
				OperationResponse employeeLoginResponse = (OperationResponse) m.getData();
				notifyEmployeeLoginResult(employeeLoginResponse);
				break;
				
			case EXISTING_CUSTOMER_LOGIN_RESPONSE:
				OperationResponse existingCustomerLoginResponse = (OperationResponse) m.getData();
				notifyExistingCustomerLoginResult(existingCustomerLoginResponse);
				break;	
				
			case REGISTER_SUBSCRIBER_RESPONSE:
				OperationResponse registerSubscriberResponse = (OperationResponse) m.getData();
				notifyRegisterSubscriberResult(registerSubscriberResponse);
				break;	

			default:
				System.out.println("Error: Server Response Unknown in "
						+ "ClientController display");
			}
		});
	}
	/*
	 * this function is used to check if a given object is a list of orders
	 * and return the order list if so
	 * 
	 * @param o 	Object to check
	 */
	private List<OrderRow> parseOrderMessage(Object o) {
		List<OrderRow> rows = new ArrayList<>();
		if (o instanceof List<?>) {
			List<?> rawList = (List<?>) o;
			for(Object row : rawList) {
				if(row instanceof OrderRow)
					rows.add((OrderRow) row);
				else
					return null;
			}
		} else
			return null;
		return rows;
	}
	
	/*
	 * this method handles clean disconnect from the server
	 * when disconnect is issued by the user
	 */
	public void disconnectFromServer() {
		client.handleMessageFromClientUI(new Message(null, Protocol.CLIENT_DISCONNECT_USER));
		client.quit();
	}
	
	/*
	 * this method handles clean disconnect from the server
	 * when disconnect is issued by the server
	 */
	public void handleServerIssuedDisconnect() {
		for (OrderObserver observer : observers) {
			observer.handleExit();
		}
	}
	
	public boolean isUserIssuedDisconnect() {
		return userIssuedDisconnect;
	}

	public void setUserIssuedDisconnect(boolean userIssuedDisconnect) {
		this.userIssuedDisconnect = userIssuedDisconnect;
	}
	
	/*
	 * Observer pattern addition
	 * adding park observer to observer list
	 * 
	 * @param observer park observer to add
	 */
	public void addParkObserver(ParkObserver observer) {
		if (observer != null && !parkObservers.contains(observer)) {
			parkObservers.add(observer);
		}
	}

	/*
	 * Observer pattern addition
	 * removing park observer from observer list
	 * 
	 * @param observer park observer to remove
	 */
	public void removeParkObserver(ParkObserver observer) {
		parkObservers.remove(observer);
	}
	
	/*
	 * Adds an employee login observer to the observer list.
	 * 
	 * @param observer the observer to add
	 */
	public void addEmployeeLoginObserver(EmployeeLoginObserver observer) {
		if (observer != null && !employeeLoginObservers.contains(observer)) {
			employeeLoginObservers.add(observer);
		}
	}

	/*
	 * Removes an employee login observer from the observer list.
	 * 
	 * @param observer the observer to remove
	 */
	public void removeEmployeeLoginObserver(EmployeeLoginObserver observer) {
		employeeLoginObservers.remove(observer);
	}

	/*
	 * Notifies all employee login observers about the server response.
	 * 
	 * @param response the response received from the server
	 */
	private void notifyEmployeeLoginResult(OperationResponse response) {
		for (EmployeeLoginObserver observer : employeeLoginObservers) {
			observer.onEmployeeLoginResult(response);
		}
	}

	/*
	 * Observer pattern addition
	 * notifies the park observers of the received parks
	 * 
	 * @param parks the received parks
	 */
	private void notifyParksReceived(List<ParkInfo> parks) {
		for (ParkObserver observer : parkObservers) {
			observer.onParksReceived(parks);
		}
	}
	
	/*
	 * sends the server a request for all active parks
	 */
	public void requestActiveParks() {
		client.handleMessageFromClientUI(new Message(null, Protocol.GET_ACTIVE_PARKS));
	}
	
	/*
	 * Sends the server an occasional customer access request.
	 * 
	 * The occasional customer identifies himself by ID number.
	 * 
	 * @param customerIdNumber the ID number entered by the occasional customer
	 */
	public void requestOccasionalCustomerAccess(String customerIdNumber) {
		client.handleMessageFromClientUI(
				new Message(customerIdNumber, Protocol.OCCASIONAL_CUSTOMER_ACCESS_REQUEST)
		);
	}
	
	/*
	 * Sends the server an employee login request.
	 * 
	 * @param username the username entered by the employee
	 * @param password the password entered by the employee
	 */
	public void requestEmployeeLogin(String username, String password) {
		EmployeeLoginRequest request = new EmployeeLoginRequest(username, password);

		client.handleMessageFromClientUI(
				new Message(request, Protocol.EMPLOYEE_LOGIN_REQUEST)
		);
	}
	
	/*
	 * Sends the server a register subscriber request.
	 * 
	 * @param request the subscriber registration details
	 */
	public void requestRegisterSubscriber(RegisterSubscriberRequest request) {
		client.handleMessageFromClientUI(
				new Message(request, Protocol.REGISTER_SUBSCRIBER_REQUEST)
		);
	}

	/*
	 * sends a general message to the server
	 * 
	 * @param message the message to send
	 */
	public void sendMessageToServer(Message message) {
		client.handleMessageFromClientUI(message);
	}
	
	/*
	 * this function is used to check if a given object is a list of parks
	 * and return the park list if so
	 * 
	 * @param o Object to check
	 */
	private List<ParkInfo> parseParkMessage(Object o) {
		List<ParkInfo> parks = new ArrayList<>();

		if (o instanceof List<?>) {
			List<?> rawList = (List<?>) o;

			for (Object park : rawList) {
				if (park instanceof ParkInfo) {
					parks.add((ParkInfo) park);
				} else {
					return null;
				}
			}
		} else {
			return null;
		}

		return parks;
	}
	
	/*
	 * Sends the server a request for all orders of a specific subscriber.
	 * 
	 * This method is used after an existing customer logs in successfully.
	 * 
	 * @param subscriberId the subscriber id whose orders should be loaded
	 */
	public void requestOrdersBySubscriberId(int subscriberId) {
		client.handleMessageFromClientUI(
				new Message(String.valueOf(subscriberId), Protocol.RETURN_ORDER)
		);
	}
}