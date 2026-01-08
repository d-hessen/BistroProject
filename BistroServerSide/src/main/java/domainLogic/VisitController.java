package domainLogic;

import common.Action;
import common.BistroMessage;
import dataLayer.Visit;
import databaseController.CreateCommands;

public class VisitController {

	public static BistroMessage createVisit(Visit toCreate, ServerFrameController guiController) {
		toCreate.setVerificationCode(ReservationController.generateVerificationCode()); 
		boolean success = CreateCommands.createRandomVisit(toCreate, guiController);
		if(success) {
			return new BistroMessage(Action.VISIT_NOW, toCreate.getVerificationCode());
		}else {
			return new BistroMessage(Action.VISIT_NOW, "Error: Failed during creating visit - SQL error");
		}
	}
	
	public static BistroMessage createVisitByReservation(Visit toCreate, ServerFrameController guiController) {
		Integer resId = toCreate.getReservation().getReservationId();
		Integer tableId = toCreate.getTable().getTableNumber();
		if(toCreate.getReservation() == null || resId == null) {
			return new BistroMessage(Action.CREATE_VISIT, "Error: There is no reservation");
		}
		boolean success = CreateCommands.createVisit(resId,tableId, guiController);
		if(success) {
			return new BistroMessage(Action.CREATE_VISIT, resId);
		}else {
			return new BistroMessage(Action.CREATE_VISIT, "Error: Failed during creating visit - SQL error");
		}
	}
	

}
