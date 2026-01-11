package databaseController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import dataLayer.Member;
import dataLayer.Table;
import domainLogic.ServerFrameController;

public class DeleteCommands {
	//======================================
	//RESERVATION DELETE
	//======================================
	//Method to delete Reservation with @id in database
	public static boolean deleteReservation(Integer id, ServerFrameController guiController) {
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
	//Method to delete Member
	public static boolean deleteMember(Member memberToDelete, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		
        //SQL QUERY TO DELETE MEMBER
		String sql = "DELETE FROM members WHERE member_id = ?";
		Integer id = memberToDelete.getMemberId();
		
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			int executionResult = ps.executeUpdate();
			return executionResult > 0;
		} catch (SQLException e) {
			guiController.addToConsole("Error deleting member: " + id + ". Error: " + e.getMessage());
			return false;
		}
	}
	
	//======================================
	//TABLE DELETE
	//======================================
	public static boolean deleteTable(Table tableToDelete, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
	    //SQL QUERY TO DELETE TABLE CHECK FIELDS IN DATABASE BEFORE CHANGE
	    String sql = "DELETE FROM tables WHERE table_number = ?";
	    Integer id = tableToDelete.getTableNumber();
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, id);
	        int executionResult = ps.executeUpdate();
	        return executionResult > 0;
	    } catch (SQLException e) {
	    	guiController.addToConsole("Error deleting table: " +id+ ". Error: " +e.getMessage());
	        return false;
	        }
	}
	
	// ======================================
    // WAITING LIST DELETION
    // ======================================
    public static boolean deleteWaitingListEntry(int waitingId, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        String sql = "DELETE FROM waiting_list WHERE waiting_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, waitingId);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            guiController.addToConsole("Error deleting from waiting list: " + e.getMessage());
            return false;
        }
    }
}
