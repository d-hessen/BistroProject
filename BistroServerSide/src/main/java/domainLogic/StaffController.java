package domainLogic;

import java.util.ArrayList;

import common.Action;
import common.BistroMessage;
import dataLayer.Member;
import dataLayer.Reservation;
import dataLayer.Staff;
import dataLayer.Table;
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
        GetCommands getCmd = new GetCommands();
        ArrayList<Table> tables = getCmd.getAllTablesWithStatus(guiController);
        return new BistroMessage(Action.GET_ALL_TABLES, tables);
    }
	
	public static BistroMessage checkInCustomer(String verificationCode, ServerFrameController guiController) {
        var reservation = GetCommands.getReservationVerificationCode(verificationCode, guiController);
        
        if (reservation != null) {
            // Logic to start visit would go here (CreateCommands.startVisit...)
            // For now, we return success
            return new BistroMessage(Action.CHECK_IN_CUSTOMER, "Check-in Successful for Res #" + reservation.getReservationId());
        } else {
            return new BistroMessage(Action.CHECK_IN_CUSTOMER, "Invalid Code");
        }
    }
	
	public static BistroMessage verifyMemberArrival(String memberCode, ServerFrameController guiController) {
        GetCommands getCmd = new GetCommands();
        CreateCommands createCmd = new CreateCommands();
        
        //Check if reservation exists for this member within 30 mins
        Reservation res = getCmd.findUpcomingReservationByCode(memberCode, guiController);
        
        if (res == null) {
            return new BistroMessage(Action.VERIFY_MEMBER_ARRIVAL, "Error: No reservation found for this member in the next 30 minutes.");
        }
        
        //Find an available table for the party size
        Integer tableId = getCmd.getAvailableTableId(res.getNumberOfGuests());
        
        if (tableId == null) {
            // Reservation exists, but no table is free
            return new BistroMessage(Action.VERIFY_MEMBER_ARRIVAL, "Error: Reservation found, but no matching table is currently free.");
        }
        
        //Create the Visit (Check In)
        boolean success = createCmd.createVisit(res.getReservationId(), tableId, guiController);
        
        if (success) {
            //Send Notifications (Mock)
            Member m = getCmd.getMemberByCode(memberCode);
            String contact = (m != null) ? m.getEmail() : "Member";
            
            guiController.addToConsole("Sent Email to " + contact + ": Your table #" + tableId + " is ready!");
            guiController.addToConsole("Sent SMS: Visit confirmed.");
            
            return new BistroMessage(Action.VERIFY_MEMBER_ARRIVAL, "Success: Checked in at Table " + tableId);
        } else {
            return new BistroMessage(Action.VERIFY_MEMBER_ARRIVAL, "Error: Database failed to create visit.");
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
		boolean success = UpdateCommands.updateTable(tableToUpdate, guiController);
		if(success) {
			return new BistroMessage(Action.UPDATE_TABLE, tableToUpdate);
		} else {
			return new BistroMessage(Action.UPDATE_TABLE, "Error deleting from database");
		}
	}

}
