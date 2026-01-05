package client;

import ocsf.client.*;
import common.BistroMessage;
import common.ChatIF;
import dataLayer.*;
import handlers.StaffDashboardController;
import handlers.StaffLoginController;

import java.io.*;

public class BistroClient extends AbstractClient
{
  ChatIF clientUI; 
  private static StaffLoginController staffLoginControllerInstance = new StaffLoginController();
  private static StaffDashboardController staffDashControllerInstance = new StaffDashboardController();
  public static Reservation  reservationInstance = new Reservation(null,null,null,null,null);
  public static Integer wantedReservationId = null;
  public static Integer wantedVerCode = null;  
  public static Member memberInstance = null;
  public static Staff staffInstance = null;
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
		  	// --- STAFF ROUTES ---
		  	case STAFF_IDENTIFICATION:
		  		staffInstance = (Staff)answer.getData();
		  		break;
		  	case STAFF_NOT_FOUND:
		  		staffLoginControllerInstance.staffNotLogged((String)answer.getData());
		  		break;
		  	// --- MEMBER ROUTES ---
		  	case MEMBER_IDENTIFICATION:
		  		memberInstance = (Member)answer.getData();
		  		break;
		  	case MEMBER_NOT_FOUND:
		  		memberInstance = null;
		  		break;
		  	case CREATE_MEMBER:
		  		staffDashControllerInstance.memberCreated(true, null);
		  		break;
		  	case MEMBER_NOT_CREATED:
		  		staffDashControllerInstance.memberCreated(false, (String)answer.getData());
		  		break;
		  	// --- RESERVATION ROUTES --
		  	case GET_RESERVATION:
		  		reservationInstance = (Reservation)answer.getData();
		  		wantedReservationId = reservationInstance.getReservationId();
		  		break;
		  	case GET_VERIFICATION_CODE:
		  		if (answer.getData() == null) {
                    reservationInstance = null;
                    wantedVerCode = null;
                    return;
		  		}
		  		reservationInstance = (Reservation)answer.getData();
		  		wantedVerCode = reservationInstance.getVerificationCode();

		  		break;
		  	case UPDATE_RESERVATION:
		  		if((boolean) answer.getData()) System.out.println("Update succeeded");
		  		else {
		  			System.out.println("Update failed");
		  		}
		  		break;
		  	case RESERVATION_NOT_FOUND:
		  		wantedReservationId = (Integer)answer.getData();
		  		break;
		  		
		  	case CREATE_RESERVATION:
		  		Reservation newRes = (Reservation) answer.getData();		  	    
		  	    reservationInstance = newRes;
		  	    wantedReservationId = newRes.getReservationId(); 	  	    
		  	    System.out.println("Reservation created successfully. ID: " + wantedReservationId);
		  	    break;
		  	    
		  	case CANCEL_RESERVATION:
		  	    if ((boolean) answer.getData()) {
		  	        System.out.println("Reservation deleted successfully.");
		  	    } else {
		  	        System.out.println("Failed to delete reservation.");
		  	    }
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
