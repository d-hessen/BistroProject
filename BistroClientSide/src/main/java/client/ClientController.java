package client;
import java.io.*;
import common.ChatIF;

public class ClientController implements ChatIF 
{
  /**
   * The instance of the client that created this ConsoleChat.
   */
  BistroClient client;

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
   * This method waits for input from the console.  Once it is 
   * received, it sends it to the client's message handler.
   */
  public void accept(Object obj) 
  {
	  client.handleMessageFromClientUI(obj);
  }
  
  /**
   * This method overrides the method in the ChatIF interface.  It
   * displays a message onto the screen.
   *
   * @param message The string to be displayed.
   */
  public void display(String message) 
  {
    System.out.println("> " + message);
  }
}
