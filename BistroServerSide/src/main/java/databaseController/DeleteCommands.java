package databaseController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import domainLogic.ServerFrameController;

public class DeleteCommands {
	//======================================
	//OPERATIONS WITH RESERVATION
	//======================================
	//Method to delete Reservation with @id in database
	public boolean deleteReservation(Integer id, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
	    //SQL QUERY TO DELETE RESERVATION CHECK FIELDS IN DATABASE BEFORE CHANGE
	    String sql = "DELETE FROM reservation WHERE reservation_number = ?";
	        
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, id);
	        int executionResult = ps.executeUpdate();
	        return executionResult > 0;
	    } catch (SQLException e) {
	    	guiController.addToConsole("Error deleting reservation: " +id+ ". Error: " +e.getMessage());
	        return false;
	        }
	    }
}
