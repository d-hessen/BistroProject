package databaseController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import dataLayer.Member;
import dataLayer.Table;
import domainLogic.ServerFrameController;
/**
 * Handle sql delete commands
 */
public class DeleteCommands {
	
	/**
	 * Delete reservation from db by id
	 * @param id reservation id
	 * @param guiController logging controller for server console
	 * @return true if deleted succeessfully false otherwise
	 */
	public static boolean deleteReservation(Integer id, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
	    String sql = "DELETE FROM reservation WHERE reservation_number = ?";
	        
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, id);
	        return ps.executeUpdate() > 0;
	    } catch (SQLException e) {
	    	guiController.addToConsole("Error deleting reservation: " +id+ ". Error: " +e.getMessage());
	        return false;
	    }
	  }
	
	/**
	 * Delete member fron db
	 * @param memberToDelete member object to delete
	 * @param guiController controller for logging
	 * @return true if success, otherwise false
	 */
	public static boolean deleteMember(Member memberToDelete, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		String sql = "DELETE FROM members WHERE member_id = ?";
		Integer id = memberToDelete.getMemberId();
		
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			guiController.addToConsole("Error deleting member: " + id + ". Error: " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Delete table from db
	 * @param tableToDelete table object to delete
	 * @param guiController controller for logging 
	 * @return true if success, otherwise false
	 */
	public static boolean deleteTable(Table tableToDelete, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
	    String sql = "DELETE FROM tables WHERE table_number = ?";
	    Integer id = tableToDelete.getTableNumber();
	    
	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, id);
	        return ps.executeUpdate() > 0;
	    } catch (SQLException e) {
	    	guiController.addToConsole("Error deleting table: " +id+ ". Error: " +e.getMessage());
	        return false;
	    }
	}
	
	/**
	 * Delete visit in waiting_list
	 * @param waitingId id of waiting visit
	 * @param guiController logging controller
	 * @return true if success, otherwise false
	 */
    public static boolean deleteWaitingListEntry(int waitingId, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        String sql = "DELETE FROM waiting_list WHERE waiting_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, waitingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            guiController.addToConsole("Error deleting from waiting list: " + e.getMessage());
            return false;
        }
    }
}
