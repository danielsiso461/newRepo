package client;

import java.io.IOException;

import common.Message;
import javafx.application.Platform;
import ocsf.client.AbstractClient;

public class Client extends AbstractClient {
//Instance variables **********************************************

	/**
	 * The interface type variable. It allows the implementation of the display
	 * method in the client.
	 */
	ClientService clientController;

//Constructors ****************************************************

	/**
	 * Constructs an instance of the chat client.
	 *
	 * @param host     The server to connect to.
	 * @param port     The port number to connect on.
	 * @param clientUI The interface type variable.
	 */

	public Client(String host, int port, ClientService clientController) throws IOException {
		super(host, port); // Call the superclass constructor
		this.clientController = clientController;
		openConnection();
	}

//Instance methods ************************************************

	/**
	 * This method handles all data that comes in from the server.
	 *
	 * @param msg The message from the server.
	 */
	public void handleMessageFromServer(Object msg) {
		Message m = (Message) msg;
		clientController.display(m);
	}

	/**
	 * This method handles all data coming from the UI
	 *
	 * @param message The message from the UI.
	 */
	public void handleMessageFromClientUI(Message msg) {
		try {
			sendToServer(msg);
		} catch (IOException e) {
			System.out.println("err");
			quit();
		}
	}
	
	@Override
	protected void connectionClosed() {
		if(clientController.isUserIssuedDisconnect()) {
			Platform.runLater(() -> {
	            Platform.exit();
	            System.exit(0);
	        });
		}
		else
			clientController.handleServerIssuedDisconnect();
	}

	/**
	 * This method terminates the client.
	 */
	public void quit() {
		try {
			closeConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
//End of ChatClient class
