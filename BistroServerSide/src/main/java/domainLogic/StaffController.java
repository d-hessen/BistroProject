package domainLogic;

import common.Action;
import common.BistroMessage;
import dataLayer.Staff;
import databaseController.GetCommands;

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

}
