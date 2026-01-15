package databaseController;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

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
    public static Member updateMember(Member memberToUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        //SQL QUERY TO UPDATE TABLE CHECK FIELDS IN DATABASE BEFORE CHANGE
        String sql = "UPDATE members SET phone = ?, email = ? WHERE member_id = ?";
        Member toReturn = null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, memberToUpdate.getPhoneNumber());
            ps.setString(2, memberToUpdate.getEmail());
            ps.setInt(3, memberToUpdate.getMemberId());
            int result = ps.executeUpdate();
            if(result>0) {
            	toReturn = GetCommands.getMemberById(memberToUpdate.getMemberId(), guiController);
            }
        } catch (SQLException e) {
        	guiController.addToConsole("Error updating member: " + e.getMessage());
        }
        return toReturn;
    }   
	//======================================
	//TABLE UPDATES
	//======================================
    //Update an existing reservation
    //IN RESERVATION FIELDS THAT CAN BE UPDATED ARE: numberOfGuests, reservationDate, status
    public static Table updateTable(Table tableToUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        //SQL QUERY TO UPDATE TABLE CHECK FIELDS IN DATABASE BEFORE CHANGE
        String sql = "UPDATE tables SET is_active = ?, capacity = ? WHERE table_number = ?";
        Table toReturn = null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, tableToUpdate.isActive());
            ps.setInt(2, tableToUpdate.getTableCapacity());
            ps.setInt(3, tableToUpdate.getTableNumber());
            
            int result = ps.executeUpdate();
            if(result > 0) {
            	toReturn = GetCommands.getTable(tableToUpdate.getTableNumber(), guiController);
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error updating table: " + e.getMessage());
        }
        return toReturn;
    }
	//======================================
	//BILL UPDATES
	//======================================
    //Update Bill for specific Visit in DB
    public static Visit updateBillForVisit(Visit toUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        //SQL QUERY TO UPDATE TABLE CHECK FIELDS IN DATABASE BEFORE CHANGE
        String sql = "UPDATE bills SET total_amount = ?, discount_amount = ?, final_amount = ?, is_paid = ? WHERE visit_id = ?";
        Visit toReturn = null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        	Bill billToUpdate = toUpdate.getBillOfVisit();
            ps.setDouble(1, billToUpdate.getTotalAmount());
            ps.setDouble(2, billToUpdate.getDiscountAmount());
            ps.setDouble(3, billToUpdate.getFinalAmount());
            ps.setBoolean(4, billToUpdate.isPaid());
            ps.setInt(5, toUpdate.getVisitId());
            
            int result = ps.executeUpdate();
            if(result > 0) {
            	toReturn = GetCommands.getVisit(toUpdate.getVisitId(), guiController);
            }
        } catch (SQLException e) {
        	guiController.addToConsole("Error updating bill: " + e.getMessage());
        }
        return toReturn;
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
    
    public static Visit updateVisitInWaitingList(Visit toUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        //SQL QUERY TO UPDATE TABLE CHECK FIELDS IN DATABASE BEFORE CHANGE
        String sql = "UPDATE waiting_list SET status = ?, notified_at = NOW() WHERE visit_id = ?";
        Visit toReturn = null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        	if(toUpdate.isActive()) {
            	ps.setString(1, "seated");
        	} else {
        		ps.setObject(1, null);
        	}
            int result = ps.executeUpdate();
            if(result > 0) {
            	toReturn = GetCommands.getVisit(toUpdate.getVisitId(), guiController);
            }
        } catch (SQLException e) {
        	guiController.addToConsole("Error updating bill: " + e.getMessage());
        }
        return toReturn;
    }
    
    public static boolean updateRestaurantConfig(RestaurantConfig config, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        try {
            conn.setAutoCommit(false); // Start transaction

            // Update Regular Hours
            String updateReg = "UPDATE regular_hours SET open_time=?, close_time=? WHERE day_name=?";
            try (PreparedStatement ps = conn.prepareStatement(updateReg)) {
                for (String day : config.getRegularHours().keySet()) {
                    String[] times = config.getRegularHours().get(day);
                    ps.setString(1, times[0]);
                    ps.setString(2, times[1]);
                    ps.setString(3, day);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            
            // Clear old Special Hours
            String deleteSpec = "DELETE FROM special_hours";
            try (PreparedStatement ps = conn.prepareStatement(deleteSpec)) {
                ps.executeUpdate();
            }

            // Insert the new list of Special Hours
            String insertSpec = "INSERT INTO special_hours (special_date, open_time, close_time) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSpec)) {
                for (LocalDate date : config.getSpecialHours().keySet()) {
                    String[] times = config.getSpecialHours().get(date);
                    ps.setDate(1, Date.valueOf(date));
                    ps.setString(2, times[0]);
                    ps.setString(3, times[1]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }            
            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            try { 
                conn.rollback(); 
            } catch (SQLException ex) { 
                ex.printStackTrace(); 
            }
            guiController.addToConsole("Error updating config: " + e.getMessage());
            return false;
        }
    }
}
