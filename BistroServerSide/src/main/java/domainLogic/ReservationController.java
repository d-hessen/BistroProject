package domainLogic;

import common.Action;
import common.BistroMessage;
import dataLayer.Reservation;
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
        // 1. Call DB to create reservation. 
        // returns the Reservation object with the new ID set inside it.
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
}