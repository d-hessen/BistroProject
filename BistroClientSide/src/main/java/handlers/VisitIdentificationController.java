package handlers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
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
    
    @FXML
    private TextField phoneEmailField;
    
    @FXML
    private ComboBox<String> codesComboBox;

    public static Visit created = null;
    private ActionEvent event;
    
    @Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		BistroClient.visitIdentificationControllerInstance = this;
		if(phoneEmailField != null) {
            phoneEmailField.setVisible(false);
            phoneEmailField.setManaged(false);
        }
		if(codesComboBox != null) {
			codesComboBox.setVisible(false);
			codesComboBox.setManaged(false);
		}
		if(BistroClient.memberInstance != null) {
			String memberPhone = BistroClient.memberInstance.getPhoneNumber();
			if(memberPhone != null) {
				phoneEmailField.setText(memberPhone);
			}

			loadMemberVerificationCodes(memberPhone);
		}
	}
    
    private void loadMemberVerificationCodes(String phoneNumber) {
    	List<String> codes = new ArrayList<>();
    	ClientUI.chat.accept(new BistroMessage(Action.GET_MEMBER_RESERVATIONS, phoneNumber));
    	ClientUI.chat.accept(new BistroMessage(Action.GET_WAITING_LIST, null));
    	ClientUI.chat.accept(new BistroMessage(Action.GET_ACTIVE_VISITS,null));
    	if(BistroClient.visitsList != null && !BistroClient.visitsList.isEmpty()) {
    		for (Visit visit : BistroClient.visitsList) {
				if(visit.getVerificationCode() != null && !visit.getVerificationCode().isEmpty()) {
					if(visit.getGuest().getPhoneNumber().equals(phoneNumber)) {
						codes.add(visit.getVerificationCode());
					}
				}
			}
    	}
    	if(BistroClient.reservationsList != null && !BistroClient.reservationsList.isEmpty()) {
    		for(Reservation res : BistroClient.reservationsList) {
    			if(res.getVerificationCode() != null && !res.getVerificationCode().isEmpty()) {
    				if(!codes.contains(res.getVerificationCode())) {
    					codes.add(res.getVerificationCode());
    				}	
    			}
    		}
    	}
    	if(BistroClient.waitingList != null && !BistroClient.waitingList.isEmpty()) {
    		for(Visit visit : BistroClient.waitingList) {
    			if(visit.getVerificationCode() != null && !visit.getVerificationCode().isEmpty()) {
    				if(visit.getGuest().getPhoneNumber().equals(phoneNumber))
    					if(!codes.contains(visit.getVerificationCode())) {
        					codes.add(visit.getVerificationCode());
        				}
    			}
    		}
    	}
		if(!codes.isEmpty()) {
			codesComboBox.getItems().setAll(codes);
			codesComboBox.setVisible(true);
			codesComboBox.setManaged(true);

			codesComboBox.setOnAction(e -> {
				String selected = codesComboBox.getValue();
				if(selected != null) {
					visitCodeField.setText(selected);
				}
			});
		}
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
        this.event = event;
        ClientUI.chat.accept(new BistroMessage(Action.GET_VERIFICATION_CODE, code));
        
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
        		BistroClient.visitInstance = (Visit)answer;
        		SceneLoader.switchScreen(
            		    event, 
            		    "/gui/VisitDetails.fxml", 
            		    "Visit Details", 
            		    (VisitDetailsController controller) -> {
            		        // This code runs after the controller is loaded
            		        controller.loadVisit(BistroClient.visitInstance);
            		    }
            		);
        	}
    	});
    }

    // Handles the "Forgot my code" action.
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        Hyperlink sourceLink = (Hyperlink) event.getSource();

        if (!phoneEmailField.isVisible()) {
            phoneEmailField.setVisible(true);
            phoneEmailField.setManaged(true);
            sourceLink.setText("Send Recovery Email/SMS");
            statusLabel.setText(""); 

            if (BistroClient.memberInstance != null) {
                String memberPhone = BistroClient.memberInstance.getPhoneNumber();
                if (memberPhone != null) {
                    phoneEmailField.setText(memberPhone);
                }
            }
            return; 
        }

        String inputContact = phoneEmailField.getText().trim();
        if (inputContact.isEmpty()) {
            statusLabel.setText("Please enter your Phone Number or Email.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        ClientUI.chat.accept(new BistroMessage(Action.FORGOT_CODE, inputContact));
        statusLabel.setText("If the code exists, it has been sent to your Email/SMS.");
        statusLabel.setTextFill(Color.web("#1976d2"));
    }
    
    public void forgotenCode(String answer) {
    	Platform.runLater(()->{
    		if(answer.startsWith("Error")) {
    			SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", answer);
    		}else {
    			SceneLoader.showAlert(Alert.AlertType.INFORMATION, "SMS", answer);
    		}
    	});
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