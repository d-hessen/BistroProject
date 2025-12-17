package client;

import ocsf.client.*;
import common.BistroMessage;
import common.ChatIF;
import dataLayer.Reservation;

import java.io.*;

public class BistroClient extends AbstractClient
{
  ChatIF clientUI; 
  public static Reservation  reservationInstance = new Reservation(null,null,null,null,null,null);
  public static boolean awaitResponse = false;
	 
  public BistroClient(String host, int port, ChatIF clientUI) 
    throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.clientUI = clientUI;
  }

  //This method handles all data that comes in from the server.
  public void handleMessageFromServer(Object msg) 
  {
	  if (!(msg instanceof BistroMessage)) {
          System.out.println("Unknown object received");
          return;
      }
	  awaitResponse = false;
	  BistroMessage answer = (BistroMessage) msg;
	  try {
		  switch(answer.getAction()) {
		  		case GET_RESERVATION:
		  			reservationInstance = (Reservation)answer.getData();
		  			break;
		  		case UPDATE_RESERVATION:
		  			if((boolean) answer.getData()) System.out.println("Update succeeded");
		  			else {
		  				System.out.println("Update failed");
		  			}
		  			break;
		  		case RESERVATION_NOT_FOUND:
		  			reservationInstance = new Reservation((Integer)answer.getData(),null,null,null,null,null);
		  			break;
		  		default:
	                  System.out.println("Unknown Action: " + answer.getAction());
		  }
	  } catch(Exception e) {
		  e.printStackTrace();
	  }
  }
  
  //Method that handles every message from client
  public void handleMessageFromClientUI(Object obj)  
  {
	if (!(obj instanceof BistroMessage)) {
        System.out.println("Unknown object received");
        return;
    }
	BistroMessage request = (BistroMessage) obj;
    try
    {
    	switch(request.getAction()) {
    		case DISCONNECT: //Disconnect before connection opens not waiting to response
        		if(isConnected())
        			sendToServer(obj);
    			break;
    		default:
    			openConnection();
    	       	awaitResponse = true;
    	    	sendToServer(request);
    			// wait for response
    			while (awaitResponse) {
    				try {
    					Thread.sleep(100);
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
    			}
    	}
    	
    }
    catch(IOException e)
    {
    	e.printStackTrace();
      clientUI.display("Could not send message to server: Terminating client."+ e);
      quit();
    }
  }
  
  //Terminate the client
  public void quit()
  {
    try
    {
      closeConnection();
    }
    catch(IOException e) {}
    System.exit(0);
  }
}
