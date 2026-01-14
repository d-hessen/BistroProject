package handlers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Guest;
import dataLayer.Visit;


 // Controller for the VisitNow screen.
 // Handles walk-in customers and validates contact information.

public class VisitNowController implements Initializable {

    @FXML private TextField contactField;
    @FXML private TextField dinersField;
    @FXML private TextField fullNameField;
    @FXML private VBox confirmationArea;
    @FXML private Label generatedCodeLabel;
    @FXML private Button generateCodeBtn;

    // Regular expression for basic email validation
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private boolean hasAssignedTable;
    private boolean isWaiting;
    private Visit createdVisit;
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		BistroClient.visitNowControllerInstance = this;
		if(BistroClient.memberInstance != null) {
			contactField.setText(BistroClient.memberInstance.getPhoneNumber());
			fullNameField.setText(BistroClient.memberInstance.getFullName());
			contactField.setDisable(true);
			fullNameField.setDisable(true);
		}
		
	}
    // Handles the "Get Confirmation Code" button click.
    // Validates that the input is either a valid phone number or a valid email.
    @FXML
    private void handleGenerateCode(ActionEvent event) {
        String contact = contactField.getText().trim();
        String diners = dinersField.getText().trim();
        String fullName = fullNameField.getText().trim();

        // Validate that fields are not empty
        if (contact.isEmpty() || diners.isEmpty() || fullName.isEmpty()) {
        	SceneLoader.showAlert(Alert.AlertType.ERROR,"Input Error", "All fields are required. Please fill in your contact info and number of diners.");
            return;
        }

        // Validate Contact: Check if it's a valid Email or a valid Phone Number
        boolean isEmail = Pattern.compile(EMAIL_REGEX).matcher(contact).matches();
        boolean isPhone = contact.matches("\\d{9,10}"); // Validates phone as 9 or 10 digits only

        if (!isEmail && !isPhone) {
            // Provide specific feedback based on what the user tried to enter
            if (contact.contains("@")) {
                SceneLoader.showAlert(Alert.AlertType.ERROR,"Invalid Email", "The email format you entered is incorrect.");
            } else {
            	SceneLoader.showAlert(Alert.AlertType.ERROR,"Invalid Phone", "Phone number must contain only digits (9-10 digits).");
            }
            return;
        }

        // Validate Diners: Must be a positive integer
        try {
            int numDiners = Integer.parseInt(diners);
            if (numDiners <= 0) {
            	SceneLoader.showAlert(Alert.AlertType.ERROR,"Input Error", "Number of diners must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
        	SceneLoader.showAlert(Alert.AlertType.ERROR,"Input Error", "Please enter a valid number for diners.");
            return;
        }
        
        // Validate Full Name: Must contain at least two words
        String[] nameParts = fullName.split("\\s+");
        if (nameParts.length < 2) {
        	SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Full name must contain at least two words.");
            return;
        }
        
        System.out.println("Validations passed for: " + contact);
        Visit toCreate;
        if(BistroClient.memberInstance != null) {
        	toCreate = new Visit(BistroClient.memberInstance,null);
        } else {
            Guest guest = new Guest(fullName,null,null);
            if(isPhone) {
            	 guest.setPhoneNumber(contact);
            }else {
            	guest.setEmail(contact);
            }
            toCreate = new Visit(guest,null);
        }
        toCreate.setPartySize(Integer.valueOf(diners));
        ClientUI.chat.accept(new BistroMessage(Action.VISIT_NOW, toCreate));
        if(hasAssignedTable) {
        	hasAssignedTable = false;
        	SceneLoader.showAlert(Alert.AlertType.INFORMATION, "You can take table now!", "Your verification code is: " +createdVisit.getVerificationCode() +"\nUse it in order to pay for your visit!\n Bon Appetit!");
        	SceneLoader.switchScreen(
        		    event, 
        		    "/gui/VisitDetails.fxml", 
        		    "Visit Details", 
        		    (VisitDetailsController controller) -> {
        		        // This code runs after the controller is loaded
        		        controller.loadVisit(createdVisit);
        		    }
        		);
        }
        if(isWaiting) {
        	isWaiting = false;
        	SceneLoader.loadScene(event, "/gui/ClientWaiting.fxml", "Waiting");
        }
    }

    // Navigates back to the previous screen.
    @FXML
    private void handleBack(ActionEvent event) {
    	if(BistroClient.staffInstance != null) {
    		SceneLoader.closeWindow(event);
    	}else {
    		SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    	}
    }
    
    public void walkInVisitNotCreated(String message) {
    	Platform.runLater(() -> {
    		if(message.startsWith("Error")) {
        		SceneLoader.showAlert(Alert.AlertType.ERROR, "Creating walk-in visit failed", message);
    		}else if(message.startsWith("Wait")) {
    			isWaiting = true;
    		}
    		else {
        		generatedCodeLabel.setText(message); //Show verification code to identify when table is ready
                confirmationArea.setVisible(true);
                generateCodeBtn.setDisable(true); // Disable button after successful generation
    		}
    		if(BistroClient.memberInstance == null && BistroClient.staffInstance == null && createdVisit == null) {
    			ClientUI.chat.accept(new BistroMessage(Action.DISCONNECT, null));
    		}

    	});
    }
    
    public void walkInVisitWaiting(Visit visit) {
        BistroClient.waitingVisit = visit;
        isWaiting = true;
        
        Platform.runLater(() -> {
            SceneLoader.showAlert(Alert.AlertType.INFORMATION, "Added to Waiting List", 
                "No tables available.\nVerification Code: " + visit.getVerificationCode());
        });
    }
    
    public void walkInVisitCreated(Visit visit) {
    	createdVisit = visit;
		hasAssignedTable = true;
    }
}