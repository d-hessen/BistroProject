package client;

import ocsf.client.*;
import common.BistroMessage;
import common.ChatIF;
import dataLayer.*;
import handlers.MemberProfileController;
import handlers.PaymentScreenController;
import handlers.SceneLoader;
import handlers.StaffDashboardController;
import handlers.StaffLoginController;
import handlers.StaffWaitingListController;
import handlers.SystemSettingsController;
import handlers.TableManagementController;
import handlers.VisitDetailsController;
import handlers.VisitIdentificationController;
import handlers.VisitNowController;
import handlers.TimeSlotController;
import javafx.scene.control.Alert;
import handlers.SystemSettingsController;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
/**
 * BistroClient is the main client-side communication handler.
 * <p>
 * This class is responsible for:
 * <ul>
 *   <li>Managing the connection to the server</li>
 *   <li>Sending requests from the UI to the server</li>
 *   <li>Receiving and routing responses from the server to the appropriate controllers</li>
 * </ul>
 */
public class BistroClient extends AbstractClient
{

  // Reference to the UI interface that initiated the client.
  ChatIF clientUI; 
  
  /* =======================
   * Active Controller References
   * ======================= */
  
  /** Active staff login controller */
  public static StaffLoginController staffLoginControllerInstance;
  
  /** Active staff dashboard controller */
  public static StaffDashboardController staffDashControllerInstance;

  /** Active time slot selection controller */
  public static TimeSlotController timeSlotControllerInstance;

  /** Active walk-in visit controller */
  public static VisitNowController visitNowControllerInstance;

  /** Active visit identification controller */
  public static VisitIdentificationController visitIdentificationControllerInstance;

  /** Active visit details controller */
  public static VisitDetailsController visitDetailsControllerInstance;

  /** Active member profile controller */
  public static MemberProfileController memberProfileControllerInstance;

  /** Active table management controller */
  public static TableManagementController tableManagementControllerInstance;

  /** Active staff waiting list controller */
  public static StaffWaitingListController staffWaitingListControllerInstance;

  /* =======================
   * Shared Session Data
   * ======================= */
  
  /** Currently selected reservation */
  public static Reservation reservationInstance = null;

  /** List of visits retrieved from the server */
  public static List<Visit> visitsList = null;

  /** List of all restaurant tables */
  public static ArrayList<Table> tables = new ArrayList<>();

  /** Reservation ID currently being searched or processed */
  public static Integer wantedReservationId = null;

  /** Verification code currently being validated */
  public static String wantedVerCode = null;

  /** Logged in member instance */
  public static Member memberInstance = null;

  /** Logged in staff instance */
  public static Staff staffInstance = null;

  /** Indicates whether the client is waiting for a server response */
  public static boolean awaitResponse = false;

  /** Indicates whether the last operation succeeded */
  public static boolean operationSuccess = false;

  /** List of reservations for the logged in member */
  public static List<Reservation> reservationsList = null;

  /** Visit currently waiting for table allocation */
  public static Visit waitingVisit = null;

  /** Current waiting list */
  public static List<Visit> waitingList = null;

  /** Active visit instance */
  public static Visit visitInstance = null;

  /** List of all reservations in the system */
  public static ArrayList<Reservation> allReservationsList;
  public static List<Reservation> historyReservations;
  public static List<Visit> historyVisits;
  public static Boolean memberFoundStatus = null;

