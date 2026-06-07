package clientController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import client.Client;
import clientCommon.*;
import common.CancelOrderMessage;
import common.ChatIF;
import common.Message;
import common.OperationResponse;
import common.OrderRow;
import common.ParkInfo;
import common.Protocol;
import common.UpdateMessage;
import javafx.application.Platform;
import common.WaitingListMessage;

/*
 * this class is the controller that connects the client networking side to the UI side
 * it is also taking care of the logic between the two components
 */
@SuppressWarnings("deprecation")
public class ClientController implements ChatIF {

	// Instance variables **********************************************

	// The instance of the client that handles the connection to the server.
	private Client client;

	// The ID of the currently connected user.
	private String id = null;

	private boolean userIssuedDisconnect = false;

	/*
	 * Observer pattern addition for order screens.
	 * These observers are notified when order-related responses arrive from the server.
	 */
	private List<OrderObserver> observers = new ArrayList<>();

	/*
	 * Observer pattern addition for park screens.
	 * These observers are notified when park-related responses arrive from the server.
	 */
	private List<ParkObserver> parkObservers = new ArrayList<>();

	/*
	 * Observer pattern addition for occasional customer access screen.
	 * These observers are notified when access-check responses arrive from the server.
	 */
	private List<OccasionalCustomerAccessObserver> occasionalCustomerAccessObservers = new ArrayList<>();
	/*
	 * Observer pattern addition for waiting list screens.
	 * These observers are notified when waiting-list responses arrive from the server.
	 */
	private List<WaitingListObserver> waitingListObservers = new ArrayList<>();
	// Constructors ****************************************************

	/**
	 * Constructs an instance of the ClientController and connects to the server.
	 *
	 * @param host the host to connect to
	 * @param port the port to connect on
	 * @param id   the user ID
	 * @throws IOException if the client connection cannot be created
	 */
	public ClientController(String host, int port, String id) throws IOException {
		try {
			client = new Client(host, port, this);
		} catch (IOException exception) {
			throw exception;
		}

		this.id = id;
	}

	/*
	 * Observer pattern addition.
	 * Adds an order observer to the observer list.
	 * 
	 * @param observer observer to add
	 */
	public void addObserver(OrderObserver observer) {
		if (observer != null && !observers.contains(observer)) {
			observers.add(observer);
		}
	}

	/*
	 * Observer pattern addition.
	 * Removes an order observer from the observer list.
	 * 
	 * @param observer observer to remove
	 */
	public void removeObserver(OrderObserver observer) {
		observers.remove(observer);
	}

	/*
	 * Observer pattern addition.
	 * Notifies the observers of the received orders and sends them the order list.
	 * 
	 * @param rows the received orders
	 */
	private void notifyOrdersReceived(List<OrderRow> rows) {
		for (OrderObserver observer : observers) {
			observer.onOrdersReceived(rows);
		}
	}

	/*
	 * Observer pattern addition.
	 * Notifies the observers about the result of an order update request.
	 * 
	 * @param success       true if the update succeeded, false otherwise
	 * @param updateMessage the data of the update
	 */
	private void notifyUpdateResult(boolean success, UpdateMessage updateMessage) {
		for (OrderObserver observer : observers) {
			observer.onUpdateResult(success, updateMessage);
		}
	}

