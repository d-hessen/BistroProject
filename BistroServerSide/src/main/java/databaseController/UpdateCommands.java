package databaseController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import common.BistroMessage;
import dataLayer.*;
import domainLogic.ServerFrameController;

public class UpdateCommands {
	//======================================
	//RESERVATION UPDATES
	//======================================
    //Update an existing reservation
    //IN RESERVATION FIELDS THAT CAN BE UPDATED ARE: numberOfGuests, reservationDate, status
    public static boolean updateReservation(Reservation resToUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        //SQL QUERY TO UPDATE RESERVATION CHECK FIELDS IN DATABASE BEFORE CHANGE
        String sql = "UPDATE reservation SET reservation_date = ?, number_of_guests = ?, status = ? WHERE reservation_number = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resToUpdate.getReservationDate().getDate());
            ps.setInt(2, resToUpdate.getNumberOfGuests());
            ps.setString(3, resToUpdate.getStatus().name());
            ps.setInt(4, resToUpdate.getReservationId());
            
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating reservation: " + e.getMessage());
            return false;
        }
    }
	//======================================
	//MEMBER UPDATES
	//======================================
    //Update an existing reservation
    //IN RESERVATION FIELDS THAT CAN BE UPDATED ARE: numberOfGuests, reservationDate, status
    public static boolean updateMember(Member memberToUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        //SQL QUERY TO UPDATE TABLE CHECK FIELDS IN DATABASE BEFORE CHANGE
        String sql = "UPDATE members SET full_name = ?, phone = ?, email = ?, password = ?, WHERE table_number = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(0, memberToUpdate.getFullName());
            ps.setString(1, memberToUpdate.getPhoneNumber());
            ps.setString(2, memberToUpdate.getEmail());
            ps.setString(3, memberToUpdate.getPassword());
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating member: " + e.getMessage());
            return false;
        }
    }   
	//======================================
	//TABLE UPDATES
	//======================================
    //Update an existing reservation
    //IN RESERVATION FIELDS THAT CAN BE UPDATED ARE: numberOfGuests, reservationDate, status
    public static boolean updateTable(Table tableToUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        //SQL QUERY TO UPDATE TABLE CHECK FIELDS IN DATABASE BEFORE CHANGE
        String sql = "UPDATE tables SET is_active = ?, capacity = ? WHERE table_number = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, tableToUpdate.isActive());
            ps.setInt(2, tableToUpdate.getTableCapacity());
            ps.setInt(3, tableToUpdate.getTableNumber());
            
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating table: " + e.getMessage());
            return false;
        }
    }
	//======================================
	//BILL UPDATES
	//======================================
    //Update an existing reservation
    //IN RESERVATION FIELDS THAT CAN BE UPDATED ARE: numberOfGuests, reservationDate, status
    public static boolean updateBillForVisit(Integer visitId, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        //SQL QUERY TO UPDATE TABLE CHECK FIELDS IN DATABASE BEFORE CHANGE
        String sql = "UPDATE bills SET total_amount = ?, discount_amount = ?, final_amount = ?, is_paid = ? WHERE visit_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        	Bill billToUpdate = GetCommands.getBill(visitId, guiController);
            ps.setDouble(0, billToUpdate.getTotalAmount());
            ps.setDouble(1, billToUpdate.getDiscountAmount());
            ps.setDouble(2, billToUpdate.getFinalAmount());
            ps.setBoolean(3, billToUpdate.isPaid());
            ps.setInt(4, visitId);
            
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating bill: " + e.getMessage());
            return false;
        }
    }
    
	//======================================
	//VISIT UPDATES
	//======================================
    //Update Visit according to requeted action
    public static boolean updateVisit(BistroMessage toUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
    	
    	if(toUpdate.getData() instanceof Visit) {
    		Visit recieved = (Visit)toUpdate.getData();
    		Integer affectedRows = null;
        	switch (toUpdate.getAction()) {
    		case START_VISIT:
    			String sql = "UPDATE visits SET start_time = NOW() WHERE visit_id = ?";
    	        try (PreparedStatement ps = conn.prepareStatement(sql)) {
    	        	ps.setInt(1, recieved.getVisitId());
    	            affectedRows = ps.executeUpdate();
    	        } catch (SQLException e) {
    	            guiController.addToConsole("Error updating visit: " + e.getMessage());
    	        }
    			break;

    		default:
    			guiController.addToConsole(toUpdate.getAction() + " - UNKNOW ACTION IN UPDATE VISIT");
    			break;
    		}
	        return affectedRows != null;
    	} else {
    		guiController.addToConsole("UpdateCommands.updateVisit() CAN RECIEVE ONLY VISIT TYPE");
    		return false;
    	}
    }
}
