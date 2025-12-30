package databaseController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import dataLayer.Member;
import domainLogic.ServerFrameController;

public class DeleteCommands {
	//======================================
	//RESERVATION DELETE
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
	//======================================
	//MEMBER DELETE
	//======================================
	//Method to delete Reservation with @id in database
	public static boolean deleteMember(Member memberToDelete, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
	    //SQL QUERY TO DELETE RESERVATION CHECK FIELDS IN DATABASE BEFORE CHANGE
	    String sql = "DELETE FROM members WHERE member_id = ?";
	    Integer id = memberToDelete.getMemberId();
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, id);
	        int executionResult = ps.executeUpdate();
	        return executionResult > 0;
	    } catch (SQLException e) {
	    	guiController.addToConsole("Error deleting member: " +id+ ". Error: " +e.getMessage());
	        return false;
	        }
	}
}
