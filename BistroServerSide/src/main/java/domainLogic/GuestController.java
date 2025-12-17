package domainLogic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dataLayer.Guest;
import databaseController.dbController;

public class GuestController {
	
	public Guest getGuest(int phone) {
        Connection conn = dbController.getInstance().getConnection();
        
        String sql = "SELECT * FROM guest WHERE phone_number = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Guest(
                        rs.getInt("phone_number"),
                        rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching guest: " + e.getMessage());
        }
        return null;
    }

}
