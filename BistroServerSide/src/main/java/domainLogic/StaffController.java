package domainLogic;

import java.util.ArrayList;
import java.util.List;

import common.Action;
import common.BistroMessage;
import dataLayer.Member;
import dataLayer.Reservation;
import dataLayer.Staff;
import dataLayer.Table;
import dataLayer.Visit;
import databaseController.CreateCommands;
import databaseController.DeleteCommands;
import databaseController.GetCommands;
import databaseController.UpdateCommands;

/**
 * Controller responsible for Staff-specific logic.
 * Includes staff authentication, table management (CRUD), and verifying member arrival.
 */
public class StaffController {

	/**
	 * Authenticates a staff member.
	 * * @param staffRecieved the staff object containing credentials
	 * @param guiController reference to the server GUI controller for logging
	 * @return a BistroMessage containing the authenticated Staff object or an error
	 */
	public static BistroMessage staffIdentification(Staff staffRecieved, ServerFrameController guiController) {
		Staff wantedStaff = GetCommands.getStaff(staffRecieved.getUsername(), guiController);
		
		if(wantedStaff == null) {
			String message = "Staff not found";
			guiController.addToConsole(message);
			return new BistroMessage(Action.STAFF_NOT_FOUND, message);
		}
		
		if(!wantedStaff.getPassword().equals(staffRecieved.getPassword())) {
			String message = "Password is wrong";
			guiController.addToConsole("Password is wrong");
			return new BistroMessage(Action.STAFF_NOT_FOUND, message);
		}
		
		return new BistroMessage(Action.STAFF_IDENTIFICATION, wantedStaff);
	}
	
	/**
	 * Retrieves all tables from the database.
	 * * @param guiController reference to the server GUI controller
	 * @return a BistroMessage containing an ArrayList of Table objects
	 */
	public static BistroMessage getAllTables(ServerFrameController guiController) {
        ArrayList<Table> tables = GetCommands.getAllTablesWithStatus(guiController);
        return new BistroMessage(Action.GET_ALL_TABLES, tables);
    }

	/**
	 * Verifies if a member with a specific code has arrived.
	 * Handles logic for converting a reservation or waiting list entry into an active Visit.
	 * * @param memberCode    the unique code provided by the member
	 * @param guiController reference to the server GUI controller for logging
	 * @return a BistroMessage containing the created Visit or an error message
	 */
	public static BistroMessage verifyMemberArrival(String memberCode, ServerFrameController guiController) { 
        //Check if reservation exists for this member within 15 mins
        Reservation reservation = GetCommands.findUpcomingReservationByCode(memberCode, guiController);
        List<Visit> waitingList = GetCommands.getWaitingList(guiController);
        Visit waiting = null;
        for (Visit visit : waitingList) {
			if(visit.getGuest() instanceof Member) {
				Member member = (Member)visit.getGuest();
				if(member.getCardCode().equals(memberCode)) {
					waiting = visit;
				}
			}
		}
        BistroMessage response = null;
        if (reservation == null) {
        	if(waiting == null) {
                return new BistroMessage(Action.VERIFY_MEMBER_ARRIVAL, "Error: No reservation found for this member in the next 15 minutes.");
        	}else {
        		response = VisitController.createWalkInVisit(waiting, guiController);
        	}
        }else {
        	response = VisitController.createReservatedVisit(reservation, guiController);
        }
        
        //Find an available table for the party 
        if(response.getData() instanceof Visit) {
        	Visit created = (Visit)response.getData();
        	Member m = GetCommands.getMemberByCode(memberCode, guiController);
            String contact = (m != null) ? m.getEmail() : "Member";
            guiController.addToConsole("Sent Email to " + contact + ": Your table #" + created.getTable().getTableNumber() + " is ready!");
            guiController.addToConsole("Sent SMS: Visit confirmed.");
            return new BistroMessage(Action.VERIFY_MEMBER_ARRIVAL, created);
        }else {//Response is String
        	return new BistroMessage(Action.VERIFY_MEMBER_ARRIVAL, response.getData());
        }
    }

	/**
	 * Adds a new table to the restaurant layout.
	 * * @param tableRecieved the table object to create
	 * @param guiController reference to the server GUI controller for logging
	 * @return a BistroMessage containing the created Table or an error
	 */
	public static BistroMessage addNewTable(Table tableRecieved, ServerFrameController guiController) {
		Table created = CreateCommands.createTable(tableRecieved, guiController);
		if(created != null) {
			return new BistroMessage(Action.ADD_TABLE, created);
		} else {
			return new BistroMessage(Action.ADD_TABLE, "Error adding to database");
		}
		
	}

	/**
	 * Deletes a table from the restaurant layout.
	 * * @param tableToDelete the table object to delete
	 * @param guiController reference to the server GUI controller for logging
	 * @return a BistroMessage containing the deleted Table or an error
	 */
	public static BistroMessage deleteTable(Table tableToDelete, ServerFrameController guiController) {
		boolean success = DeleteCommands.deleteTable(tableToDelete, guiController);
		if(success) {
			return new BistroMessage(Action.DELETE_TABLE, tableToDelete);
		} else {
			return new BistroMessage(Action.GET_ALL_TABLES, "Error deleting from database");
		}
	}

	/**
	 * Updates an existing table's details.
	 * * @param tableToUpdate the table object with new details
	 * @param guiController reference to the server GUI controller for logging
	 * @return a BistroMessage with the updated Table or an error string
	 */
	public static Object updateTable(Table tableToUpdate, ServerFrameController guiController) {
		Table updated = UpdateCommands.updateTable(tableToUpdate, guiController);
		if(updated != null) {
			return new BistroMessage(Action.UPDATE_TABLE, updated);
		} else {
			return new BistroMessage(Action.UPDATE_TABLE, "Error deleting from database");
		}
	}

}