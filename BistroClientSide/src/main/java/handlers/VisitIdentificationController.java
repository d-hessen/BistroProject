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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Controller for the Visit Identification screen.
 * <p>
 * Responsible for identifying customers based on verification codes,
 * handling check in logic, recovery of lost codes, and navigation
 * to the visit details or waiting screens.
 */
public class VisitIdentificationController implements Initializable {

	/**
     * Text field for entering the visit verification code.
     */
    @FXML
    private TextField visitCodeField;

    /**
     * Button used to trigger the identification process.
     */
    @FXML
    private Button identifyButton;

    /**
     * Label used to display status and error messages.
     */
    @FXML
    private Label statusLabel;

    /**
     * Text field for entering phone number or email for recovery.
     */
    @FXML
    private TextField phoneEmailField;

    /**
     * ComboBox holding available verification codes for logged-in members.
     */
    @FXML
    private ComboBox<String> codesComboBox;

    /**
     * Static reference to the created visit after successful identification.
     */
    public static Visit created = null;
    
    /**
     * Stored action event used for navigation after asynchronous responses.
     */
    private ActionEvent event;
    
    /**
     * Initializes the controller after FXML loading.
     * Registers the controller instance and prepares UI state.
     *
     * @param arg0  the location used to resolve relative paths
     * @param arg1 the resources used to localize the root object
     */
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
    
    /**
     * Loads verification codes for a member based on phone number.
     *
     * @param phoneNumber the member's phone number
     */
    private void loadMemberVerificationCodes(String phoneNumber) {
    	List<String> codes = new ArrayList<>();
    	ClientUI.chat.accept(new BistroMessage(Action.GET_MEMBER_RESERVATIONS, phoneNumber));
    	ClientUI.chat.accept(new BistroMessage(Action.GET_WAITING_LIST, null));
    	ClientUI.chat.accept(new BistroMessage(Action.GET_ACTIVE_VISITS,null));
    	if(BistroClient.visitsList != null && !BistroClient.visitsList.isEmpty()) {
    		for (Visit visit : BistroClient.visitsList) {
				if(visit.getVerificationCode() != null && !visit.getVerificationCode().isEmpty()) {
					if(visit.getGuest().getPhoneNumber() != null) {
						if(visit.getGuest().getPhoneNumber().equals(phoneNumber))
						codes.add(visit.getVerificationCode());
					}
				}
			}
    	}
    	if(BistroClient.reservationsList != null && !BistroClient.reservationsList.isEmpty()) {
    		for(Reservation res : BistroClient.reservationsList) {
    			if(res.getVerificationCode() != null && !res.getVerificationCode().isEmpty()) {
    				if(res.getGuest().getPhoneNumber() != null && !codes.contains(res.getVerificationCode())) {
    					if(res.getGuest().getPhoneNumber().equals(phoneNumber))
    					codes.add(res.getVerificationCode());
    				}	
    			}
    		}
    	}
    	if(BistroClient.waitingList != null && !BistroClient.waitingList.isEmpty()) {
    		for(Visit visit : BistroClient.waitingList) {
    			if(visit.getVerificationCode() != null && !visit.getVerificationCode().isEmpty()) {
    				if(visit.getGuest().getPhoneNumber() != null)
    					if(!codes.contains(visit.getVerificationCode())) {
    						if(visit.getGuest().getPhoneNumber().equals(phoneNumber)) {
    							codes.add(visit.getVerificationCode());
    						}
        					
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

    /**
     * Handles visit identification when the user submits a verification code.
     *
     * @param event the action event triggered by the button
     * @throws IOException if scene loading fails
     */
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
    
    /**
     * Processes the server response for verification code lookup.
     * Response may be a Reservation, Visit, or error message.
     *
     * @param response the server response object
     */
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
  	  				
  	  		    // Reservation already active or approved – allow check in
					case seated:
					case approved:
						ClientUI.chat.accept(new BistroMessage(Action.CHECK_IN_CUSTOMER, reservation));
						break;
						
			    // Reservation exists but not yet approved
					case pending:
						SceneLoader.showAlert(Alert.AlertType.ERROR, "Reservation not approved", "Your reservation not approved yet.");
						return;
						
			    // Reservation was cancelled by user or system
					case cancelled:
						SceneLoader.showAlert(Alert.AlertType.ERROR, "Reservation cancelled", "Your reservation was cancelled.");
						return;
						
			    // Reservation expired due to no-show
					case no_show:
						SceneLoader.showAlert(Alert.AlertType.ERROR, "Reservation finished", "Your reservation finished.");
						return;
			    // Fallback for unexpected status values
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
    
    /**
     * Handles the result of a customer check in attempt.
     * Navigates to waiting screen or visit details accordingly.
     *
     * @param answer the server response after check-in
     */
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

    /**
     * Handles the "Forgot my code" flow.
     * Allows recovery of verification code via phone or email.
     *
     * @param event the action event triggered by the hyperlink
     */
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
    
    /**
     * Handles server response after a recovery request was sent.
     *
     * @param answer the server response message
     */
    public void forgotenCode(String answer) {
    	Platform.runLater(()->{
    		if(answer.startsWith("Error")) {
    			SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", answer);
    		}else {
    			SceneLoader.showAlert(Alert.AlertType.INFORMATION, "SMS", answer);
    		}
    	});
    }

    /**
     * Navigates back to the appropriate dashboard based on user role.
     *
     * @param event the action event triggered by the Back button
     */ 
    @FXML
    private void handleBack(ActionEvent event) {
    	if(BistroClient.staffInstance != null) {
    		SceneLoader.closeWindow(event);
    	}else {
    		SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    	}
    }
}