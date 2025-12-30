package server;

import java.io.*;

import databaseController.dbController;
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
          	// --- MEMBER ROUTES ---
          	case MEMBER_IDENTIFICATION:
          		Member memberRecieved = (Member)request.getData();
          		BistroMessage identificationResult = GuestController.memberIdentification(memberRecieved, guiController);
          		client.sendToClient(identificationResult);
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
            // --- RESERVATION ROUTES ---
            case GET_RESERVATION:
            	// Data is an Integer (ID)
                Integer resId = Integer.parseInt((String)request.getData()); 
                BistroMessage reservation = ReservationController.getReservation(resId, guiController);
                client.sendToClient(reservation);
                break;
            case UPDATE_RESERVATION:
                //Data is a Reservation Object
                Reservation resToUpdate = (Reservation) request.getData();
                // Send back success/failure
                client.sendToClient(ReservationController.updateReservation(resToUpdate)); 
                break;

            default:
                  System.out.println("Unknown Action: " + request.getAction());
          }
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
   
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  @Override
  protected void serverStarted()
  {
	  dbController.getInstance();
      guiController.addToConsole("SQL connection succeed");
      guiController.addToConsole("Server listening on port " + getPort());
      guiController.serverStatusChanged(true);
  }

  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  @Override
  protected void serverStopped()  {
	guiController.serverStatusChanged(false); // Update GUI to Red
	dbController.getInstance().disconnectFromDB();
  }
  /**
   * This method overrides the one in the superclass.  
   * Called when client connects to server.
   */
  @Override
  protected void clientConnected(ConnectionToClient client) {
      guiController.addToConsole("Client connected: " + client.getInetAddress());
      guiController.updateClientList(client, "Connected");
  }
  /**
   * This method overrides the one in the superclass.  
   * Called when client disconnects from server.
   */
  @Override
  synchronized protected void clientDisconnected(ConnectionToClient client) {
      guiController.updateClientList(client, "Disconnected");
  }
}
