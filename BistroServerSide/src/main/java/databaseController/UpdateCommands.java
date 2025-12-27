package databaseController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import dataLayer.Reservation;

public class UpdateCommands {
	//======================================
	//OPERATIONS WITH RESERVATION
	//======================================
    //Update an existing reservation
    //IN RESERVATION FIELDS THAT CAN BE UPDATED ARE: numberOfGuests, reservationDate, status
    public static boolean updateReservation(Reservation resToUpdate) {
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
}
