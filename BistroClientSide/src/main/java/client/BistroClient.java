package client;

import ocsf.client.*;
import common.BistroMessage;
import common.ChatIF;
import dataLayer.*;
import handlers.MemberProfileController;
import handlers.SceneLoader;
import handlers.StaffDashboardController;
import handlers.StaffLoginController;
import handlers.StaffWaitingListController;
import handlers.TableManagementController;
import handlers.VisitDetailsController;
import handlers.VisitIdentificationController;
import handlers.VisitNowController;
import handlers.TimeSlotController;
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
  public static TimeSlotController timeSlotControllerInstance;
  public static VisitNowController visitNowControllerInstance;
  public static VisitIdentificationController visitIdentificationControllerInstance;
  public static VisitDetailsController visitDetailsControllerInstance;
  public static MemberProfileController memberProfileControllerInstance;
  public static TableManagementController tableManagementControllerInstance;

  public static Reservation  reservationInstance = null;
  public static List<Visit> visitsList = null;
  public static ArrayList<Table> tables = new ArrayList<>();
  public static Integer wantedReservationId = null;
  public static String wantedVerCode = null;  
  public static Member memberInstance = null;
  public static Staff staffInstance = null;
  public static boolean awaitResponse = false;
  public static boolean operationSuccess = false;
  public static List<Reservation> reservationsList = null;
  public static StaffWaitingListController staffWaitingListControllerInstance;

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
            	tableManagementControllerInstance.updated(answer.getData());
            	break;
            // --- MANAGEMENT ROUTES ---
            case VERIFY_MEMBER_ARRIVAL:
            	staffDashControllerInstance.updateCheckInStatus(answer.getData());
		  		break;
            case CHECK_IN_CUSTOMER:
            	visitIdentificationControllerInstance.customerCheckedIn(answer.getData());
            	break;
            case GET_WAITING_LIST:
                if (staffWaitingListControllerInstance != null) {
                    staffWaitingListControllerInstance.updateWaitingList((List<Visit>) answer.getData());
                }
                break;
            case GET_ALL_MEMBERS:
                ArrayList<Member> receivedMembers = (ArrayList<Member>) answer.getData();                
                if (staffDashControllerInstance != null) {
                    staffDashControllerInstance.updateMembersList(receivedMembers);
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
		  	case UPDATE_MEMBER:
		  		memberProfileControllerInstance.isUpdated((Member)answer.getData());
		  		break;
		  	// --- RESERVATION ROUTES --
		  	case GET_RESERVATION:
		  		reservationInstance = (Reservation)answer.getData();
		  		wantedReservationId = reservationInstance.getReservationId();
		  		break;
		  	case GET_VERIFICATION_CODE:
		  		if(visitIdentificationControllerInstance != null) {
		  			visitIdentificationControllerInstance.checkIn(answer.getData());	  		
		  		}
		  		break;
		  	case FIND_RESERVATION:
		  		reservationInstance = (Reservation)answer.getData();
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
		  	    operationSuccess = true;
		  	  if (timeSlotControllerInstance != null) {
		          timeSlotControllerInstance.goToReservationDetails();
		      }
		  	    break;
		  	case RESERVATION_NOT_CREATED:
		  	    String errorMessage = (String) answer.getData();
		  	    SceneLoader.showAlert(Alert.AlertType.ERROR, "Reservation Failed", errorMessage);
		  	    operationSuccess = false;
		        reservationInstance = null;
		  	    break;
		  	case CANCEL_RESERVATION:
		  	    if ((boolean) answer.getData()) {
		  	        System.out.println("Reservation deleted successfully.");
		  	    } else {
		  	        System.out.println("Failed to delete reservation.");
		  	    }
		  		break;
		  	case CHECK_RESERVATION_AVAILABILITY:
		  	    if (answer.getData() instanceof List<?>) {
		  	        List<String> times = (List<String>) answer.getData();
		  	        if (timeSlotControllerInstance != null) {
		  	            timeSlotControllerInstance.updateAvailableTimes(times);
		  	        }
		  	        awaitResponse = false; // Stop waiting
		  	    }
		  	    break;
		  	case GET_MEMBER_RESERVATIONS:
		  		List<Reservation> reservations = new ArrayList<>();
		  		
		  		if(answer.getData() instanceof List<?>) {
		  			List<?> list = (List<?>) answer.getData();
		  			for (Object item : list) {
		  	            if (item instanceof Reservation) {
		  	                reservations.add((Reservation) item);
		  	            }
		  	        }
		  		}
		  		reservationsList = reservations;
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
//		  	case CREATE_VISIT:
//		  		Integer visitId = (Integer)answer.getData();
//		  		VisitDetailsController.visitCreated(visitId);
//		  	    break;
		  	case START_VISIT:
		  		boolean isStarted = (boolean)answer.getData();
		  		visitDetailsControllerInstance.visitStarted(isStarted);
		  		break;
		  	case VISIT_NOW:
		  		if(answer.getData() instanceof String) {
			  		String recieved = (String)answer.getData();
			  		visitNowControllerInstance.walkInVisitNotCreated(recieved);
		  		} else {
		  			Visit created = (Visit)answer.getData();
		  			visitNowControllerInstance.walkInVisitCreated(created);
		  		}
		  		break;
		  	// --- BILL ROUTES ---
		  	case UPDATE_BILL:
		  		tableManagementControllerInstance.updated(answer.getData());
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
      SceneLoader.showAlert(Alert.AlertType.ERROR, "Connection to server", "Server is unreacheable now: Terminating client.");
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
