package domainLogic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dataLayer.Guest;
import databaseController.dbController;

public class GuestController {
	
	//Get guest by phone number
	public Guest getGuest(int phone) {
        Connection conn = dbController.getInstance().getConnection();
        
        String sql = "SELECT guest_full_name,email FROM reservation WHERE guest_phone = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Guest(
                        rs.getInt("guest_phone"),
                        rs.getString("email"),
                        rs.getString("guest_full_name")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching guest: " + e.getMessage());
        }
        return null;
    }

}
