package server;

import java.io.*;
import java.util.List;

import databaseController.GetCommands;
import databaseController.dbController;
import domain.WaitingList;
import domainLogic.*;
import common.*;
import dataLayer.*;
import ocsf.server.*;

public class BistroServer extends AbstractServer 
{
  private ServerFrameController guiController; // Reference to the GUI Controller

  public BistroServer(int port, ServerFrameController controller) 
  {
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
                String cardCode = (String) request.getData();
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
                	client.sendToClient(VisitController.createReservatedVisit((Reservation)request.getData(), guiController));
            	}
            	else {//Visit
            		client.sendToClient(VisitController.createWalkInVisit((Visit)request.getData(), guiController));
            	}
            	break;
            case REMOVE_FROM_WAITING_LIST:
                if (request.getData() instanceof Integer) {
                    int waitingIdToRemove = (Integer) request.getData();
                    boolean success = databaseController.DeleteCommands.deleteWaitingListEntry(waitingIdToRemove, guiController);
                    
                    if (success) {
                        guiController.addToConsole("Removed waiting ID: " + waitingIdToRemove);
                        client.sendToClient(new BistroMessage(Action.REMOVE_FROM_WAITING_LIST, true));
                    } else {
                        client.sendToClient(new BistroMessage(Action.REMOVE_FROM_WAITING_LIST, false));
                    }
                }
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
                // Data is a Reservation Object
                Reservation resToUpdate = (Reservation) request.getData();
                // Send back success/failure
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
            case GET_VERIFICATION_CODE:
            case FIND_RESERVATION:
            	//Sends back instance of Reservation or Visit
            	client.sendToClient(ReservationController.codeVerification(request, guiController));
                break;
            // --- BILL ROUTES ---
            case UPDATE_BILL:
            	client.sendToClient(VisitController.updateBillOfVisit((Visit)request.getData(), guiController));
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
  protected void serverStarted()
  {
	  dbController.getInstance();
      guiController.addToConsole("SQL connection succeed");
      guiController.addToConsole("Server listening on port " + getPort());
      guiController.serverStatusChanged(true);
  }

  @Override
  protected void serverStopped()  {
	guiController.serverStatusChanged(false); // Update GUI to Red
	dbController.getInstance().disconnectFromDB();
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