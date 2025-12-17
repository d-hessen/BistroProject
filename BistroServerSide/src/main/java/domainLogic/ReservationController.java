package domainLogic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dataLayer.Reservation;
import databaseController.dbController;
import gui.ServerFrameController;

public class ReservationController {
	
	// Retrieve a reservation by ID
    public Reservation getReservation(int id, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        
        String sql = "SELECT * FROM reservation WHERE reservation_number = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Reservation(
                        rs.getInt("reservation_number"),
                        rs.getString("reservation_date"),
                        rs.getInt("number_of_guests"),
                        rs.getInt("verification_code"),
                        rs.getString("date_of_placing_reservation"),
                        rs.getInt("member_id")
                    );
                }
                else {
					guiController.addToConsole("Reservation not found for ID: " + id);
					return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reservation: " + e.getMessage());
        }
        return null;
    }
    
 // Update an existing reservation
    public boolean updateReservation(Reservation res) {
        Connection conn = dbController.getInstance().getConnection();
        String sql = "UPDATE reservation SET reservation_date = ?, number_of_guests = ? WHERE reservation_number = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, res.getReservationDate());
            ps.setInt(2, res.getNumberOfGuests());
            ps.setInt(3, res.getReservationId());
            
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating reservation: " + e.getMessage());
            return false;
        }
    }

}
