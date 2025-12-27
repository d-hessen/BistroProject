package handlers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;


// Controller for the Visit Identification screen.

public class VisitIdentificationController {

    @FXML
    private TextField visitCodeField;

    @FXML
    private Button identifyButton;

    @FXML
    private Label statusLabel;


    // Handles the identification process when the user clicks the "Identify Visit" button.
    @FXML
    private void handleIdentification(ActionEvent event) {
        String code = visitCodeField.getText().trim();

        // Validate that the code field is not empty
        if (code.isEmpty()) {
            statusLabel.setText("Please enter your visit code.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        /* TODO:
         * 1. Send the code to the server via BistroClient.
         * 2. If valid and table is ready than Navigate to VisitDetails.fxml.
         * 3. If valid but table is not ready than Navigate to ClientWaiting.fxml.
         * 4. If invalid than Display error message in statusLabel.
         */
        
        //For check only
        System.out.println("Attempting to identify visit with code: " + code);
    }

    // Handles the "Forgot my code" action.
    @FXML
    private void handleForgotPassword(ActionEvent event) {
    	// TODO: send the client code via email and SMS
        statusLabel.setText("If the code exists, it has been sent to your Email/SMS.");
        statusLabel.setTextFill(Color.web("#1976d2"));
    }


    // Navigates back to the previous dashboard.
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/GuestDashboard.fxml", "Guest Dashboard");
    }
}