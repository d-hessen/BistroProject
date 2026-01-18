package server;

import java.io.*;
import java.util.ArrayList;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import common.*;
import dataLayer.*;
import databaseController.*;
import domainLogic.*;
import ocsf.server.*;
import utils.ReportGenerator;

/**
 * BistroServer is the main server side controller of the system.
 * <p>
 * This class extends {@link AbstractServer} and is responsible for:
 * <ul>
 *   <li>Receiving requests from connected clients</li>
 *   <li>Routing requests based on {@link Action} type</li>
 *   <li>Delegating business logic to domain and controller layers</li>
 *   <li>Sending responses back to clients</li>
 *   <li>Managing scheduled background tasks</li>
 * </ul>
 *
 * <p>
 * The server also runs periodic maintenance jobs such as:
 * <ul>
 *   <li>No show processing</li>
 *   <li>Waiting list expiration</li>
 *   <li>Automatic billing</li>
 *   <li>Monthly report generation</li>
 * </ul>
 */
public class BistroServer extends AbstractServer 
{
	/** Reference to the server GUI controller (admin console). */
	private ServerFrameController guiController;

	/** Scheduler for periodic background server tasks. */
	private ScheduledExecutorService scheduler;
	
	/**
	 * Constructs a BistroServer instance.
	 *
	 * @param port the TCP port on which the server listens
	 * @param controller reference to the server GUI controller
	 */
	public BistroServer(int port, ServerFrameController controller) {
		super(port);
		this.guiController = controller;
	}

