package domainLogic;
//GUEST LOGIC 
import common.*;
import dataLayer.*;
import databaseController.*;
import utils.EmailSend;

/**
 * Controller responsible for managing Guest and Member logic.
 * Handles member identification, creation, deletion, and communication (SMS/Email).
 */
public class GuestController {
	
	/**
	 * Sends an SMS message to a client's phone number.
	 * * @param phoneNumber   the recipient's phone number
	 * @param message       the message content wrapped in a BistroMessage
	 * @param guiController reference to the server GUI controller for logging
	 */
	public static void sendSMS(Integer phoneNumber, BistroMessage message, ServerFrameController guiController) {
		
	}
	
	/**
	 * Sends an email message to a client.
	 * * @param email         the recipient's email address
	 * @param message       the message content wrapped in a BistroMessage
	 * @param guiController reference to the server GUI controller for logging
	 */
	public static void sendEmail(String email, BistroMessage message, ServerFrameController guiController) {
		
	}
	
	/**
	 * Identifies a member based on phone number or email and verifies the password.
	 * * @param memberToCheck the member object containing login credentials (phone/email and password)
	 * @param guiController reference to the server GUI controller for logging
	 * @return a BistroMessage containing the found Member or an error status
	 */
	public static BistroMessage memberIdentification(Member memberToCheck, ServerFrameController guiController) {
		String phoneStr = memberToCheck.getPhoneNumber();
		String email = memberToCheck.getEmail();
		Member wantedMember = null;
		System.out.println(memberToCheck.toString());
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

	/**
	 * Creates a new member in the system and sends a welcome email.
	 * * @param memberToCreate the member object containing details to be saved
	 * @param guiController  reference to the server GUI controller for logging
	 * @return a BistroMessage containing the created Member or an error message
	 */
	public static BistroMessage memberCreation(Member memberToCreate, ServerFrameController guiController) {
		Object recieved = (CreateCommands.createMember(memberToCreate, guiController)).getData(); 
		if(recieved instanceof String) {
			String errorMessage = (String)recieved;
			return new BistroMessage(Action.MEMBER_NOT_CREATED, errorMessage);
		}else {
			Member createdMember = (Member)recieved;
			EmailSend.sendMembershipCreation(createdMember);
			return new BistroMessage(Action.CREATE_MEMBER, createdMember);
		}	
	}

	/**
	 * Deletes an existing member from the system.
	 * * @param memberToDelete the member object to be deleted
	 * @param guiController  reference to the server GUI controller for logging
	 * @return a BistroMessage indicating success (DELETE_MEMBER) or failure (MEMBER_NOT_FOUND)
	 */
	public static BistroMessage memberDelete(Member memberToDelete, ServerFrameController guiController) {
		boolean memberDeleted = DeleteCommands.deleteMember(memberToDelete, guiController); 
		if(memberDeleted) {
			return new BistroMessage(Action.DELETE_MEMBER, memberDeleted);
		}
		return new BistroMessage(Action.MEMBER_NOT_FOUND, null);
	}
	
	/**
	 * Updates the details of an existing member.
	 * * @param toUpdate      the member object with updated details
	 * @param guiController reference to the server GUI controller for logging
	 * @return a BistroMessage containing the updated Member object
	 */
	public static BistroMessage updateMemberDetails(Member toUpdate, ServerFrameController guiController) {
		Member memberUpdated = UpdateCommands.updateMember(toUpdate, guiController);
		return new BistroMessage(Action.UPDATE_MEMBER, memberUpdated);
	}

}