  /**
   * Constructs a new BistroClient.
   *
   * @param host     server host name or IP
   * @param port     server port number
   * @param clientUI UI interface that interacts with the client
   * @throws IOException if connection initialization fails
   */
  public BistroClient(String host, int port, ChatIF clientUI) 
    throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.clientUI = clientUI;
  }

  /**
   * Handles messages received from the server.
   * <p>
   * The method routes each response according to the {@link BistroMessage.Action}
   * and updates the relevant controllers and shared state.
   *
   * @param msg message object received from the server
   */
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
		  
          /* =======================
           * Staff Routes
           * ======================= */
		  
	        /**
	         * Handles successful staff identification.
	         * Sets the active staff session instance.
	         */
		  	case STAFF_IDENTIFICATION:
		  		staffInstance = (Staff)answer.getData();
		  		break;
	        /**
	         * Handles failed staff identification.
	         * Notifies the login controller with an error message.
	         */
		  	case STAFF_NOT_FOUND:
		  		if(staffLoginControllerInstance != null)
                    staffLoginControllerInstance.staffNotLogged((String)answer.getData());
                break;

            /* =======================
             * Dashboard & Tables
             * ======================= */
                
            /**
             * Retrieves all restaurant tables and updates the staff dashboard.
             */
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
            /**
             * Handles table creation or deletion results.
             */
            case ADD_TABLE:
            case DELETE_TABLE:
            	if(answer.getData() instanceof Table) {
            		operationSuccess = true;
            	}else {
            		SceneLoader.showAlert(Alert.AlertType.ERROR, "Table Management", answer.getData().toString());
            	}
            	break;
            /**
             * Handles table updates.
             */
            case UPDATE_TABLE:
            	tableManagementControllerInstance.updated(answer.getData());
            	break;
	        /**
	         * Updates member arrival verification status.
	         */
        	case VERIFY_MEMBER_ARRIVAL:
        		staffDashControllerInstance.updateCheckInStatus(answer.getData());
        		break;
            /**
             * Retrieves and updates the waiting list.
             */
            case GET_WAITING_LIST:
                if (staffWaitingListControllerInstance != null) {
                    staffWaitingListControllerInstance.updateWaitingList((List<Visit>) answer.getData());
                }
                waitingList = (List<Visit>) answer.getData();
                break;
            /**
             * Handles removal from waiting list.
             */
            case REMOVE_FROM_WAITING_LIST:
                if ((boolean) answer.getData()) {
                    operationSuccess = true;
                } else {
                    operationSuccess = false;
                }
                break;
                    
                /* =======================
                 * Member Routes
                 * ======================= */   
            	
            /**
             * Handles successful member identification.
             */
            case MEMBER_IDENTIFICATION:
                memberInstance = (Member)answer.getData();
                break;
            /**
             * Handles failed member identification.
             */
            case MEMBER_NOT_FOUND:
                memberInstance = null;
                break;
            /**
             * Handles member profile updates.
             */
            case UPDATE_MEMBER:
                memberProfileControllerInstance.isUpdated((Member)answer.getData());
                break;
            /**
             * Retrieves all registered members.
             */
            case GET_ALL_MEMBERS:
                ArrayList<Member> receivedMembers = (ArrayList<Member>) answer.getData();                
                if (staffDashControllerInstance != null) {
                    staffDashControllerInstance.updateMembersList(receivedMembers);
                }
                break;
            /**
             * Handles successful member creation.
             */
		  	case CREATE_MEMBER:
                if(staffDashControllerInstance != null)
                    staffDashControllerInstance.memberCreated(true, null);
                break;
            /**
             * Handles failed member creation.
             */
		  	case MEMBER_NOT_CREATED:
                if(staffDashControllerInstance != null)
                    staffDashControllerInstance.memberCreated(false, (String)answer.getData());
                break;
	        /**
	         * Handles member history fetching
	         */            
		  	case GET_MEMBER_HISTORY:
		  	    Object responseData = answer.getData();		  	    
		  	    if (responseData instanceof String && "NOT_FOUND".equals(responseData)) {
		  	        BistroClient.memberFoundStatus = false; 
		  	        BistroClient.historyReservations = null;
		  	        BistroClient.historyVisits = null;
		  	    } else {
		  	        List<Object> historyData = (List<Object>) responseData;
		  	        BistroClient.historyReservations = (List<Reservation>) historyData.get(0);
		  	        BistroClient.historyVisits = (List<Visit>) historyData.get(1);
		  	        BistroClient.memberFoundStatus = true; 
		  	    }
		  	    break;

                /* =======================
                 * Reservation Routes
                 * ======================= */
            
             /**
             * Retrieves a specific reservation.
             */
		  	case GET_RESERVATION:
		  		reservationInstance = (Reservation)answer.getData();
		  		wantedReservationId = reservationInstance.getReservationId();
		  		break;
            /**
             * Handles successful reservation creation.
             */
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
	        /**
	         * Handles failed reservation creation.
	         */
		  	case RESERVATION_NOT_CREATED:
		  	    String errorMessage = (String) answer.getData();
		  	    SceneLoader.showAlert(Alert.AlertType.ERROR, "Reservation Failed", errorMessage);
		  	    operationSuccess = false;
		        reservationInstance = null;
		  	    break;
	        /**
	         * Finds an existing reservation.
	         */
		  	case FIND_RESERVATION:
		  		reservationInstance = (Reservation)answer.getData();
		  		break;
	        /**
	         * Handles reservation update result.
	         */
		  	case UPDATE_RESERVATION:
		  		if((boolean) answer.getData()) System.out.println("Update succeeded");
		  		else {
		  			System.out.println("Update failed");
		  		}
		  		break;
	        /**
	         * Handles reservation not found scenario.
	         */
		  	case RESERVATION_NOT_FOUND:
		  		wantedReservationId = (Integer)answer.getData();
		  		break; 		
	        /**
	         * Handles reservation cancellation result.
	         */
		  	case CANCEL_RESERVATION:
		  	    if ((boolean) answer.getData()) {
		  	        System.out.println("Reservation deleted successfully.");
		  	    } else {
		  	        System.out.println("Failed to delete reservation.");
		  	    }
		  		break;
	        /**
	         * Handles availability check for reservation times.
	         */
		  	case CHECK_RESERVATION_AVAILABILITY:
		  	    if (answer.getData() instanceof List<?>) {
		  	        List<String> times = (List<String>) answer.getData();
		  	        if (timeSlotControllerInstance != null) {
		  	            timeSlotControllerInstance.updateAvailableTimes(times);
		  	        }
		  	        awaitResponse = false; // Stop waiting
		  	    }
		  	    break;
	        /**
	         * Retrieves reservations of a specific member.
	         */
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
            /**
             * Retrieves all reservations in the system.
             */
            case GET_ALL_RESERVATIONS:
                allReservationsList = (ArrayList<Reservation>)answer.getData();
                break;
		  	    
                /* =======================
                 * Visit Routes
                 * ======================= */
		  	    
	        /**
	         * Handles visit start confirmation.
	         */
		  	case START_VISIT:
		  		boolean isStarted = (boolean)answer.getData();
		  		visitDetailsControllerInstance.visitStarted(isStarted);
		  		break;
	        /**
	         * Retrieves visit history or active visits.
	         */
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
	        /**
	         * Handles walk-in visit creation.
	         */
		  	case VISIT_NOW:
		  		if(answer.getData() instanceof String) {
			  		String recieved = (String)answer.getData();
			  		visitNowControllerInstance.walkInVisitNotCreated(recieved);
		  		} else {
		  			Visit created = (Visit)answer.getData();
		  			if (created.getWaitingId() != null) {
		  			    visitNowControllerInstance.walkInVisitWaiting(created);
		  			} else {
		  			    visitNowControllerInstance.walkInVisitCreated(created);
		  			}
		  		}
		  		break;
	        /**
	         * Confirms customer check-in.
	         */
            case CHECK_IN_CUSTOMER:
            	visitIdentificationControllerInstance.customerCheckedIn(answer.getData());
            	break;
		  	    
                /* =======================
                 * Billing Routes
                 * ======================= */
		  	    
	        /**
	         * Marks the bill as paid.
	         */
		  	case BILL_PAID:
		  		PaymentScreenController.billWasPaid = true;
		  		break;
		  	case UPDATE_BILL:
		  		if(staffInstance != null) {
			  		tableManagementControllerInstance.updated(answer.getData());
		  		}
		  		break;
		  		
                /* =======================
                 * Restaurant Config Routes
                 * ======================= */
            
		  	 /**
             * Handles restaurant configuration update result.
             */
            case UPDATE_RESTAURANT_CONFIG:
                boolean success = (boolean)answer.getData();
                if(!success) {
                     SceneLoader.showAlert(Alert.AlertType.ERROR, "System Settings", "Failed to save settings.");
                } else {
                     System.out.println("Settings updated successfully.");
                }
                break;
            /**
             * Retrieves restaurant configuration data.
             */
            case GET_RESTAURANT_CONFIG:
                if (SystemSettingsController.getInstance() != null) {
                    SystemSettingsController.getInstance().setConfigData((RestaurantConfig)answer.getData());
                }
                break;

                /* =======================
                 * Verification Code Routes
                 * ======================= */

            /**
             * Handles verification code retrieval.
             */
		  	case GET_VERIFICATION_CODE:
		  		if(visitIdentificationControllerInstance != null && !PaymentScreenController.updateVisit) {
		  			visitIdentificationControllerInstance.checkIn(answer.getData());
		  		} else if(answer.getData() instanceof Visit) {
		  			visitInstance = (Visit)answer.getData();
		  		}
		  		break;
	        /**
	         * Handles forgotten verification code.
	         */
		  	case FORGOT_CODE:
		  		visitIdentificationControllerInstance.forgotenCode((String)answer.getData());
		  		break;
		  	// --- REPORT ROUTES ---
		  	case GET_REPORT_FILE:
		  	    if (staffDashControllerInstance != null) {
		  	        staffDashControllerInstance.receiveReport(answer.getData());
		  	    }
		  	    break;
		  	default:
	            System.out.println("Unknown Actionblbl: " + answer.getAction());
		  }
	  } catch(Exception e) {
		  e.printStackTrace();
	  }
  }
  
  /**
   * Sends a message from the UI to the server.
   * <p>
   * The method blocks until a response is received, ensuring synchronous behavior.
   *
   * @param obj message to be sent (must be {@link BistroMessage})
   */  
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
  
  /**
   * Terminates the client application gracefully.
   * <p>
   * Closes the server connection and exits the JVM.
   */
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
