package client;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import common.*;
import javafx.application.Platform;

public class ClientService implements ChatIF {
	// Class variables *************************************************
	
	// Instance variables **********************************************
	// The instance of the client that created this ConsoleChat.
	private Client client;
	private String id = "907428969";//null
	private OrderTableDisplayPage controller;

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
		
		//client.handleMessageFromClientUI(new Message(id, Protocol.RETURN_ORDER));
	}

	public void setController(OrderTableDisplayPage controller) {
		this.controller = controller;
	}
	
	public void requestOrders() {
		client.handleMessageFromClientUI(new Message(id, Protocol.RETURN_ORDER));
	}
	
	// Instance methods ************************************************
	
	/**
	 * This method waits for input from the console. Once it is received, it sends
	 * it to the client's message handler.
	 */
	public void accept() {
		try {
			while (true) {
				//receives from UI - @todo
				//client.handleMessageFromClientUI(new Message());
			}
		} catch (Exception ex) {
			System.out.println("Unexpected error while reading from user!");
		}
	}

	/**
	 * This method overrides the method in the ChatIF interface. It displays a
	 * message onto the screen.
	 *
	 * @param message The string to be displayed.
	 */
	public void display(Message m) {
		Protocol type = m.getType();
		switch (type) {
		case UPDATE_ORDER:
			// placeholder
			break;
		case RETURN_ORDER:
			List<OrderRow> rows = (List<OrderRow>) m.getData();
			for(OrderRow r : rows)
				System.out.println(r.toString());
			controller.setData(rows);
			break;
		default:
			System.out.println("Error: ClientService display");
		}
	}

	
	// Class methods ***************************************************

	/**
	 * This method is responsible for the creation of the Client UI.
	 *
	 * @param args[0] The host to connect to.
	 */
	public static void main(String[] args) {
		/*String host = "";

		try {
			host = args[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			host = "localhost";
		}
		ClientUI clientGUI = new ClientUI(host, ConstantsUI.DEFAULT_PORT);
		clientGUI.accept();*/
	}
}
//907428969
