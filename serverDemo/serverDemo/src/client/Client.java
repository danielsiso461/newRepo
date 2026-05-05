package client;

import ocsf.client.*;
import common.*;
import java.io.*;

public class Client extends AbstractClient {
//Instance variables **********************************************

	/**
	 * The interface type variable. It allows the implementation of the display
	 * method in the client.
	 */
	ChatIF clientUI;

//Constructors ****************************************************

	/**
	 * Constructs an instance of the chat client.
	 *
	 * @param host     The server to connect to.
	 * @param port     The port number to connect on.
	 * @param clientUI The interface type variable.
	 */

	public Client(String host, int port, ChatIF clientUI) throws IOException {
		super(host, port); // Call the superclass constructor
		this.clientUI = clientUI;
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
		handleResponseFromServer(m);
		//clientUI.display(m);
	}
	
	private void handleResponseFromServer(Message m) {
		//@todo is this switch case needed?
		  Protocol type = m.getType();
		  switch(type) {
			  case CLIENT_CONNECT:
				  //placeholder
				  break;
			  case CLIENT_DISCONNECT:
				  //placeholder
				  break;
			  case MAKE_ORDER:
				  //not in prototype
				  break;
			  case UPDATE_ORDER:
				  //placeholder
				  break;
			  case RETURN_ORDER:
				  clientUI.display(m);
				  break;
			  default:
				  System.out.println("Error: sever response unknown");
		  }
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
			//clientUI.display("Could not send message to server.  Terminating client.");
			quit();
		}
	}

	/**
	 * This method terminates the client.
	 */
	public void quit() {
		try {
			closeConnection();
		} catch (IOException e) {
		}
		System.exit(0);
	}
}
//End of ChatClient class
