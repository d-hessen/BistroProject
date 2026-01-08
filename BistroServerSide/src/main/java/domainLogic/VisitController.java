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

}
