package clientController;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import common.Park;

import client.Client;
import common.*;
import clientCommon.*;
import javafx.application.Platform;

/*
 * this class is the controller that connects the client networking side to the UI side
 * it is also taking care of the logic between the two components
 */

public class ClientController implements ChatIF {
	// Class variables *************************************************

	// Instance variables **********************************************
	/* The instance of the client that created this ConsoleChat.*/
	private Client client;
	/* the user id*/
	private String id = null;
	/* true if the user issues a disconnect */
	private boolean userIssuedDisconnect = false;
	/* Observer pattern addition*/
	private List<OrderObserver> observers = new ArrayList<>();
	/*
	 * Observer pattern addition for park screens
	 */
	private List<ParkObserver> parkObservers = new ArrayList<>();
	// observer for making orders
	private List<MakeOrderObserver> makeOrderObservers = new ArrayList<>();

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
	
	// getters ***************************************************************************
	/*
	 * getter that return the user's id
	 * @return the user ID
	 * */
	public String getId() {
		return id;
	}
	
	// order observer ********************************************************************
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
	 * @param rows of the received orders
	 */
	private void notifyOrdersReceived(List<Order> rows) {
		for (OrderObserver observer : observers) {
			observer.onOrdersReceived(rows);
		}
	}
	
	/*
	 * Observer pattern addition
	 * notifies the observers of the new order and sends it to them
	 * 
	 * @param o the new order
	 */
	private void notifyOrderMade(Order o) {
		for (OrderObserver observer : observers) {
			observer.addOrder(o);
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
	
	// make order observer ******************************************************************
	/*
	 * adds observer for making orders
	 * @param o the observer to add to the observer list
	 * */
	public void addMakeOrderObserver(MakeOrderObserver o) {
		if(!(o == null) && !(makeOrderObservers.contains(o)))
			makeOrderObservers.add(o);
	}
	
	/*
	 * removes observer from make order observer list
	 * @param o the observer to remove from the observer list
	 * */
	public void removeMakeOrderObserver(MakeOrderObserver o) {
		makeOrderObservers.remove(o);
	}
	
	/*
	 * notifies the make order observer about the received list of park Names
	 * @param parkNames the list of the park names
	 * */
	private void notifyParkNamesMakeOrderObserver(List<String> parkNames) {
		for(MakeOrderObserver o : makeOrderObservers)
			o.onParkNamesReceived(parkNames);
	}
	
	/*
	 * notifies the make order observer that a response from the server was received
	 * @param m the message from the server
	 */
	private void notifyServerResponseMakeOrderObserver(Message m) {
		for(MakeOrderObserver o : new ArrayList<>(makeOrderObservers))
			o.onMakeOrderServerResponse(m);
	}
	
	// client interactions *****************************************************************
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

	// GUI methods **********************************************************************

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
				List<Order> rows = parseOrderMessage(m.getData());

				if (rows == null) {
					break;
				}

				notifyOrdersReceived(rows);
				break;
			
			case RETURN_PARK_NAMES_SUCCESS:
				List<String> parkNames = parseParkNamesMessage(m.getData());
				if (parkNames == null) {
					break;
				}
				notifyParkNamesMakeOrderObserver(parkNames);
				break;
				
			case RETURN_PARK_NAMES_FAILURE:
				notifyParkNamesMakeOrderObserver(null);
				break;
			
			case MAKE_ORDER_SUCCESS:
				notifyOrderMade((Order) m.getData());
				notifyServerResponseMakeOrderObserver(m);
				break;
				
			case MAKE_ORDER_FAIL_NOT_GUIDE, MAKE_ORDER_FAIL_TIME,
					MAKE_ORDER_FAIL_NOT_SUBSCRIBED, MAKE_ORDER_FAIL:
					notifyServerResponseMakeOrderObserver(m);
					break;	
				
			case ACTIVE_PARKS_RESULT:
			case PARKS_UPDATED:
				List<Park> parks = parseParkMessage(m.getData());

				if (parks == null) {
					break;
				}

				notifyParksReceived(parks);
				break;

			default:
				System.out.println("Error: Server Response Unknown in "
						+ "ClientController display" + m.getType());
			}
		});
	}
	
	/*
	 * this function is used to check if a given object is a list of orders
	 * and return the order list if so
	 * 
	 * @param o 	Object to check
	 */
	
	// disconnects *********************************************************************************
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
	
	/*
	 * this method returns true if the user initiated a disconnect
	 * @return true if the user initiated a disconnect
	 * */
	public boolean isUserIssuedDisconnect() {
		return userIssuedDisconnect;
	}
	
	/*
	 * this method updates whether the user initiated a disconnect
	 * @param userIssuedDisconnect the value to set to userIssuedDisconnect
	 * */
	public void setUserIssuedDisconnect(boolean userIssuedDisconnect) {
		this.userIssuedDisconnect = userIssuedDisconnect;
	
	}
	
	
	// park observers ******************************************************************
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
	 * Observer pattern addition
	 * notifies the park observers of the received parks
	 * 
	 * @param parks the received parks
	 */
	private void notifyParksReceived(List<Park> parks) {
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
	 * sends a general message to the server
	 * 
	 * @param message the message to send
	 */
	public void sendMessageToServer(Message message) {
		client.handleMessageFromClientUI(message);
	
		
	// message parsers *********************************************************************
	}
	
	/*
	 * this function is used to check if a given object is a list of parks
	 * and return the park list if so
	 * 
	 * @param o Object to check
	 */
	private List<Park> parseParkMessage(Object o) {
		List<Park> parks = new ArrayList<>();

		if (o instanceof List<?>) {
			List<?> rawList = (List<?>) o;

			for (Object park : rawList) {
				if (park instanceof Park) {
					parks.add((Park) park);
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
	 * this function is used to check if a given object is a list of Orders
	 * and return the order list if so
	 * 
	 * @param o 	Object to check
	 */
	private List<Order> parseOrderMessage(Object o) {
		List<Order> rows = new ArrayList<>();
		if (o instanceof List<?>) {
			List<?> rawList = (List<?>) o;
			for(Object row : rawList) {
				if(row instanceof Order)
					rows.add((Order) row);
				else
					return null;
			}
		} else
			return null;
		return rows;
	}
	
	/*
	 * this function is used to check if a given object is a list of Strings
	 * and return the park names list if so
	 * 
	 * @param o 	Object to check
	 */
	private List<String> parseParkNamesMessage(Object o) {
		List<String> names = new ArrayList<>();
		if (o instanceof List<?>) {
			List<?> rawList = (List<?>) o;
			for(Object name : rawList) {
				if(name instanceof String)
					names.add((String) name);
				else
					return null;
			}
		} else
			return null;
		return names;
	}
}