package handlers;

import java.net.URL;
import java.util.ResourceBundle;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Member;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ClientDashboardController implements Initializable {
	@FXML
	Label welcomeLabel;
	@FXML
	VBox memberSection;
	@FXML
	VBox guestSection;
    // Handles the action when "Current Visit" button is clicked.
    @FXML
    private void handleCurrentVisit(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/VisitIdentification.fxml", "Current Visit Identification");
    }

    // Handles the action when "Get Table Now" button is clicked.
    @FXML
    private void handleGetTableNow(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/VisitNow.fxml", "Get Table Now");
    }

    // Handles the action when "New Reservation" button is clicked.
    @FXML
    private void handleNewReservation(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/MakeReservation.fxml", "New Reservation");
    }

    // Handles the action when "Find Reservation" button is clicked.
    @FXML
    private void handleFindReservation(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/ReservationFrame.fxml", "Find Reservation");
    }

    // Handles the action when "Back" button is clicked.
    @FXML
    private void handleLogout(ActionEvent event) {
    	if(BistroClient.memberInstance != null) {
        	BistroClient.memberInstance = null;
        	ClientUI.chat.accept(new BistroMessage(Action.DISCONNECT, null));
        	SceneLoader.loadScene(event, "/gui/MemberLoginGUI.fxml", "Member Login");
    	}
    	else {
    		SceneLoader.loadScene(event, "/gui/IsMemberGUI.fxml", "Client Login Options");
    	}
    }
    
    // Handles the action when "My Reservations" button is clicked.
    @FXML
    private void handleMyReservations(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/MemberReservations.fxml", "My Reservations");
    }
    
    // Handles the action when "My Visits" button is clicked.
    @FXML
    private void handleMyVisits(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/ViewVisits.fxml", "My Visits");
    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		if(BistroClient.memberInstance != null) {
			welcomeLabel.setText("Logged in as: " +BistroClient.memberInstance.getFullName());
			memberSection.setVisible(true);
			guestSection.setVisible(false);
			guestSection.setManaged(false);
		}else {
			welcomeLabel.setText("Logged in as: Guest");
			guestSection.setVisible(true);
			memberSection.setVisible(false);
			memberSection.setManaged(false);
		}
		
	}
    
    
}