	/**
	 * Handles incoming messages from connected clients.
	 * <p>
	 * Each request is expected to be a {@link BistroMessage} containing
	 * an {@link Action} and optional payload.
	 * <p>
	 * The method routes the request to the appropriate controller
	 * based on the action type.
	 *
	 * @param msg the received message object
	 * @param client the client connection that sent the message
	 */
	@Override
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		BistroMessage request = (BistroMessage) msg;
		try {
			switch (request.getAction()) {
			
			/* =======================
		     * 		STAFF ROUTES
		     * ======================= */

		    /** Identifies a staff member attempting to log in. */
			case STAFF_IDENTIFICATION:
				Staff staffRecieved = (Staff)request.getData();
				client.sendToClient(StaffController.staffIdentification(staffRecieved, guiController));
				break;
				
			/** Retrieves all restaurant tables for staff dashboard. */
			case GET_ALL_TABLES:
				client.sendToClient(StaffController.getAllTables(guiController));
				break;
				
		    /** Adds a new table to the system. */
			case ADD_TABLE:
				Table tableRecieved = (Table)request.getData();
				client.sendToClient(StaffController.addNewTable(tableRecieved, guiController));
				break;
			
			/** Deletes an existing table. */
			case DELETE_TABLE:
				Table tableToDelete = (Table)request.getData();
				client.sendToClient(StaffController.deleteTable(tableToDelete, guiController));
				break;
				
			/** Updates table status or properties. */	
			case UPDATE_TABLE:
				Table tableToUpdate = (Table)request.getData();
				client.sendToClient(StaffController.updateTable(tableToUpdate, guiController));
				break;
				
			/** Verifies staff check in using member or reservation code. */
			case VERIFY_MEMBER_ARRIVAL:
				String cardCode = (String)request.getData();
				client.sendToClient(StaffController.verifyMemberArrival(cardCode, guiController));
				break;
			/** Retrieves the current waiting list. */
			case GET_WAITING_LIST:
				List<Visit> currentQueue = GetCommands.getWaitingList(guiController);
				BistroMessage response = new BistroMessage(Action.GET_WAITING_LIST, currentQueue);
				try {
                    client.sendToClient(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
				break;
				
			/** Creates a visit when a customer checks in (reservation or walk in). */
			case CHECK_IN_CUSTOMER:
				if(request.getData() instanceof Reservation) {
					client.sendToClient(
							VisitController.createReservatedVisit((Reservation)request.getData(), guiController));
				} else { //Visit
					client.sendToClient(VisitController.createWalkInVisit((Visit)request.getData(), guiController));
				}
				break;
				
			/** Removes a party from the waiting list by verification code. */
			case REMOVE_FROM_WAITING_LIST:
				if (request.getData() instanceof Integer) {
					int waitingIdToRemove = (Integer) request.getData();
					boolean success = DeleteCommands.deleteWaitingListEntry(waitingIdToRemove, guiController);

					if (success) {
						guiController.addToConsole("Removed waiting ID: " + waitingIdToRemove);
						client.sendToClient(new BistroMessage(Action.REMOVE_FROM_WAITING_LIST, true));
					} else {
						client.sendToClient(new BistroMessage(Action.REMOVE_FROM_WAITING_LIST, false));
					}
				}
				break;
				
			/** Recovers a lost reservation verification code. */
			case FORGOT_CODE:
				client.sendToClient(ReservationController.recoverLostCode((String)request.getData(), guiController));
				break;
				
			/** Retrieves restaurant configuration settings. */
			case GET_RESTAURANT_CONFIG:
				client.sendToClient(new BistroMessage(Action.GET_RESTAURANT_CONFIG,
						GetCommands.getRestaurantConfig(guiController)));
				break;
				
			/** Updates restaurant configuration and applies changes system-wide. */
			case UPDATE_RESTAURANT_CONFIG:
				boolean updated = UpdateCommands.updateRestaurantConfig((RestaurantConfig) request.getData(), guiController);
				ReservationController.ExistingReservationsNeedToBeCancled(guiController);
				client.sendToClient(new BistroMessage(Action.UPDATE_RESTAURANT_CONFIG, updated));
				break;
				
			/** Retrieves all reservations in the system (management view). */
			case GET_ALL_RESERVATIONS:
		        client.sendToClient(ReservationController.getAllReservations(guiController));
		        break;
		        
		    /** Retrieves full reservation and visit history for a member. */
			case GET_MEMBER_HISTORY:
			    Integer targetMemberId = (Integer) request.getData();			    
			    Member memberCheck = GetCommands.getMemberById(targetMemberId, guiController);			    
			    if (memberCheck == null) {
			        client.sendToClient(new BistroMessage(Action.GET_MEMBER_HISTORY, "NOT_FOUND"));
			    } else {
			        List<Reservation> historyReservations = GetCommands.getReservationsByMemberId(targetMemberId, guiController);
			        List<Visit> historyVisits = GetCommands.getMemberVisits(targetMemberId, guiController);			        
			        List<Object> fullHistory = new ArrayList<>();
			        fullHistory.add(historyReservations);
			        fullHistory.add(historyVisits);		        
			        client.sendToClient(new BistroMessage(Action.GET_MEMBER_HISTORY, fullHistory));
			    }
			    break;
                
			    /* =======================
			     * 		MEMBER ROUTES
			     * ======================= */
			    
			    /** Identifies a member attempting to log in. */
		    case MEMBER_IDENTIFICATION:
				Member memberRecieved = (Member)request.getData();
				client.sendToClient(GuestController.memberIdentification(memberRecieved, guiController));
				break;
				
		    /** Creates a new member account. */
			case CREATE_MEMBER:
				Member memberToCreate = (Member)request.getData();
				client.sendToClient(GuestController.memberCreation(memberToCreate, guiController));
				break;
				
		    /** Deletes a member account. */
			case DELETE_MEMBER:
				Member memberToDelete = (Member)request.getData();
				client.sendToClient(GuestController.memberDelete(memberToDelete, guiController));
				break;
				
		    /** Updates member personal details. */
			case UPDATE_MEMBER:
				client.sendToClient(GuestController.updateMemberDetails((Member)request.getData(),guiController));
				break;
				
		    /** Retrieves all registered members. */
			case GET_ALL_MEMBERS:
				List<Member> allMembers = GetCommands.getAllMembers(guiController);
				client.sendToClient(new BistroMessage(Action.GET_ALL_MEMBERS, allMembers));
				break;
                
			    /* =======================
			     * 	RESERVATION ROUTES
			     * ======================= */
				
		    /** Retrieves a reservation by ID. */
			case GET_RESERVATION:
				Integer resId = (Integer)request.getData();
				client.sendToClient(ReservationController.getReservation(resId, guiController));
				break;
				
		    /** Creates a new reservation. */
			case CREATE_RESERVATION:
				Reservation reservationToCreate = (Reservation)request.getData();
				client.sendToClient(ReservationController.createReservation(reservationToCreate, guiController));
				break;
				
		    /** Updates an existing reservation. */
			case UPDATE_RESERVATION:
				Reservation resToUpdate = (Reservation) request.getData();
				client.sendToClient(ReservationController.updateReservation(resToUpdate, guiController));
				break;
				
		    /** Cancels an existing reservation. */
			case CANCEL_RESERVATION:
				Reservation resToCancel = (Reservation) request.getData();
				client.sendToClient(ReservationController.cancelReservation(resToCancel, guiController));
				break;
				
		    /** Retrieves reservations associated with a member. */
			case GET_MEMBER_RESERVATIONS:
				String phoneNumber= (String)request.getData();
				client.sendToClient(ReservationController.getMemberReservations(phoneNumber, guiController));
				break;
				
		    /** Checks availability for a requested reservation time. */
			case CHECK_RESERVATION_AVAILABILITY:
				Reservation resForArrangement = (Reservation) request.getData();
				client.sendToClient(ReservationController.checkAvailability(resForArrangement, guiController));
				break;

			    /* =======================
			     * 		VISIT ROUTES
			     * ======================= */
				
		    /** Starts a visit session. */
			case START_VISIT:
				client.sendToClient(VisitController.updateVisit(request,guiController));
				break;
				
		    /** Retrieves visit history for a member. */
			case GET_MEMBER_VISITS:
				Integer memberId = (Integer) request.getData();
				List<Visit> memberVisits = GetCommands.getMemberVisits(memberId, guiController);
				client.sendToClient(new BistroMessage(Action.GET_MEMBER_VISITS, memberVisits));
				break;
				
		    /** Retrieves currently active visits. */
			case GET_ACTIVE_VISITS:
				List<Visit> activeVisits = GetCommands.getActiveVisits(guiController);
				client.sendToClient(new BistroMessage(Action.GET_ACTIVE_VISITS, activeVisits));
				break;
				
		    /** Creates a walk-in visit. */
			case VISIT_NOW:
				Visit toCreate = (Visit)request.getData();
				client.sendToClient(VisitController.createWalkInVisit(toCreate, guiController));
				break;
				
		    /** Retrieves a visit using a verification code. */
			case GET_VISIT:
            	client.sendToClient(GetCommands.getVisitByVerificationCode((String)request.getData(),guiController));
				break;
				
		    /** Verifies a reservation or visit using a code. */
			case GET_VERIFICATION_CODE:
            case FIND_RESERVATION:
            	//Sends back instance of Reservation or Visit
            	client.sendToClient(ReservationController.codeVerification(request, guiController));
                break;
                
                /* =======================
                 * 	  BILLING ROUTES
                 * ======================= */
                
            /** Updates bill details or marks bill as paid. */
            case UPDATE_BILL:
			case BILL_PAID:
				client.sendToClient(VisitController.updateBillOfVisit(request, guiController));
				break;
				
			    /* =======================
			     * 	  REPORT ROUTES
			     * ======================= */
				
		    /** Retrieves a generated PDF report file. */
			case GET_REPORT_FILE:
			    String filename = (String) request.getData();
			    File reportFile = new File("reports/" + filename);
			    if (reportFile.exists()) {
			        try {
			            byte[] fileContent = Files.readAllBytes(reportFile.toPath());
			            client.sendToClient(new BistroMessage(Action.GET_REPORT_FILE, fileContent));
			        } catch (IOException e) {
			            client.sendToClient(new BistroMessage(Action.GET_REPORT_FILE, "Error reading file"));
			        }
			    } else {
			        client.sendToClient(new BistroMessage(Action.GET_REPORT_FILE, null));//not found
			    }
			    break;
			    
			    /* =======================
			     * 	 CONNECTION ROUTES
			     * ======================= */
			    
		    /** Handles explicit client disconnection. */
		    case DISCONNECT:
				guiController.addToConsole("Client " + client.getInetAddress() + " disconnect");
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;

			default:
				System.out.println("Unknown Action: " + request.getAction());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called automatically when the server starts.
	 * <p>
	 * Initializes database connection, updates GUI status,
	 * and schedules periodic background maintenance tasks.
	 */
	@Override
	protected void serverStarted() {
		dbController.getInstance();
		guiController.addToConsole("SQL connection succeed");
		guiController.addToConsole("Server listening on port " + getPort());
		guiController.serverStatusChanged(true);
      
		scheduler = Executors.newScheduledThreadPool(1);
      	// Run checks every 1 minute
      	scheduler.scheduleAtFixedRate(() -> {
        	try {
        		//If month ended - generate report for previous month
        		LocalDateTime now = LocalDateTime.now();
        		if (now.getDayOfMonth() == 1 && now.getHour() == 0 && now.getMinute() == 0) {
                    LocalDateTime prevMonth = now.minusMonths(1);
                    int month = prevMonth.getMonthValue();
                    int year = prevMonth.getYear();

                    guiController.addToConsole("Generating monthly reports for " + month + "/" + year + "...");
                    List<Visit> visits = GetCommands.getFinishedVisitsForMonth(month, year, guiController);
                    List<WaitingHistoryItem> waitHistory = GetCommands.getMemberWaitingHistory(month, year, guiController);
                    ReportGenerator.generateTimeReport(visits, month, year);
                    ReportGenerator.generateMemberReport(visits, waitHistory, month, year);
                }
            	//Check for 15-minute no-shows
            	ReservationController.processNoShows(guiController);
            	//Send reminder for upcoming reservations
              	ReservationController.processReminders(guiController);
              	//Check if can insert new waiting visit
              	VisitController.checkWaitingListAndNotify(guiController);
              	//cancel waiting visits that haven't arrived after notification
              	VisitController.processWaitingListExpirations(guiController);
              	//Send bill to visits that are 2 hours or more
              	VisitController.processAutoBilling(guiController);
          	} catch (Exception e) {
              	e.printStackTrace();
          	}
      	}, 0, 1, TimeUnit.MINUTES);
  
	}

	/**
	 * Called when the server is stopped.
	 * Closes database connection and shuts down background scheduler.
	 */
	@Override
	protected void serverStopped() {
		guiController.serverStatusChanged(false); // Update GUI to Red
		dbController.getInstance().disconnectFromDB();
		if (scheduler != null && !scheduler.isShutdown()) {
        scheduler.shutdown();
    	}
	}

	/**
	 * Called when a new client connects to the server.
	 *
	 * @param client the connected client
	 */
	@Override
	protected void clientConnected(ConnectionToClient client) {
		guiController.addToConsole("Client connected: " + client.getInetAddress());
		guiController.updateClientList(client, "Connected");
	}

	/**
	 * Called when a client disconnects from the server.
	 *
	 * @param client the disconnected client
	 */
	@Override
	synchronized protected void clientDisconnected(ConnectionToClient client) {
		guiController.updateClientList(client, "Disconnected");
	}
}