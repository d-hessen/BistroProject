package handlers;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class ClientWaitingController {

	@FXML
    void handleCancelWaiting(ActionEvent event) {
        System.out.println("Cancel Waiting clicked: Removing client from queue...");
        
        if (BistroClient.waitingVisit == null) {
            SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "No active waiting session found.");
            handleBack(event);
            return;
        }

        ClientUI.chat.accept(new BistroMessage(Action.REMOVE_FROM_WAITING_LIST, BistroClient.waitingVisit.getWaitingId()));
        
        if (BistroClient.operationSuccess) {
            BistroClient.waitingVisit = null; // Clear the waiting session
            SceneLoader.showAlert(Alert.AlertType.INFORMATION, "Cancelled", "You have been removed from the waiting list.");
            handleBack(event); // Navigate back to dashboard
        } else {
            SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel waiting. Please try again or contact staff.");
        }
	}

    @FXML
    void handleBack(ActionEvent event) {
    	if(BistroClient.staffInstance != null) {
    		SceneLoader.closeWindow(event);
    	}else {
    		SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    	}
    }
}