package databaseController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import java.sql.Types; // Import for clear code
import common.Action;
import common.BistroMessage;
import common.Status;
import dataLayer.*;
import domainLogic.ServerFrameController;

public class CreateCommands {
    // ======================================
    // RESERVATION CREATION
    // ======================================
    // Method that creates a Reservation in the database and returns the Updated Reservation Object
    public static Reservation createReservation(Reservation resToCreate, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        String sql = "INSERT INTO reservation ("
                + "reservation_date,"
                + "reservation_time," 
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

        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, resToCreate.getReservationDate().getDate());            
            ps.setString(2, resToCreate.getReservationDate().getTime());
            ps.setInt(3, resToCreate.getNumberOfGuests());            
            ps.setString(4, resToCreate.getVerificationCode());
            ps.setObject(5, resToCreate.getMemberId(), Types.INTEGER);

            // 6, 7, 8. Guest Details
            if (resToCreate.getGuest() != null) {
                ps.setString(6, resToCreate.getGuest().getFullName());
                ps.setString(7, resToCreate.getGuest().getPhoneNumber());
                ps.setString(8, resToCreate.getGuest().getEmail());
            } else {
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.VARCHAR);
            }

            // 9. Status
            if (resToCreate.getStatus() != null) {
                ps.setString(9, resToCreate.getStatus().name());
            } else {
                ps.setString(9, "PENDING");
            }
            int executionResult = ps.executeUpdate();

            if (executionResult > 0) {
                // Retrieve the generated ID
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);                        
                        // update the object with the new ID
                        resToCreate.setReservationId(newId);                       
                        return resToCreate; 
                    }
                }
            }

        } catch (SQLException e) {
            guiController.addToConsole("Error adding reservation to database. Error: " + e.getMessage());
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
	//Create table with table number
	public static Table createTable(Table tableToCreate, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		Table created = null;
		String sql = "INSERT INTO tables (table_number, capacity, is_active) VALUES (?, ?, ?)";
		//Set values to query
		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setInt(1, tableToCreate.getTableNumber());
			ps.setInt(2, tableToCreate.getTableCapacity());
			ps.setBoolean(3, tableToCreate.isActive());
			//Execute prepared query
			ps.executeUpdate();
			created = GetCommands.getTable(tableToCreate.getTableNumber(), guiController);
		} catch(SQLException e) {
			guiController.addToConsole("Error creating table: " +tableToCreate.getTableNumber()+". Error: " +e.getMessage());
			e.printStackTrace();
		}
		return created;
	}
	//======================================
	//VISIT CREATION
	//======================================
	//Create visit for reservation 
	public boolean createVisit(Integer reservationId, Integer tableId, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        // Start time = Current time
        Date now = new Date();
        Timestamp startTime = new Timestamp(now.getTime());
        Reservation reservation = GetCommands.getReservation(reservationId, guiController);
        
        String sql = "INSERT INTO visits (table_id, reservation_number, start_time, is_active, party_size) VALUES (?, ?, ?, 1, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tableId);
            ps.setInt(2, reservationId);
            ps.setTimestamp(3, startTime);
            ps.setInt(4, reservation.getNumberOfGuests());
            int affectedRows = ps.executeUpdate();
            if(affectedRows > 0) {
            	try(ResultSet generatedKeys = ps.getGeneratedKeys()){
            		if(generatedKeys.next()) {
            			Integer visitId = generatedKeys.getInt(1);
            			createBill(visitId,guiController);
            		}
            	}
            	return true;
            }
            return false;
        } catch (SQLException e) {
            guiController.addToConsole("Error creating visit: " + e.getMessage());
            return false;
        }
    }
	//Create visit to random visits(waiting_list) 
	public static boolean createRandomVisit(Visit toCreate, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        Guest mainGuest = toCreate.getGuest();
        Integer memberId = null;
        if(mainGuest instanceof Member) {
        	Member member = (Member) mainGuest;
        	memberId = member.getMemberId();
        }
        String sql = "INSERT INTO waiting_list (member_id, guest_full_name, guest_phone, email, number_of_guests, verification_code) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        	ps.setObject(1, memberId);
        	ps.setString(2, mainGuest.getFullName());
        	ps.setString(3, mainGuest.getPhoneNumber());
        	ps.setString(4, mainGuest.getEmail());
        	ps.setInt(5, toCreate.getPartySize());
        	ps.setString(6, toCreate.getVerificationCode());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            guiController.addToConsole("Error creating random visit: " + e.getMessage());
            return false;
        }
    }
	//======================================
	//BILL CREATION
	//======================================
	//Create bill for visitId
	public boolean createBill(Integer visitId, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        Visit visit = GetCommands.getVisit(visitId, guiController);
        Member member = new Member(null,null,null,null);
        Guest guest = visit.getGuest();
        if(guest instanceof Member) {
        	member = (Member) guest;
        }
        
        String sql = "INSERT INTO bills (visit_id, member_id, total_amount, discount_amount, final_amount, is_paid) VALUES (?, ?, ?, ?, ?, 0)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, visit.getVisitId());
            ps.setObject(2, member.getMemberId());
            ps.setDouble(3, 0);
            if(member.getMemberId() != null) {
            	ps.setDouble(4, 10.00);
            } else {ps.setDouble(4, 0.00);}
            ps.setDouble(5, 0);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            guiController.addToConsole("Error creating bill: " + e.getMessage());
            return false;
        }
    }
}
