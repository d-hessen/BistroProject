package server;

import java.io.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import common.*;
import dataLayer.*;
import databaseController.*;
import domainLogic.*;
import ocsf.server.*;

public class BistroServer extends AbstractServer 
{
	private ServerFrameController guiController; // Reference to the GUI Controller
	private ScheduledExecutorService scheduler;
	public BistroServer(int port, ServerFrameController controller) {
		super(port);
		this.guiController = controller;
	}

	@Override
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		BistroMessage request = (BistroMessage) msg;
		try {
			switch (request.getAction()) {
			// --- START OF CASES ---
			// --- STAFF ROUTES ---
			case STAFF_IDENTIFICATION:
				Staff staffRecieved = (Staff)request.getData();
				client.sendToClient(StaffController.staffIdentification(staffRecieved, guiController));
				break;
			case GET_ALL_TABLES:
				client.sendToClient(StaffController.getAllTables(guiController));
				break;
			case ADD_TABLE:
				Table tableRecieved = (Table)request.getData();
				client.sendToClient(StaffController.addNewTable(tableRecieved, guiController));
				break;
			case DELETE_TABLE:
				Table tableToDelete = (Table)request.getData();
				client.sendToClient(StaffController.deleteTable(tableToDelete, guiController));
				break;
			case UPDATE_TABLE:
				Table tableToUpdate = (Table)request.getData();
				client.sendToClient(StaffController.updateTable(tableToUpdate, guiController));
				break;
			case VERIFY_MEMBER_ARRIVAL:
				String cardCode = (String)request.getData();
				client.sendToClient(StaffController.verifyMemberArrival(cardCode, guiController));
				break;
			case GET_WAITING_LIST:
				List<Visit> currentQueue = GetCommands.getWaitingList(guiController);
				BistroMessage response = new BistroMessage(Action.GET_WAITING_LIST, currentQueue);
				try {
                    client.sendToClient(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
				break;
			case CHECK_IN_CUSTOMER:
				if(request.getData() instanceof Reservation) {
					client.sendToClient(
							VisitController.createReservatedVisit((Reservation)request.getData(), guiController));
				} else { //Visit
					client.sendToClient(VisitController.createWalkInVisit((Visit)request.getData(), guiController));
				}
				break;
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
			case FORGOT_CODE:
				client.sendToClient(ReservationController.recoverLostCode((String)request.getData(), guiController));
				break;
			case GET_RESTAURANT_CONFIG:
				client.sendToClient(new BistroMessage(Action.GET_RESTAURANT_CONFIG,
						GetCommands.getRestaurantConfig(guiController)));
				break;

			case UPDATE_RESTAURANT_CONFIG:
				boolean updated = UpdateCommands.updateRestaurantConfig((RestaurantConfig) request.getData(), guiController);
				client.sendToClient(new BistroMessage(Action.UPDATE_RESTAURANT_CONFIG, updated));
				break;
			case GET_ALL_RESERVATIONS:
		        client.sendToClient(ReservationController.getAllReservations(guiController));
		        break;
                
			// --- MEMBER ROUTES ---
			case MEMBER_IDENTIFICATION:
				Member memberRecieved = (Member)request.getData();
				client.sendToClient(GuestController.memberIdentification(memberRecieved, guiController));
				break;
			case CREATE_MEMBER:
				Member memberToCreate = (Member)request.getData();
				client.sendToClient(GuestController.memberCreation(memberToCreate, guiController));
				break;
			case DELETE_MEMBER:
				Member memberToDelete = (Member)request.getData();
				client.sendToClient(GuestController.memberDelete(memberToDelete, guiController));
				break;
			case UPDATE_MEMBER:
				client.sendToClient(GuestController.updateMemberDetails((Member)request.getData(),guiController));
				break;
                
			// --- RESERVATION ROUTES ---
			case GET_RESERVATION:
				Integer resId = (Integer)request.getData();
				client.sendToClient(ReservationController.getReservation(resId, guiController));
				break;

			case CREATE_RESERVATION:
				Reservation reservationToCreate = (Reservation)request.getData();
				client.sendToClient(ReservationController.createReservation(reservationToCreate, guiController));
				break;
			case UPDATE_RESERVATION:
				Reservation resToUpdate = (Reservation) request.getData();
				client.sendToClient(ReservationController.updateReservation(resToUpdate, guiController));
				break;
			case CANCEL_RESERVATION:
				Reservation resToCancel = (Reservation) request.getData();
				client.sendToClient(ReservationController.cancelReservation(resToCancel, guiController));
				break;
			case GET_MEMBER_RESERVATIONS:
				String phoneNumber= (String)request.getData();
				client.sendToClient(ReservationController.getMemberReservations(phoneNumber, guiController));
				break;
			case CHECK_RESERVATION_AVAILABILITY:
				Reservation resForArrangement = (Reservation) request.getData();
				client.sendToClient(ReservationController.checkAvailability(resForArrangement, guiController));
				break;
			case GET_ALL_MEMBERS:
				List<Member> allMembers = GetCommands.getAllMembers(guiController);
				client.sendToClient(new BistroMessage(Action.GET_ALL_MEMBERS, allMembers));
				break;

			// --- VISIT ROUTES ---
			case START_VISIT:
				client.sendToClient(VisitController.updateVisit(request,guiController));
				break;
			case GET_MEMBER_VISITS:
				Integer memberId = (Integer) request.getData();
				List<Visit> memberVisits = GetCommands.getMemberVisits(memberId, guiController);
				client.sendToClient(new BistroMessage(Action.GET_MEMBER_VISITS, memberVisits));
				break;
			case GET_ACTIVE_VISITS:
				List<Visit> activeVisits = GetCommands.getActiveVisits(guiController);
				client.sendToClient(new BistroMessage(Action.GET_ACTIVE_VISITS, activeVisits));
				break;
			case VISIT_NOW:
				Visit toCreate = (Visit)request.getData();
				client.sendToClient(VisitController.createWalkInVisit(toCreate, guiController));
				break;
			case GET_VISIT:
            	client.sendToClient(GetCommands.getVisitByVerificationCode((String)request.getData(),guiController));
				break;
			case GET_VERIFICATION_CODE:
            case FIND_RESERVATION:
            	//Sends back instance of Reservation or Visit
            	client.sendToClient(ReservationController.codeVerification(request, guiController));
                break;
                
			// --- BILL ROUTES ---
			case UPDATE_BILL:
			case BILL_PAID:
				client.sendToClient(VisitController.updateBillOfVisit(request, guiController));
				break;
                
			// --- CLIENT DISCONNECTS ---
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

	@Override
	protected void serverStopped() {
		guiController.serverStatusChanged(false); // Update GUI to Red
		dbController.getInstance().disconnectFromDB();
		if (scheduler != null && !scheduler.isShutdown()) {
        scheduler.shutdown();
    	}
	}

	@Override
	protected void clientConnected(ConnectionToClient client) {
		guiController.addToConsole("Client connected: " + client.getInetAddress());
		guiController.updateClientList(client, "Connected");
	}

	@Override
	synchronized protected void clientDisconnected(ConnectionToClient client) {
		guiController.updateClientList(client, "Disconnected");
	}
}