package client;
import java.io.*;

import client.common.ChatIF;

/**
 * ClientController acts as the client-side entry point
 * between the UI layer and the {@link BistroClient}.
 * <p>
 * This class is responsible for:
 * <ul>
 *   <li>Creating and initializing the {@link BistroClient}</li>
 *   <li>Forwarding messages from the UI to the client</li>
 *   <li>Displaying messages received from the client</li>
 * </ul>
 *
 * <p>
 * It implements the {@link ChatIF} interface, allowing it to serve as
 * the communication bridge required by the OCSF framework.
 */
public class ClientController implements ChatIF 
{
	/**
	 * The client instance responsible for server communication.
	 */
  BistroClient client;

  /**
   * Constructs a new ClientController and initializes the client connection.
   *
   * @param host the server host name or IP address
   * @param port the server port number
   */
  public ClientController(String host, int port) 
  {
    try 
    {
      client = new BistroClient(host, port, this);
    } 
    catch(IOException exception) 
    {
      System.out.println("Error: Can't setup connection!"+ " Terminating client.");
      System.exit(1);
    }
  }
  
  /**
   * Receives an object from the UI layer and forwards it to the client.
   * <p>
   * This method is typically used to send {@link common.BistroMessage}
   * objects to the server via the {@link BistroClient}.
   *
   * @param obj the object to be sent to the client
   */
  public void accept(Object obj) 
  {
	  client.handleMessageFromClientUI(obj);
  }
  
  /**
   * Displays a message received from the client or server.
   * <p>
   * This method overrides {@link ChatIF#display(String)} and outputs
   * the message to the standard console.
   *
   * @param message the message to be displayed
   */
  public void display(String message) 
  {
    System.out.println("> " + message);
  }
}
