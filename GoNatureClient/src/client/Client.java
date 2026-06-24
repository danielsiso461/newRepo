package client;

import java.io.IOException;

import clientController.ClientController;
import common.Message;
import javafx.application.Platform;
import ocsf.client.AbstractClient;
/**
 * this class represents the networking side of the client
 */

public class Client extends AbstractClient {
	/**
	 * the controller of the client
	 */
	ClientController clientController;

	//Constructors ****************************************************

	/**
	 * Constructs an instance of the client.
	 *
	 * @param host    			 The server to connect to.
	 * @param port    			 The port number to connect on.
	 * @param clientController 	 The controller for the client
	 * @throws IOException 		 On connection error
	 */
	public Client(String host, int port, ClientController clientController) throws IOException {
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
		if(!(msg instanceof Message))
			return;
		Message m = (Message) msg;
		clientController.display(m);
	}

	/**
	 * This method handles all data coming from the UI
	 * and sends it to the server
	 *
	 * @param msg The message from the UI.
	 */
	public void handleMessageFromClientUI(Message msg) {
		try {
			sendToServer(msg);
		} catch (IOException e) {
			System.out.println("err");
			quit();
		}
	}
	
	/**
	 * this method handles closing the client when a user quits
	 */
	@Override
	protected void connectionClosed() {
		if(clientController.isUserIssuedDisconnect()) {
			Platform.runLater(() -> {
	            Platform.exit();
	            System.exit(0);
	        });
		}
	}
	
	/**
	 * This method is called when an exception occurs in the connection to the server.
	 * 
	 * This usually happens when the server crashes, closes unexpectedly, or the
	 * network connection is lost.
	 * In this case, the client exits safely from the JavaFX application thread.
	 *
	 * @param exception the exception that caused the connection failure
	 */
	@Override
	protected void connectionException(Exception exception) {
		Platform.runLater(() -> {
			System.out.println("Connection to server was lost.");
			Platform.exit();
			System.exit(0);
		});
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
