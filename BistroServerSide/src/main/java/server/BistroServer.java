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
            case CHECK_IN_CUSTOMER:
                String code = (String) request.getData();
                client.sendToClient(StaffController.checkInCustomer(code, guiController));
                break;
            case VERIFY_MEMBER_ARRIVAL:
                String cardCode = (String) request.getData();
                client.sendToClient(StaffController.verifyMemberArrival(cardCode, guiController));
                break;
            case GET_WAITING_LIST:
                List<Visit> currentQueue = WaitingList.getInstance().getWaitingListAsVisits();
                BistroMessage response = new BistroMessage(Action.GET_WAITING_LIST, currentQueue);
                try {
                    client.sendToClient(response);
                } catch (IOException e) {
                    e.printStackTrace();
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
            	System.out.println("1");
            	String phoneNumber= (String)request.getData();
            	System.out.println("1");
            	client.sendToClient(ReservationController.getMemberReservations(phoneNumber, guiController));
                break;
            case GET_VERIFICATION_CODE:
            	String ver_code = (String)request.getData();
            	client.sendToClient(ReservationController.codeVerification(ver_code, guiController));
                break;

            // --- VISIT ROUTES ---
            case CREATE_VISIT:
            	Visit visitToCreate = (Visit)request.getData();
            	client.sendToClient(VisitController.createVisitByReservation(visitToCreate, guiController));
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
            	client.sendToClient(VisitController.createVisit(toCreate, guiController));
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