package domainLogic;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import common.Action;
import common.BistroMessage;
import dataLayer.Reservation;
import dataLayer.Visit;
import databaseController.CreateCommands;
import databaseController.DeleteCommands;
import databaseController.GetCommands;
import databaseController.UpdateCommands;

// RESERVATION LOGIC 
public class ReservationController {
	
	public static BistroMessage getReservation(Integer reservationID, ServerFrameController guiController) {
		Reservation recieved = GetCommands.getReservation(reservationID, guiController);
		if(recieved != null) {
			return new BistroMessage(Action.GET_RESERVATION, recieved);
		}
		return new BistroMessage(Action.RESERVATION_NOT_FOUND, recieved);
	}
	
	public static BistroMessage updateReservation(Reservation reservationToUpdate, ServerFrameController guiController) {
		boolean success = UpdateCommands.updateReservation(reservationToUpdate, guiController);
		return new BistroMessage(Action.UPDATE_RESERVATION, success);
	}

	public static BistroMessage createReservation(Reservation reservationToCreate, ServerFrameController guiController) {
        reservationToCreate.setVerificationCode(generateVerificationCode());
        Reservation createdReservation = CreateCommands.createReservation(reservationToCreate, guiController);
        
        // 2. Check if creation was successful (object is not null)
        if(createdReservation != null) {
            return new BistroMessage(Action.CREATE_RESERVATION, createdReservation);
        }
        
        return new BistroMessage(Action.RESERVATION_NOT_CREATED, null);
	}
	
	public static BistroMessage cancelReservation(Reservation res, ServerFrameController guiController) {	    
	    boolean success = DeleteCommands.deleteReservation(res.getReservationId(), guiController);    
	    if (success) {
	        return new BistroMessage(Action.CANCEL_RESERVATION, true);
	    } else {
	        return new BistroMessage(Action.CANCEL_RESERVATION, false);
	    }
	}
	
	public static BistroMessage codeVerification(String code, ServerFrameController guiController) {
		Reservation reservation = GetCommands.getReservationVerificationCode(code, guiController);
		if(reservation != null) {
			return new BistroMessage(Action.GET_VERIFICATION_CODE, reservation);
		}
		Visit waiting = GetCommands.getWaitingVisit(code, guiController);
		if(waiting != null) {
			return new BistroMessage(Action.GET_VERIFICATION_CODE, waiting);
		}
		return new BistroMessage(Action.GET_VERIFICATION_CODE, "Error: Verification Code wasn't found");
	}

	public static String generateVerificationCode() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		SecureRandom random = new SecureRandom();
		String result = random.ints(5, 0, chars.length())
                .mapToObj(chars::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
		return result;
	}

	public static BistroMessage getMemberReservations(String phoneNumber, ServerFrameController guiController) {
		List<Reservation> memberReservations = new ArrayList<>();
		memberReservations = GetCommands.getReservationsByPhoneNumber(phoneNumber, guiController); 
	    if (memberReservations != null) {
	        return new BistroMessage(Action.GET_MEMBER_RESERVATIONS, memberReservations);
	    } else {
	        return new BistroMessage(Action.MEMBER_NOT_FOUND, phoneNumber);
	    }
	}
}