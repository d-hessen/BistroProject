package handlers;

import client.BistroClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ClientWaitingController {

    @FXML
    void handleCancelWaiting(ActionEvent event) {
        System.out.println("Cancel Waiting clicked: Removing client from queue...");
        //TODO
        //ADD LOGIC OF SETTING STATUS TO CANCELLED
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