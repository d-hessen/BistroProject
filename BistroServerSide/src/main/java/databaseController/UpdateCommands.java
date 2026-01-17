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
 * Handles all SQL UPDATE operations in the database.
 * This class is responsible for updating reservations, members, tables, visits, bills,
 * waiting list entries, and restaurant configuration.
 */
public class UpdateCommands {
	//======================================
	//RESERVATION UPDATES
	//======================================
    
	/**
	 * Updates existing reservation details in the database.
	 * Updates the date, number of guests, and status.
	 *
	 * @param resToUpdate the reservation object containing the updated data
	 * @param guiController reference to the server GUI controller for logging errors
	 * @return true if the update was successful, false otherwise
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
     * Marks a reservation as having received a reminder to avoid duplicate notifications.
     * Sets the 'reminder_sent' flag to TRUE in the database.
     *
     * @param reservationId the ID of the reservation
     * @param guiController reference to the server GUI controller for logging errors
     * @return true if the update was successful, false otherwise
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
     * Updates a member's contact details (Phone and Email).
     *
     * @param memberToUpdate the member object containing updated details
     * @param guiController reference to the server GUI controller for logging errors
     * @return the updated Member object retrieved from the DB, or null if the update failed
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
     * Updates table details including active status and capacity.
     *
     * @param tableToUpdate the table object containing updated details
     * @param guiController reference to the server GUI controller for logging errors
     * @return the updated Table object on success, or null on failure
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
     * Updates the bill information for a specific visit.
     * Updates total amount, discount, final amount, and payment status.
     *
     * @param toUpdate the visit object containing the updated bill
     * @param guiController reference to the server GUI controller for logging errors
     * @return the updated Visit object on success, or null on failure
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
     * Marks a bill as sent to the client.
     * Updates the 'bill_sent' flag in the visits table.
     *
     * @param visitId the ID of the visit
     * @param guiController reference to the server GUI controller for logging errors
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
     * Updates visit status/timestamps based on the Action provided in the message.
     * Handles START_VISIT (sets start_time) and BILL_PAID (sets end_time and inactive).
     *
     * @param toUpdate BistroMessage object containing the Action (START_VISIT/BILL_PAID) and the Visit object
     * @param guiController reference to the server GUI controller for logging errors
     * @return true on success, false on failure or invalid data
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
     * Updates a waiting list entry with an assigned table and status.
     * Used when notifying a waiting customer that their table is ready.
     *
     * @param waitingId the ID of the waiting list entry
     * @param status the new status (e.g., "notified", "seated")
     * @param tableId the ID of the table assigned to the visit
     * @param guiController reference to the server GUI controller for logging errors
     * @return true on success, false otherwise
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
     * Updates a waiting list entry to 'seated' status.
     * This effectively moves the customer from the waiting state to an active visit.
     *
     * @param toUpdate the visit object containing the waiting ID
     * @param guiController reference to the server GUI controller for logging errors
     * @return the updated Visit object on success, or null on failure
     */
    public static Visit updateVisitInWaitingList(Visit toUpdate, ServerFrameController guiController) {
    	Connection conn = dbController.getInstance().getConnection();
        String sql = "UPDATE waiting_list SET status = ?, notified_at = NOW() WHERE waiting_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        	ps.setString(1, "seated");
        	ps.setObject(2, toUpdate.getWaitingId());
            if(ps.executeUpdate() > 0) {
            	return GetCommands.getVisit(toUpdate.getVisitId(), guiController);            }
        } catch (SQLException e) {
        	guiController.addToConsole("Error updating waiting list visit: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Cancels a waiting list entry.
     * Used when a guest does not show up within the time limit after being notified.
     *
     * @param waitingId the ID of the waiting list entry
     * @param guiController reference to the server GUI controller for logging errors
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
    
    /**
     * Updates the restaurant configuration (Regular and Special hours).
     * Performs a transaction to update regular hours and replace special hours.
     *
     * @param config the RestaurantConfig object containing new hours
     * @param guiController reference to the server GUI controller for logging errors
     * @return true if the transaction was committed successfully, false otherwise
     */
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