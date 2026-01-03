package domainLogic;

import common.Action;
import common.BistroMessage;
import dataLayer.Reservation;
import databaseController.CreateCommands;
import databaseController.GetCommands;
import databaseController.UpdateCommands;

//RESERVATION LOGIC 
public class ReservationController {
	public static BistroMessage getReservation(Integer reservationID, ServerFrameController guiController) {
		Reservation recieved = GetCommands.getReservation(reservationID, guiController);
		if(recieved != null) {
			return new BistroMessage(Action.GET_RESERVATION, recieved);
		}
		return new BistroMessage(Action.RESERVATION_NOT_FOUND, recieved);
	}
	
	public static BistroMessage updateReservation(Reservation reservationToUpdate) {
		boolean success = UpdateCommands.updateReservation(reservationToUpdate);
		return new BistroMessage(Action.UPDATE_RESERVATION, success);
	}

	public static BistroMessage createReservation(Reservation reservationToCreate, ServerFrameController guiController) {
		if (getReservation(reservationToCreate.getReservationId(), guiController).getAction() == Action.RESERVATION_NOT_FOUND) {
			Integer createdReservationId = CreateCommands.createReservation(reservationToCreate, guiController);
			if(createdReservationId > 0) {
				return new BistroMessage(Action.CREATE_RESERVATION, createdReservationId);
			}
		}
		return new BistroMessage(Action.RESERVATION_NOT_CREATED, null);
	}
	
	public static BistroMessage codeVerification(String code, ServerFrameController guiController) {
		Reservation res = GetCommands.getVerificationCode(code, guiController);
		if(res != null) {
			return new BistroMessage(Action.GET_VERIFICATION_CODE, res);
		}
		return new BistroMessage(Action.GET_VERIFICATION_CODE, res);
	}
}
