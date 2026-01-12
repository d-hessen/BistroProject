package handlers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import common.Status;
import dataLayer.Reservation;
import dataLayer.Visit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


// Controller for the Visit Identification screen.

public class VisitIdentificationController implements Initializable {

    @FXML
    private TextField visitCodeField;

    @FXML
    private Button identifyButton;

    @FXML
    private Label statusLabel;


    public static Visit created = null;
    private ActionEvent event;
    
    @Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		BistroClient.visitIdentificationControllerInstance = this;
	}

    // Handles the identification process when the user clicks the "Identify Visit" button.
    @FXML
    private void handleIdentification(ActionEvent event) throws IOException {
        String code = visitCodeField.getText().trim();
        // Validate that the code field is not empty
        if (code.isEmpty()) {
            statusLabel.setText("Please enter your visit code.");
            statusLabel.setTextFill(Color.RED);
            return;
        }
        ClientUI.chat.accept(new BistroMessage(Action.GET_VERIFICATION_CODE, code));
        this.event = event;
    }
    //BistroClient calls this method - if verification code found - try to create new visit
    //response can be Reservation/Waiting Visit/String
    public void checkIn(Object response) {
    	 Platform.runLater(() -> {
    		 if(response instanceof String) {
  		  		String message = (String)response;
  		  		statusLabel.setText(message);
  		  		statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
  	  		} else{
  	  			//Response here is either Reservation(from reservations) or Visit(from waiting_list)
  	  			if(response instanceof Reservation) {
  	  				Reservation reservation = (Reservation)response;
  	  				Status reservationStatus = reservation.getStatus();
  	  				switch (reservationStatus) {
					case seated:
					case approved:
						ClientUI.chat.accept(new BistroMessage(Action.CHECK_IN_CUSTOMER, reservation));
						break;
					case pending:
						SceneLoader.showAlert(Alert.AlertType.ERROR, "Reservation not approved", "Your reservation not approved yet.");
						return;
					case cancelled:
						SceneLoader.showAlert(Alert.AlertType.ERROR, "Reservation cancelled", "Your reservation was cancelled.");
						return;
					case no_show:
						SceneLoader.showAlert(Alert.AlertType.ERROR, "Reservation finished", "Your reservation finished.");
						return;
					default:
						break;
					}
  	  			} else if(response instanceof Visit) {
  	  				Visit visit = (Visit)response;
  	  				ClientUI.chat.accept(new BistroMessage(Action.CHECK_IN_CUSTOMER, visit));
  	  			}
  	  			
  	  		}
         });
    }
    //BistroClient calls this method - if customer checked in return his visit from visits
    //If recieved wait message - customer waits
    //If recieved error show error
    public void customerCheckedIn(Object answer) {
    	Platform.runLater(() -> {
    		if(answer instanceof String) {
        		String message = (String)answer;
        		if(message.startsWith("Wait")) {
        			SceneLoader.loadScene(event, "/gui/ClientWaiting.fxml", "Waiting");
        		}else {
        			statusLabel.setText(message);
     		  		statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        		}
        	} else {
        		created = (Visit)answer;
        		SceneLoader.switchScreen(
            		    event, 
            		    "/gui/VisitDetails.fxml", 
            		    "Visit Details", 
            		    (VisitDetailsController controller) -> {
            		        // This code runs after the controller is loaded
            		        controller.loadVisit(created);
            		    }
            		);
        	}
    	});
    }

    // Handles the "Forgot my code" action.
    @FXML
    private void handleForgotPassword(ActionEvent event) {
    	// TODO: send the client code via email and SMS
    	if(BistroClient.memberInstance != null) {
            ClientUI.chat.accept(new BistroMessage(Action.GET_MEMBER_RESERVATIONS, BistroClient.memberInstance.getPhoneNumber()));
    		EmailSend.sendReservationsTableByEmail(BistroClient.reservationsList);    		
    	}
        statusLabel.setText("If the code exists, it has been sent to your Email/SMS.");
        statusLabel.setTextFill(Color.web("#1976d2"));
    }


    // Navigates back to the previous dashboard.
    @FXML
    private void handleBack(ActionEvent event) {
    	if(BistroClient.staffInstance != null) {
    		SceneLoader.closeWindow(event);
    	}else {
    		SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    	}
    }
}