package databaseController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import common.Action;
import common.BistroMessage;
import dataLayer.*;

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
	public static Integer createReservation(Reservation resToCreate, ServerFrameController guiController) {
			Connection conn = dbController.getInstance().getConnection();

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
				guiController.addToConsole("Error adding reservation for" +resToCreate.getMemberId()+ " to database. Error: " +e.getMessage());
				e.printStackTrace();
			}
			return null;
	}
	//======================================
	//MEMBER CREATION
	//======================================
	public static BistroMessage createMember(Member memberToCreate, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		
		String sql = "INSERT INTO members (full_name, phone, email, password, card_code) VALUES (?, ?, ?, ?, ?)";
		String errorMessage = null;
		//Set values to query
		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, memberToCreate.getFullName());
			ps.setString(2, memberToCreate.getPhoneNumber());
			ps.setString(3, memberToCreate.getEmail());
			ps.setString(4, memberToCreate.getPassword());
			ps.setString(5, memberToCreate.getCardCode());
			//Execute prepared query
			ps.executeUpdate();
			Member createdMember = GetCommands.getMember(Integer.parseInt(memberToCreate.getPhoneNumber()), guiController);
			return new BistroMessage(Action.CREATE_MEMBER, createdMember);
		} catch(SQLIntegrityConstraintViolationException e) {
			errorMessage = "User already exists";
		}
		catch(SQLException e) {
			
			guiController.addToConsole("Error creating member: " +memberToCreate.getFullName()+". Error: " +e.getMessage());
		}
		return new BistroMessage(Action.MEMBER_NOT_CREATED, errorMessage);
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
	
	//======================================
	//TABLE CREATION
	//======================================
	public boolean createTable(Integer tableNumber, Integer tableCapacity, boolean isActive, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();

		Table tableToCreate = new Table(tableNumber, tableCapacity, isActive);
		
		String sql = "INSERT INTO tables (table_number, capacity, is_active) VALUES (?, ?, ?)";
		//Set values to query
		int executionResult = 0;
		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setInt(1, tableToCreate.getTableNumber());
			ps.setInt(2, tableToCreate.getTableCapacity());
			ps.setBoolean(3, tableToCreate.isActive());
			//Execute prepared query
			executionResult = ps.executeUpdate();
		} catch(SQLException e) {
			guiController.addToConsole("Error creating table: " +tableToCreate.getTableNumber()+". Error: " +e.getMessage());
			e.printStackTrace();
		}
		return executionResult > 0;
	}
}
