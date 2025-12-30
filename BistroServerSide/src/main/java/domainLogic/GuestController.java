package domainLogic;
//GUEST LOGIC 
import common.*;
import dataLayer.*;
import databaseController.GetCommands;
public class GuestController {
	
	//Method will send message to phone number of client
	public static void sendSMS(Integer phoneNumber, BistroMessage message, ServerFrameController guiController) {
		
	}
	
	//Method will send message to email of client
	public static void sendEmail(String email, BistroMessage message, ServerFrameController guiController) {
		
	}
	
	public static BistroMessage memberIdentification(Member memberToCheck, ServerFrameController guiController) {
		String phone = memberToCheck.getPhoneNumber();
		String email = memberToCheck.getEmail();
		Member wantedMember = null;
		if(phone != null || email != null) {
			if(phone != null) {
				wantedMember = GetCommands.getMember(phone, guiController);
			}
			else if(email != null) {
				wantedMember = GetCommands.getMember(email, guiController);
			}
		}
		else {
			guiController.addToConsole("Recieved member with NULL for phoneNumber and NULL for email");
		}
		
		if(wantedMember == null) {
			guiController.addToConsole("Member not found");
		}
		
		if(!wantedMember.getPassword().equals(memberToCheck.getPassword())) {
			guiController.addToConsole("Password is wrong");
			return new BistroMessage(Action.MEMBER_NOT_FOUND, null);
		}
		return new BistroMessage(Action.MEMBER_IDENTIFICATION, wantedMember);
	}

}
