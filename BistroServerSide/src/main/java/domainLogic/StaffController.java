package domainLogic;

import java.util.ArrayList;

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

public class StaffController {

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
	
	public static BistroMessage getAllTables(ServerFrameController guiController) {
        ArrayList<Table> tables = GetCommands.getAllTablesWithStatus(guiController);
        return new BistroMessage(Action.GET_ALL_TABLES, tables);
    }

	public static BistroMessage verifyMemberArrival(String memberCode, ServerFrameController guiController) { 
        //Check if reservation exists for this member within 30 mins
        Reservation reservation = GetCommands.findUpcomingReservationByCode(memberCode, guiController);
        
        if (reservation == null) {
            return new BistroMessage(Action.VERIFY_MEMBER_ARRIVAL, "Error: No reservation found for this member in the next 30 minutes.");
        }
        
        //Find an available table for the party 
        BistroMessage response = VisitController.createReservatedVisit(reservation, guiController);
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

	public static BistroMessage addNewTable(Table tableRecieved, ServerFrameController guiController) {
		Table created = CreateCommands.createTable(tableRecieved, guiController);
		if(created != null) {
			return new BistroMessage(Action.ADD_TABLE, created);
		} else {
			return new BistroMessage(Action.ADD_TABLE, "Error adding to database");
		}
		
	}

	public static BistroMessage deleteTable(Table tableToDelete, ServerFrameController guiController) {
		boolean success = DeleteCommands.deleteTable(tableToDelete, guiController);
		if(success) {
			return new BistroMessage(Action.DELETE_TABLE, tableToDelete);
		} else {
			return new BistroMessage(Action.GET_ALL_TABLES, "Error deleting from database");
		}
	}

	public static Object updateTable(Table tableToUpdate, ServerFrameController guiController) {
		Table updated = UpdateCommands.updateTable(tableToUpdate, guiController);
		if(updated != null) {
			return new BistroMessage(Action.UPDATE_TABLE, updated);
		} else {
			return new BistroMessage(Action.UPDATE_TABLE, "Error deleting from database");
		}
	}

}
