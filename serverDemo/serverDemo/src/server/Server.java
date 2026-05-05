package server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import ocsf.server.*;
import common.*;

public class Server extends AbstractServer {
	//Class variables *************************************************
	  
	  /**
	   * The default port to listen on.
	   */
	  final public static int DEFAULT_PORT = 5555;
	  OrderConnection oc;
	  
	 //Constructors ****************************************************
	  
	 /**
	  * Constructs an instance of the echo server.
	  *
	   * @param port The port number to connect on.
	   */
	  public Server(int port) 
	  {
	    super(port);
	    oc = new OrderConnection();
	  }

	  
	  //Instance methods ************************************************
	  
	  /**
	   * This method handles any messages received from the client.
	   *
	   * @param msg The message received from the client.
	   * @param client The connection from which the message originated.
	   */
	  public void handleMessageFromClient
	    (Object msg, ConnectionToClient client)
	  {
		    System.out.println(
		    		"Message received: " + msg + " from " + client);
		    
		    Message m = (Message)msg;
		    handleRequest(m, client);
	  }
	  
	  private void handleRequest(Message m, ConnectionToClient client) {
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
				  UpdateMessage um = (UpdateMessage) m.getData();
				  try {
					  oc.updateOrder(um);
				  } catch(SQLException e) {
					  System.out.println(e.getMessage());
				  }
				  break;
			  case RETURN_ORDER:
				  List<OrderRow> req = null;
				  try {
					  req = oc.getUserOrders(m);
				  } catch(SQLException e) {
					  System.out.println(e.getMessage());
				  }
				  try {
					  if(req != null)
						  client.sendToClient(new Message(req, Protocol.RETURN_ORDER));
				  } catch(IOException e) {
					  System.out.println(e.getMessage());
				  }
				  break;
			  default:
				  System.out.println("Error: client request unknown");
		  }
	  }

	    
	  /**
	   * This method overrides the one in the superclass.  Called
	   * when the server starts listening for connections.
	   */
	  protected void serverStarted()
	  {
	    System.out.println
	      ("Server listening for connections on port " + getPort());
	  }
	  
	  /**
	   * This method overrides the one in the superclass.  Called
	   * when the server stops listening for connections.
	   */
	  protected void serverStopped()
	  {
	    System.out.println
	      ("Server has stopped listening for connections.");
	  }
	  
	  //Class methods ***************************************************
	  
	  /**
	   * This method is responsible for the creation of 
	   * the server instance (there is no UI in this phase).
	   *
	   * @param args[0] The port number to listen on.  Defaults to 5555 
	   *          if no argument is entered.
	   */
	  public static void main(String[] args) 
	  {
	    int port = 0; //Port to listen on

	    try
	    {
	      port = Integer.parseInt(args[0]); //Get port from command line
	    }
	    catch(Throwable t)
	    {
	      port = DEFAULT_PORT; //Set port to 5555
	    }
		
	    Server sv = new Server(port);
	    
	    try 
	    {
	      sv.listen(); //Start listening for connections
	    } 
	    catch (Exception ex) 
	    {
	      System.out.println("ERROR - Could not listen for clients!");
	    }
	  }
}