	/**
	 * Notifies all order observers about the result of an order cancellation request.
	 *
	 * @param success            true if the cancellation succeeded, false otherwise
	 * @param cancelOrderMessage the cancellation request data returned by the server
	 */
	private void notifyCancelResult(boolean success, CancelOrderMessage cancelOrderMessage) {
		for (OrderObserver observer : observers) {
			observer.onCancelResult(success, cancelOrderMessage);
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
	/*
	 * Adds a waiting list observer to the observer list.
	 * 
	 * @param observer the observer to add
	 */
	public void addWaitingListObserver(WaitingListObserver observer) {
		if (observer != null && !waitingListObservers.contains(observer)) {
			waitingListObservers.add(observer);
		}
	}

	/*
	 * Removes a waiting list observer from the observer list.
	 * 
	 * @param observer the observer to remove
	 */
	public void removeWaitingListObserver(WaitingListObserver observer) {
		waitingListObservers.remove(observer);
	}

	/**
	 * Notifies all waiting list observers about the result of a join waiting list request.
	 *
	 * @param success            true if the visitor was added to the waiting list successfully
	 * @param waitingListMessage the waiting list data returned by the server
	 */
	private void notifyJoinWaitingListResult(boolean success, WaitingListMessage waitingListMessage) {
		for (WaitingListObserver observer : waitingListObservers) {
			observer.onJoinWaitingListResult(success, waitingListMessage);
		}
	}

	/*
	 * Sends the server a request for all orders of the current user.
	 */
	public void requestOrders() {
		client.handleMessageFromClientUI(new Message(id, Protocol.RETURN_ORDER));
	}

	/*
	 * Sends the server a request to update a specific order.
	 * 
	 * @param um the data to update
	 */
	public void requestUpdate(UpdateMessage um) {
		client.handleMessageFromClientUI(new Message(um, Protocol.UPDATE_ORDER));
	}

	/**
	 * Sends the server a request to cancel a specific order.
	 *
	 * The order will not be deleted from the database. The server should update
	 * its order_status to "cancelled", so the cancellation can still be used in
	 * reports and order history.
	 *
	 * @param cancelOrderMessage the data of the order cancellation request
	 */
	public void requestCancelOrder(CancelOrderMessage cancelOrderMessage) {
		client.handleMessageFromClientUI(new Message(cancelOrderMessage, Protocol.CANCEL_ORDER));
	}
	/**
	 * Sends the server a request to add a visitor to the waiting list.
	 *
	 * @param waitingListMessage the data of the waiting list request
	 */
	public void requestJoinWaitingList(WaitingListMessage waitingListMessage) {
		client.handleMessageFromClientUI(
				new Message(waitingListMessage, Protocol.JOIN_WAITING_LIST_REQUEST)
		);
	}

	/*
	 * Sends the server a request for all active parks.
	 */
	public void requestActiveParks() {
		client.handleMessageFromClientUI(new Message(null, Protocol.GET_ACTIVE_PARKS));
	}

	/*
	 * Sends the server a request to check occasional customer access by order number.
	 * 
	 * @param orderNumber the order number entered by the customer
	 */
	public void requestOccasionalCustomerAccess(int orderNumber) {
		client.handleMessageFromClientUI(
				new Message(orderNumber, Protocol.OCCASIONAL_CUSTOMER_ACCESS_REQUEST)
		);
	}

	/**
	 * This method overrides the method in the ChatIF interface.
	 * It handles updating the UI according to the message received from the server.
	 *
	 * @param m the message received from the server
	 */
	@Override
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

			case CANCEL_ORDER_SUCCESS:
				notifyCancelResult(true, (CancelOrderMessage) m.getData());
				break;

			case CANCEL_ORDER_FAILURE:
				notifyCancelResult(false, (CancelOrderMessage) m.getData());
				break;
			case JOIN_WAITING_LIST_SUCCESS:
				notifyJoinWaitingListResult(true, (WaitingListMessage) m.getData());
				break;

			case JOIN_WAITING_LIST_FAILURE:
				notifyJoinWaitingListResult(false, (WaitingListMessage) m.getData());
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

			default:
				System.out.println("Error: Server Response Unknown in ClientController display");
			}
		});
	}

	/*
	 * This function is used to check if a given object is a list of orders
	 * and return the order list if so.
	 * 
	 * @param o object to check
	 */
	private List<OrderRow> parseOrderMessage(Object o) {
		List<OrderRow> rows = new ArrayList<>();

		if (o instanceof List<?>) {
			List<?> rawList = (List<?>) o;

			for (Object row : rawList) {
				if (row instanceof OrderRow) {
					rows.add((OrderRow) row);
				} else {
					return null;
				}
			}
		} else {
			return null;
		}

		return rows;
	}

	/*
	 * This method handles clean disconnect from the server
	 * when disconnect is issued by the user.
	 */
	public void disconnectFromServer() {
		client.handleMessageFromClientUI(new Message(null, Protocol.CLIENT_DISCONNECT_USER));
		client.quit();
	}

	/*
	 * This method handles clean disconnect from the server
	 * when disconnect is issued by the server.
	 */
	public void handleServerIssuedDisconnect() {
		for (OrderObserver observer : observers) {
			observer.handleExit();
		}

		for (WaitingListObserver observer : waitingListObservers) {
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
	 * Observer pattern addition.
	 * Adds a park observer to the observer list.
	 * 
	 * @param observer park observer to add
	 */
	public void addParkObserver(ParkObserver observer) {
		if (observer != null && !parkObservers.contains(observer)) {
			parkObservers.add(observer);
		}
	}

	/*
	 * Observer pattern addition.
	 * Removes a park observer from the observer list.
	 * 
	 * @param observer park observer to remove
	 */
	public void removeParkObserver(ParkObserver observer) {
		parkObservers.remove(observer);
	}

	/*
	 * Observer pattern addition.
	 * Notifies the park observers of the received parks.
	 * 
	 * @param parks the received parks
	 */
	private void notifyParksReceived(List<ParkInfo> parks) {
		for (ParkObserver observer : parkObservers) {
			observer.onParksReceived(parks);
		}
	}

	/*
	 * Sends a general message to the server.
	 * 
	 * @param message the message to send
	 */
	public void sendMessageToServer(Message message) {
		client.handleMessageFromClientUI(message);
	}

	/*
	 * This function is used to check if a given object is a list of parks
	 * and return the park list if so.
	 * 
	 * @param o object to check
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
}