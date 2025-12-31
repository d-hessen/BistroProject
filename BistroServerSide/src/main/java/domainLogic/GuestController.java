package domainLogic;
//GUEST LOGIC 
import common.*;
import dataLayer.*;
import databaseController.*;
public class GuestController {
	
	//Method will send message to phone number of client
	public static void sendSMS(Integer phoneNumber, BistroMessage message, ServerFrameController guiController) {
		
	}
	
	//Method will send message to email of client
	public static void sendEmail(String email, BistroMessage message, ServerFrameController guiController) {
		
	}
	
	public static BistroMessage memberIdentification(Member memberToCheck, ServerFrameController guiController) {
		String phoneStr = memberToCheck.getPhoneNumber();
		String email = memberToCheck.getEmail();
		Member wantedMember = null;
		
		boolean hasPhone = (phoneStr != null && !phoneStr.isEmpty());
		boolean hasEmail = (email != null && !email.isEmpty());	
		
		if(hasPhone || hasEmail) {
			if(hasPhone) {
				try {
	                // Parse inside the safe block
	                int phone = Integer.parseInt(phoneStr);
	                wantedMember = GetCommands.getMember(phone, guiController);
	            } catch (NumberFormatException e) {
	                guiController.addToConsole("Error: Invalid phone number format");
	            }
			}
			else if(hasEmail) {
				wantedMember = GetCommands.getMember(email, guiController);
			}
		}
		else {
			guiController.addToConsole("Received member with NULL/Empty phone and NULL/Empty email");
		}
		
		if(wantedMember == null) {
			guiController.addToConsole("Member not found");
			return new BistroMessage(Action.MEMBER_NOT_FOUND, null);
		}
		
		if(!wantedMember.getPassword().equals(memberToCheck.getPassword())) {
			guiController.addToConsole("Password is wrong");
			return new BistroMessage(Action.MEMBER_NOT_FOUND, null);
		}
		return new BistroMessage(Action.MEMBER_IDENTIFICATION, wantedMember);
	}

	public static BistroMessage memberCreation(Member memberToCreate, ServerFrameController guiController) {
		Object recieved = (CreateCommands.createMember(memberToCreate, guiController)).getData(); 
		if(recieved instanceof String) {
			String errorMessage = (String)recieved;
			return new BistroMessage(Action.MEMBER_NOT_CREATED, errorMessage);
		}else {
			Member createdMember = (Member)recieved;
			return new BistroMessage(Action.CREATE_MEMBER, createdMember);
		}	
	}

	public static BistroMessage memberDelete(Member memberToDelete, ServerFrameController guiController) {
		boolean memberDeleted = DeleteCommands.deleteMember(memberToDelete, guiController); 
		if(memberDeleted) {
			return new BistroMessage(Action.DELETE_MEMBER, memberDeleted);
		}
		return new BistroMessage(Action.MEMBER_NOT_FOUND, null);
	}

}
