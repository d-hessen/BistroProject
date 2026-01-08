package client;

import ocsf.client.*;
import common.Action;
import common.BistroMessage;
import common.ChatIF;
import dataLayer.*;
import handlers.SceneLoader;
import handlers.StaffDashboardController;
import handlers.StaffLoginController;
import handlers.VisitDetailsController;
import handlers.VisitNowController;
import javafx.scene.control.Alert;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BistroClient extends AbstractClient
{
  ChatIF clientUI; 
  
  //STATIC REFERENCES TO ACTIVE CONTROLLERS
  public static StaffLoginController staffLoginControllerInstance;
  public static StaffDashboardController staffDashControllerInstance;
  public static VisitNowController visitNowControllerInstance;
  
  public static Reservation  reservationInstance = null;
  public static List<Visit> visitsList = null;
  public static ArrayList<Table> tables = new ArrayList<>();
  public static Integer wantedReservationId = null;
  public static String wantedVerCode = null;  
  public static Member memberInstance = null;
  public static Staff staffInstance = null;
  public static boolean awaitResponse = false;
  public static boolean operationSuccess = false;
  public static Visit currentVisit;

	 
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
		  		if(staffLoginControllerInstance != null)
                    staffLoginControllerInstance.staffNotLogged((String)answer.getData());
                break;
            // --- DASHBOARD UPDATES ---
            case GET_ALL_TABLES:
		  		if(answer.getData() instanceof ArrayList<?>) {
		  			ArrayList<?> list = (ArrayList<?>) answer.getData();
		  			for(Object item : list) {
		  				if(item instanceof Table) {
		  					tables.add((Table)item);
		  				} else {
		  					throw new ClassCastException("List contained an element of unexpected type");
		  				}
		  			}
		  		}
                if(staffDashControllerInstance != null) {
                    staffDashControllerInstance.updateTableGrid(tables);
                }
                break;
            // --- TABLE ROUTES ---
            case DELETE_TABLE:
            	if(answer.getData() instanceof Table) {
            		operationSuccess = true;
            	}else {
            		SceneLoader.showAlert(Alert.AlertType.ERROR, "Table Management", answer.getData().toString());
            	}
            	break;
            case ADD_TABLE:
            	if(answer.getData() instanceof Table) {
            		operationSuccess = true;
            	}else {
            		SceneLoader.showAlert(Alert.AlertType.ERROR, "Table Management", answer.getData().toString());
            	}
            	break;
            case UPDATE_TABLE:
            	if(answer.getData() instanceof Table) {
            		operationSuccess = true;
            	} else {
            		SceneLoader.showAlert(Alert.AlertType.ERROR, "Table Management", answer.getData().toString());
            	}
            	break;
            // --- MANAGEMENT ROUTES ---
            case CHECK_IN_CUSTOMER: // Response from check-in
                String result = (String) answer.getData();
                if(staffDashControllerInstance != null) {
                    staffDashControllerInstance.updateCheckInStatus(result.startsWith("Success"), result);
                }
                break;
            case VERIFY_MEMBER_ARRIVAL:
                String mesg = (String) answer.getData();
                boolean isSuccess = mesg.startsWith("Success");
                if (staffDashControllerInstance != null) {
                    staffDashControllerInstance.updateCheckInStatus(isSuccess, mesg);
                }
                break;
		  	// --- MEMBER ROUTES ---
		  	case MEMBER_IDENTIFICATION:
		  		memberInstance = (Member)answer.getData();
		  		break;
		  	case MEMBER_NOT_FOUND:
		  		memberInstance = null;
		  		break;
		  	case CREATE_MEMBER:
                if(staffDashControllerInstance != null)
                    staffDashControllerInstance.memberCreated(true, null);
                break;
		  	case MEMBER_NOT_CREATED:
                if(staffDashControllerInstance != null)
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
		  	// ---VISITS ROUTES---
		  	case GET_MEMBER_VISITS:
		  	case GET_ACTIVE_VISITS:
		  		List<Visit> visits = new ArrayList<>();
		  		if(answer.getData() instanceof List<?>) {
		  			List<?> list = (List<?>) answer.getData();
		  			for(Object item : list) {
		  				if(item instanceof Visit) {
		  					visits.add((Visit)item);
		  				} else {
		  					throw new ClassCastException("List contained an element of unexpected type");
		  				}
		  			}
		  		}
		  	    visitsList = visits;
		  	    break;
		  	case CREATE_VISIT:
		  		Integer visitId = (Integer)answer.getData();
		  		VisitDetailsController.visitCreated(visitId);
		  	    break;
		  	    
		  	case VISIT_NOW:
		  		String recieved = (String)answer.getData();
		  		visitNowControllerInstance.randomVisitCreated(recieved);
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
      SceneLoader.showAlert(Alert.AlertType.ERROR, "Connection to server", "Could not send message to server: Terminating client.");
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
