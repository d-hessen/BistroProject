package domainLogic;

import common.Action;
import common.BistroMessage;
import dataLayer.Reservation;
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
}
