package databaseController;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import common.BistroMessage;
import dataLayer.*;
import domainLogic.ServerFrameController;
/**
 * Handle all sql updates
 */
public class UpdateCommands {
	//======================================
	//RESERVATION UPDATES
	//======================================
    
	/**
	 * Update existing reservation details: (Date,Guest,Status)
	 * @param resToUpdate reservation object with updated data
	 * @param guiController logging controller
	 * @return true if successful, false otherwise
	 */
    public static boolean updateReservation(Reservation resToUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        String sql = "UPDATE reservation SET reservation_date = ?, number_of_guests = ?, status = ? WHERE reservation_number = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resToUpdate.getReservationDate().getDate());
            ps.setInt(2, resToUpdate.getNumberOfGuests());
            ps.setString(3, resToUpdate.getStatus().name());
            ps.setInt(4, resToUpdate.getReservationId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating reservation: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Flag reservation that reminder was sent to avoid duplicate sending
     * @param reservationId
     * @param guiController
     * @return
     */
    public static boolean markReminderSent(Integer reservationId, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        String sql = "UPDATE reservation SET reminder_sent = TRUE WHERE reservation_number = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            guiController.addToConsole("Error marking reminder sent: " + e.getMessage());
            return false;
        }
    }
	
    //======================================
	//MEMBER UPDATES
	//======================================

    /**
     * Update member details: (Phone,Email)
     * @param memberToUpdate member object with updated details
     * @param guiController logging controller
     * @return updated member object from DB, if failed null
     */
    public static Member updateMember(Member memberToUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        String sql = "UPDATE members SET phone = ?, email = ? WHERE member_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, memberToUpdate.getPhoneNumber());
            ps.setString(2, memberToUpdate.getEmail());
            ps.setInt(3, memberToUpdate.getMemberId());
            
            if(ps.executeUpdate() > 0) {
            	return GetCommands.getMemberById(memberToUpdate.getMemberId(), guiController);            }
        } catch (SQLException e) {
        	guiController.addToConsole("Error updating member: " + e.getMessage());
        }
        return null;
    }   
	
    //======================================
	//TABLE UPDATES
	//======================================

    /**
     * Update table details: (capacity,isActive,status)
     * @param tableToUpdate table object with updated details
     * @param guiController logging controller
     * @return updated table project on success, on fail null
     */
    public static Table updateTable(Table tableToUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        String sql = "UPDATE tables SET is_active = ?, capacity = ? WHERE table_number = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, tableToUpdate.isActive());
            ps.setInt(2, tableToUpdate.getTableCapacity());
            ps.setInt(3, tableToUpdate.getTableNumber());
            
            if(ps.executeUpdate() > 0) {
            	return GetCommands.getTable(tableToUpdate.getTableNumber(), guiController);            }
        } catch (SQLException e) {
            guiController.addToConsole("Error updating table: " + e.getMessage());
        }
        return null;
    }

    //======================================
    //BILL & VISIT UPDATES
    //======================================

    /**
     * Update bill for specific visit
     * @param toUpdate visit object with updated details
     * @param guiController logging controller
     * @return updated visit object on success, null on fail
     */
    public static Visit updateBillForVisit(Visit toUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        String sql = "UPDATE bills SET total_amount = ?, discount_amount = ?, final_amount = ?, is_paid = ? WHERE visit_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        	Bill billToUpdate = toUpdate.getBillOfVisit();
            ps.setDouble(1, billToUpdate.getTotalAmount());
            ps.setDouble(2, billToUpdate.getDiscountAmount());
            ps.setDouble(3, billToUpdate.getFinalAmount());
            ps.setBoolean(4, billToUpdate.isPaid());
            ps.setInt(5, toUpdate.getVisitId());
            
            if(ps.executeUpdate() > 0) {
            	return GetCommands.getVisit(toUpdate.getVisitId(), guiController);            }
        } catch (SQLException e) {
        	guiController.addToConsole("Error updating bill: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Flags bill that was sent to client
     * @param visitId visit id in db
     * @param guiController logging controller
     */
    public static void markBillSent(Integer visitId, ServerFrameController guiController) {
        String sql = "UPDATE visits SET bill_sent = TRUE WHERE visit_id = ?";
        try (PreparedStatement ps = dbController.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, visitId);
            ps.executeUpdate();
        } catch (SQLException e) {
        	guiController.addToConsole("Error marking bill sent: " + e.getMessage());
        }
    }
    
    /**
     * Update visit status based on Action in BistroMessage.
     * Handle START_VISIT and BILL_PAID actions.
     * @param toUpdate BistroMessage object containing action: (START_VISIT/BILL_PAID) and data as Visit
     * @param guiController logging controller
     * @return true on success, false on fail
     */
    public static boolean updateVisit(BistroMessage toUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
    	
    	if (!(toUpdate.getData() instanceof Visit)) {
            guiController.addToConsole("UpdateCommands.updateVisit() expects a Visit object.");
            return false;
        }
    	
    	Visit received = (Visit)toUpdate.getData();
    	String sql = "";
    	
    	switch (toUpdate.getAction()) {
        case START_VISIT:
            sql = "UPDATE visits SET start_time = NOW() WHERE visit_id = ?";
            break;
        case BILL_PAID:
            sql = "UPDATE visits SET end_time = NOW(), is_active = 0 WHERE visit_id = ?";
            break;
        default:
            guiController.addToConsole("Unknown Action for updateVisit: " + toUpdate.getAction());
            return false;
    	}
    	
    	try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, received.getVisitId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            guiController.addToConsole("Error updating visit status: " + e.getMessage());
            return false;
        }
    }
    
    //======================================
    //WAITING LIST UPDATES
    //======================================
    
    /**
     * Update waiting visit with assigned table and mark it as notified or seated
     * @param waitingId visit object waiting id in waiting_list table
     * @param status notified/seated
     * @param tableId table id to be assigned for visit
     * @param guiController logging controller
     * @return on success true, otherwise false
     */
    public static boolean updateWaitingListStatus(int waitingId, String status, int tableId, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        String sql = "UPDATE waiting_list SET status = ?, table_id = ?, notified_at = NOW() WHERE waiting_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, tableId);
            ps.setInt(3, waitingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            guiController.addToConsole("Error updating waiting list: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update waiting visit based on visit object. Use to move waiting visit from waiting to regular visit or clearing the table
     * @param toUpdate visit object with updated details
     * @param guiController logging controller
     * @return Visit object on success, null on failure
     */
    public static Visit updateVisitInWaitingList(Visit toUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        String sql = "UPDATE waiting_list SET status = ?, notified_at = NOW() WHERE visit_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        	if(toUpdate.isActive()) {
            	ps.setString(1, "seated");
        	} else {
        		ps.setObject(1, null);
        	}
        	ps.setObject(2, toUpdate.getWaitingId());
            if(ps.executeUpdate() > 0) {
            	return GetCommands.getVisit(toUpdate.getVisitId(), guiController);            }
        } catch (SQLException e) {
        	guiController.addToConsole("Error updating waiting list visit: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Cancel waiting list visit (if guest doesn't show up 15 mins after being notified)
     * @param waitingId visit waiting id in waiting_list
     * @param guiController logging controller
     * @return true on success, false on failure
     */
    public static boolean cancelWaitingListEntry(int waitingId, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        String sql = "UPDATE waiting_list SET status = 'cancelled', table_id = NULL WHERE waiting_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, waitingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            guiController.addToConsole("Error cancelling waiting list entry " + waitingId + ": " + e.getMessage());
            return false;
        }
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
