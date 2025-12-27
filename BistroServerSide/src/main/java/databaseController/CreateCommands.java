package databaseController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import dataLayer.DateTime;
import dataLayer.Guest;
import dataLayer.Member;
import dataLayer.Reservation;
import dataLayer.Staff;
import domainLogic.ServerFrameController;

public class CreateCommands {
	//======================================
	//RESTAURANT CONFIG CREATION
	//======================================
	//Create restaurant (METHOD TO BE WRITTEN)
	
	//======================================
	//RESERVATION CREATION
	//======================================
	//Method that creates Reservation in database and returns Reservation Number
	public Integer createReservation(Integer reservationId,String reservationDate, String reservationTime, Integer numberOfGuests, Integer memberId, Guest guest, ServerFrameController guiController) {
			Connection conn = dbController.getInstance().getConnection();
			
			Reservation resToCreate = new Reservation(reservationId, new DateTime(reservationDate,reservationTime), numberOfGuests, memberId, guest);
			//SQL QUERY TO INSERT RESERVATION CHECK FIELDS IN DATABASE BEFORE CHANGE
			String sql = "INSERT INTO reservation ("
					+ "reservation_number,"
					+ "reservation_date,"
					+ "number_of_guests, "
					+ "verification_code,"
					+ "member_id,"
					+ "guest_full_name,"
					+ "guest_phone,"
					+ "email,"
					+ "status"
					+ ") "
					+ "VALUES "
					+ "(?, ?, ?, ?, ?, ?, ?, ?, ?)";
			//Set values to query
			try (PreparedStatement ps = conn.prepareStatement(sql)){
				ps.setInt(1,resToCreate.getReservationId());
				ps.setString(2, resToCreate.getReservationDate().getDate());
				ps.setInt(3, resToCreate.getNumberOfGuests());
				ps.setInt(4, resToCreate.getVerificationCode());
				ps.setInt(5, resToCreate.getMemberId());
				ps.setString(6, resToCreate.getGuest().getFullName());
				ps.setString(7, resToCreate.getGuest().getPhoneNumber());
				ps.setString(8, resToCreate.getGuest().getEmail());
				ps.setString(9, resToCreate.getStatus().name());
				//Execute prepared query
				int executionResult = ps.executeUpdate();
				if(executionResult > 0) return resToCreate.getReservationId();
			} catch(SQLException e) {
				guiController.addToConsole("Error adding reservation for" +memberId+ " to database. Error: " +e.getMessage());
				e.printStackTrace();
			}
			return null;
	}
	//======================================
	//MEMBER CREATION
	//======================================
	public Integer createMember(String fullName, String phoneNumber, String email, String password, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();

		Member memToCreate = new Member(fullName, phoneNumber, email, password);
		
		String sql = "INSERT INTO members (member_id, full_name, phone, email, password, card_code) VALUES (?, ?, ?, ?, ?, ?)";
		//Set values to query
		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setInt(1,memToCreate.getMemberId());
			ps.setString(2, memToCreate.getFullName());
			ps.setString(3, memToCreate.getPhoneNumber());
			ps.setString(4, memToCreate.getEmail());
			ps.setString(5, memToCreate.getPassword());
			ps.setString(6, memToCreate.getCardCode());
			//Execute prepared query
			int executionResult = ps.executeUpdate();
			if(executionResult > 0) return memToCreate.getMemberId();
		} catch(SQLException e) {
			guiController.addToConsole("Error creating member: " +memToCreate.getFullName()+". Error: " +e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	//======================================
	//STAFF CREATION
	//======================================
	//Create staff member if succeeded return true otherwise false
	public boolean createStaff(String username, String password, String fullName, boolean isManager, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();

		Staff staffToCreate = new Staff(username, password, fullName, isManager);
		
		String sql = "INSERT INTO staff (username, password, full_name, role) VALUES (?, ?, ?, ?)";
		//Set values to query
		int executionResult = 0;
		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1,staffToCreate.getUsername());
			ps.setString(2, staffToCreate.getPassword());
			ps.setString(3, staffToCreate.getFullName());
			if(isManager) {
				ps.setString(4, "manager");
			}
			else {
				ps.setString(4, "worker");
			}
			//Execute prepared query
			executionResult = ps.executeUpdate();
		} catch(SQLException e) {
			guiController.addToConsole("Error creating staff: " +staffToCreate.getFullName()+". Error: " +e.getMessage());
			e.printStackTrace();
		}
		return executionResult > 0;
	}
}
