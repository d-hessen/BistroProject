package server;

import java.io.*;

import databaseController.dbController;
import domainLogic.ReservationController;
import gui.ServerFrameController;
import common.Action;
import common.BistroMessage;
import dataLayer.Reservation;
import ocsf.server.*;

public class BistroServer extends AbstractServer 
{
 private ServerFrameController guiController; // Reference to the GUI Controller
 private ReservationController resController;


  public BistroServer(int port, ServerFrameController controller) 
  {
    super(port);
    this.guiController = controller;
    this.resController = new ReservationController();
    
  }
  @Override
  public void handleMessageFromClient(Object msg, ConnectionToClient client) {
      BistroMessage request = (BistroMessage) msg;
      try {
          switch (request.getAction()) {
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
                  int resId = Integer.parseInt((String)request.getData()); 
                  Reservation result = resController.getReservation(resId, guiController);
                  if(result == null) {
                	  client.sendToClient(new BistroMessage(Action.RESERVATION_NOT_FOUND,resId));
                  } else {
                	  client.sendToClient(new BistroMessage(Action.GET_RESERVATION, result));
                  }
                  break;

              case UPDATE_RESERVATION:
                  //Data is a Reservation Object
                  dataLayer.Reservation resToUpdate = (dataLayer.Reservation) request.getData();
                  boolean success = resController.updateReservation(resToUpdate);
                  // Send back success/failure
                  client.sendToClient(new BistroMessage(Action.UPDATE_RESERVATION, success)); 
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
