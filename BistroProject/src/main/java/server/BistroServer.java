// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 
package server;

import java.io.*;

import dataLayer.Reservation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */

public class BistroServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  //final public static int DEFAULT_PORT = 5555;
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   * 
   */
 private static Connection conn;

  public BistroServer(int port) 
  {
    super(port);
  }

  //Instance methods ************************************************
  
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   * @param 
   */
  public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		try {
			if (msg instanceof Integer) {
				Integer id = (Integer) msg;
				PreparedStatement ps = conn.prepareStatement("SELECT * FROM reservation WHERE reservation_number = ?");
				ps.setInt(1, id);
				ResultSet rs = ps.executeQuery();

				if (rs.next()) {
					Reservation reservation = new Reservation(rs.getInt("reservation_number")
	                		,rs.getString("reservation_date")
	                		,rs.getInt("number_of_guests")
	                		,rs.getInt("verification_code")
	                		,rs.getString("date_of_placing_order")
	                		,rs.getInt("member_id"));
					System.out.println("Reservation found");
					client.sendToClient(reservation);
				} 
				else {
					System.out.println("Reservation not found");
					String errorReservation = "Error";
					client.sendToClient(errorReservation);
				}
			}
			
			else if (msg instanceof Reservation) {
				Reservation updatedReservation = (Reservation) msg;
				System.out.println("Updating Reservation: " + updatedReservation.getReservationId());

				PreparedStatement ps = conn.prepareStatement(
					"UPDATE reservation SET reservation_date = ?, number_of_guests = ? WHERE reservation_number = ?"
				);
				
				ps.setString(1, updatedReservation.getReservationDate());
				ps.setInt(2, updatedReservation.getNumberOfGuests());

				int result = ps.executeUpdate();
				System.out.println("Update Result: " + result); 
				client.sendToClient("Updated");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
   
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
	  try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bistro?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false", "root", "danhessen");
			//Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.3.68/test","root","Root");
			System.out.println("SQL connection succeed");
			
		  }
		  catch(SQLException ex) {
			  System.out.println("SQLException: " + ex.getMessage());
	          System.out.println("SQLState: " + ex.getSQLState());
	          System.out.println("VendorError: " + ex.getErrorCode());
		  }
	  System.out.println ("Server listening for connections on port " + getPort());
  }
  
  public Reservation getReservation(int reservationNumber) {
	    String sql = "SELECT * FROM reservation WHERE reservation_number = ?";
	    
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, reservationNumber);

	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                return new Reservation(rs.getInt("reservation_number")
	                		,rs.getString("reservation_date")
	                		,rs.getInt("number_of_guests")
	                		,rs.getInt("verification_code")
	                		,rs.getString("date_of_placing_order")
	                		,rs.getInt("member_id"));
	            }
	        }
	    }
	    catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()  {
    System.out.println ("Server has stopped listening for connections.");
    try {
        if (conn != null)
            conn.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
  }  
}
//End of EchoServer class
