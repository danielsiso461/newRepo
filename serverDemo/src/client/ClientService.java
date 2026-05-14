package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import common.ChatIF;
import common.Message;
import common.OrderRow;
import common.Protocol;
import common.UpdateMessage;
import javafx.application.Platform;

public class ClientService implements ChatIF {
	// Class variables *************************************************
	
	// Instance variables **********************************************
	// The instance of the client that created this ConsoleChat.
	private Client client;
	private String id = "1";//"907428969";//null
	private boolean userIssuedDisconnect = false;

	// Observer pattern addition
	private List<OrderObserver> observers = new ArrayList<>();

	// Constructors ****************************************************

	/**
	 * Constructs an instance of the ClientConsole UI.
	 *
	 * @param host The host to connect to.
	 * @param port The port to connect on.
	 */
	public ClientService(String host, int port) {
		try {
			client = new Client(host, port, this);
		} catch (IOException exception) {
			System.out.println("Error: Can't setup connection!" + " Terminating client.");
			System.exit(1);
		}
		
		//@todo THIS IS THE INPUT CHECK FOR ID DO NOT DELETE
		/*Scanner s = new Scanner(System.in);
		while (true) {
			System.out.println("Enter ID number: ");
			id = s.nextLine();
			// ID should have 9 characters
			if (id.length() != 9)
				continue;
			// check if it's a positive integer
			try {
				int val = Integer.parseInt(id);
				if (val > 0) {
					break;
				}
			} catch (NumberFormatException e) {
				continue;
			}
		}
		s.close();*/
	}

	// Observer pattern addition
	public void addObserver(OrderObserver observer) {
		if (observer != null && !observers.contains(observer)) {
			observers.add(observer);
		}
	}

	// Observer pattern addition
	public void removeObserver(OrderObserver observer) {
		observers.remove(observer);
	}

	// Observer pattern addition
	private void notifyOrdersReceived(List<OrderRow> rows) {
		for (OrderObserver observer : observers) {
			observer.onOrdersReceived(rows);
		}
	}

	// Observer pattern addition
	private void notifyUpdateResult(boolean success, UpdateMessage updateMessage) {
		for (OrderObserver observer : observers) {
			observer.onUpdateResult(success, updateMessage);
		}
	}

	public void requestOrders() {
		client.handleMessageFromClientUI(new Message(id, Protocol.RETURN_ORDER));
	}
	
	public void requestUpdate(UpdateMessage um) {
		client.handleMessageFromClientUI(new Message(um, Protocol.UPDATE_ORDER));
	}
	
	// Instance methods ************************************************

	/**
	 * This method overrides the method in the ChatIF interface. It displays a
	 * message onto the screen.
	 *
	 * @param message The string to be displayed.
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
				List<OrderRow> rows = (List<OrderRow>) m.getData();
				notifyOrdersReceived(rows);
				break;

			default:
				System.out.println("Error: Server Response Unknown in ClientService display");
			}
		});
	}
	
	public void disconnectFromServer() {
		client.handleMessageFromClientUI(new Message(null, Protocol.CLIENT_DISCONNECT_USER));
		client.quit();
	}
	
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
}