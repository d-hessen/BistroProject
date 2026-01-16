package databaseController;

import java.sql.*;
import common.Action;
import common.BistroMessage;
import dataLayer.*;
import domainLogic.ServerFrameController;

//HANDLES ALL SQL INSERT OPERATIONS
public class CreateCommands {
    // ======================================
    // RESERVATION CREATION
    // ======================================
	/**
	 * Creates new reservation object in db
	 * @param resToCreate reservation object containing details to insert
	 * @param guiController controller used for logging to server console
	 * @return updated reservation ogject with generated ID or null if failed
	 */
    public static Reservation createReservation(Reservation resToCreate, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        String sql = "INSERT INTO reservation ("
                + "reservation_date, reservation_time, number_of_guests, verification_code, "
                + "member_id, guest_full_name, guest_phone, email, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"; 

        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, resToCreate.getReservationDate().getDate());            
            ps.setString(2, resToCreate.getReservationDate().getTime());
            ps.setInt(3, resToCreate.getNumberOfGuests());            
            ps.setString(4, resToCreate.getVerificationCode());
            ps.setObject(5, resToCreate.getMemberId(), Types.INTEGER);

            //Guest details
            if (resToCreate.getGuest() != null) {
                ps.setString(6, resToCreate.getGuest().getFullName());
                ps.setString(7, resToCreate.getGuest().getPhoneNumber());
                ps.setString(8, resToCreate.getGuest().getEmail());
            } else {
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.VARCHAR);
            }

            //Status
            if (resToCreate.getStatus() != null) {
                ps.setString(9, resToCreate.getStatus().name());
            } else {
                ps.setString(9, "pending");
            }
            int executionResult = ps.executeUpdate();

            if (executionResult > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                    	resToCreate.setReservationId(generatedKeys.getInt(1));                       
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
    /**
     * Create new member in db
     * @param memberToCreate member object to insert
     * @param guiController controller for logging to server console
     * @return BistroMessage (with created member) indicating success /(with null) failure 
     */
	public static BistroMessage createMember(Member memberToCreate, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		String sql = "INSERT INTO members (full_name, phone, email, password, card_code) VALUES (?, ?, ?, ?, ?)";
		String errorMessage = null;

		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, memberToCreate.getFullName());
			ps.setString(2, memberToCreate.getPhoneNumber());
			ps.setString(3, memberToCreate.getEmail());
			ps.setString(4, memberToCreate.getPassword());
			ps.setString(5, memberToCreate.getCardCode());

			ps.executeUpdate();
			//Get the created member to confirm and get generated fields
			Member createdMember = GetCommands.getMember(Integer.parseInt(memberToCreate.getPhoneNumber()), guiController);
			return new BistroMessage(Action.CREATE_MEMBER, createdMember);
		} catch(SQLIntegrityConstraintViolationException e) {
			return new BistroMessage(Action.MEMBER_NOT_CREATED, "User already exists");
		}
		catch(SQLException e) {
			guiController.addToConsole("Error creating member: " +memberToCreate.getFullName()+". Error: " +e.getMessage());
			return new BistroMessage(Action.MEMBER_NOT_CREATED, errorMessage);
		}	
	}
	//======================================
	//STAFF CREATION
	//======================================
	/**
     * Creates a new Staff member
     * @param username Staff username.
     * @param password Staff password.
     * @param fullName Staff full name.
     * @param isManager True if manager, false if worker.
     * @param guiController Controller for logging.
     * @return true if creation was successful. false otherwise.
     */
	public boolean createStaff(Staff staffToCreate, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		String sql = "INSERT INTO staff (username, password, full_name, role) VALUES (?, ?, ?, ?)";

		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1,staffToCreate.getUsername());
			ps.setString(2, staffToCreate.getPassword());
			ps.setString(3, staffToCreate.getFullName());
			ps.setString(4, staffToCreate.isManager() ? "manager" : "worker");
			return ps.executeUpdate() > 0;
		} catch(SQLException e) {
			guiController.addToConsole("Error creating staff: " +staffToCreate.getFullName()+". Error: " +e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	//======================================
	//TABLE CREATION
	//======================================
	/**
	 * Create table in system.
	 * @param tableToCreate table to insert to DB
	 * @param guiController controller for logging
	 * @return created table object from DB, or null if failed.
	 */
	public static Table createTable(Table tableToCreate, ServerFrameController guiController) {
		Connection conn = dbController.getInstance().getConnection();
		String sql = "INSERT INTO tables (table_number, capacity, is_active) VALUES (?, ?, ?)";
		//Set values to query
		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setInt(1, tableToCreate.getTableNumber());
			ps.setInt(2, tableToCreate.getTableCapacity());
			ps.setBoolean(3, tableToCreate.isActive());
			
			ps.executeUpdate();
			return GetCommands.getTable(tableToCreate.getTableNumber(), guiController);
		} catch(SQLException e) {
			guiController.addToConsole("Error creating table: " +tableToCreate.getTableNumber()+". Error: " +e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	//======================================
	//VISIT CREATION
	//======================================
	/**
	 * Create visit from existing reservation by reservation ID in DB
	 * @param reservationId Integer reservation id
	 * @param tableId id of assigned table
	 * @param guiController logging controller for server console
	 * @return created visit object in DB
	 */
	public static Visit createVisit(Integer reservationId, Integer tableId, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        Reservation reservation = GetCommands.getReservation(reservationId, guiController);
        String sql = "INSERT INTO visits (reservation_number, table_id, is_active, party_size, "
                + "member_id, guest_full_name, guest_phone, email, verification_code) "
                + "VALUES (?, ?, 1, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reservationId);
        	ps.setInt(2, tableId);
            ps.setInt(3, reservation.getNumberOfGuests());
            ps.setObject(4, reservation.getMemberId());
            ps.setObject(5, reservation.getGuest().getFullName());
            ps.setObject(6, reservation.getGuest().getPhoneNumber());
            ps.setObject(7, reservation.getGuest().getEmail());
            ps.setString(8, reservation.getVerificationCode());

            if(ps.executeUpdate() > 0) {
            	try(ResultSet generatedKeys = ps.getGeneratedKeys()){
            		if(generatedKeys.next()) {
            			Integer visitId = generatedKeys.getInt(1);
                        createBill(visitId, guiController); // Auto-create bill
                        return GetCommands.getVisit(visitId, guiController);
            		}
            	}
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error creating visit from reservation: " + e.getMessage());
        }
        return null;
    }
	/**
	 * Create walk-in visit to waiting_list table in DB
	 * @param toCreate visit object with guest details
	 * @param guiController controller for logging
	 * @return visit object updated with waiting ID generated in DB
	 */
	public static Visit createWaitingWalkInVisit(Visit toCreate, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        Guest mainGuest = toCreate.getGuest();
        Integer memberId = (mainGuest instanceof Member) ? ((Member) mainGuest).getMemberId() : null;
        
        String sql = "INSERT INTO waiting_list (member_id, guest_full_name, guest_phone, email, "
                + "number_of_guests, verification_code) VALUES (?, ?, ?, ?, ?, ?)";  
        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        	ps.setObject(1, memberId);
        	ps.setString(2, mainGuest.getFullName());
        	ps.setString(3, mainGuest.getPhoneNumber());
        	ps.setString(4, mainGuest.getEmail());
        	ps.setInt(5, toCreate.getPartySize());
        	ps.setString(6, toCreate.getVerificationCode());
        	
            if(ps.executeUpdate() > 0) {
            	try(ResultSet generatedKeys = ps.getGeneratedKeys()){
            		if(generatedKeys.next()) {
            			Visit created = GetCommands.getWaitingVisit(toCreate.getVerificationCode(), guiController);
                        created.setVisitId(generatedKeys.getInt(1)); // Set waiting_id as visit_id temporarily
                        return created;
            		}
            	}
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error creating random visit: " + e.getMessage());
        }
        return null;
    }
    /**
     * Create immediate active visit for walk-in guest that seated immediately
     * @param visitToCreate visit object to create
     * @param tableId assigned tableId to visit
     * @param guiController logging controller
     * @return created Visit object
     */
    public static Visit createSeatedWalkInVisit(Visit visitToCreate, Integer tableId, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        Guest guest = visitToCreate.getGuest();
        Integer memberId = (guest instanceof Member) ? ((Member) guest).getMemberId() : null;

        String sql = "INSERT INTO visits (table_id, is_active, party_size, member_id, waiting_id, "
                + "guest_full_name, guest_phone, email, verification_code) "
                + "VALUES (?, 1, ?, ?, ?, ?, ?, ?, ?)";        
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tableId);
            ps.setInt(2, visitToCreate.getPartySize());
            ps.setObject(3, memberId);
            ps.setObject(4, visitToCreate.getWaitingId());
            ps.setObject(5, guest.getFullName());
            ps.setObject(6, guest.getPhoneNumber());
            ps.setObject(7, guest.getEmail());
            ps.setString(8, visitToCreate.getVerificationCode());
            
            if(ps.executeUpdate() > 0) {
            	try(ResultSet generatedKeys = ps.getGeneratedKeys()){
            		if(generatedKeys.next()) {
            			Integer visitId = generatedKeys.getInt(1);
                        createBill(visitId, guiController);
                        return GetCommands.getVisit(visitId, guiController);
            		}
            	}
            }
        } catch (SQLException e) {
            guiController.addToConsole("Error creating directly seated walk-in visit: " + e.getMessage());
        }
        return null;
    }
	//======================================
	//BILL CREATION
	//======================================
	/**
	 * Create initial unpaid Bill for specific visit. Apply discount if member
	 * @param visitId visitId attached to Bill
	 * @param guiController controller for logging
	 * @return true if insert succeeded, otherwise false
	 */
	public static boolean createBill(Integer visitId, ServerFrameController guiController) {
        Connection conn = dbController.getInstance().getConnection();
        Visit visit = GetCommands.getVisit(visitId, guiController);
        
        Integer memberId = null;
        double discount = 0.00;
        
        if(visit.getGuest() instanceof Member) {
        	Member member = (Member)visit.getGuest();
        	memberId = member.getMemberId();
        	discount = 10.00;
        }
        
        String sql = "INSERT INTO bills (visit_id, member_id, total_amount, discount_amount, final_amount, is_paid) "
                + "VALUES (?, ?, 0, ?, 0, 0)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        	ps.setInt(1, visit.getVisitId());
            ps.setObject(2, memberId);
            ps.setDouble(3, discount);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            guiController.addToConsole("Error creating bill: " + e.getMessage());
            return false;
        }
    }
}
