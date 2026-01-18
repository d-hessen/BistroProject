package handlers;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Staff;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

/**
 * Controller responsible for handling staff authentication.
 * <p>
 * This class manages the staff login flow, including:
 * <ul>
 *   <li>Validating login input</li>
 *   <li>Sending authentication requests to the server</li>
 *   <li>Handling successful and failed login attempts</li>
 * </ul>
 */
public class StaffLoginController {
	
	/**
     * Temporary Staff object used to validate login credentials.
     */
	private static Staff staffToCheck = new Staff(null,null,null,false);
	
	/**
     * Text field for entering the staff username.
     */
    @FXML
    private TextField usernameField;

    /**
     * Password field for entering the staff password.
     */
    @FXML
    private PasswordField passwordField;

    /**
     * Label used to display login error messages to the user.
     */
    @FXML
    private Label errorLabel;
    
    /**
     * Handles the login action triggered by the staff login button.
     * <p>
     * Validates input fields, sends a staff identification request to the server,
     * and navigates to the staff dashboard on successful authentication.
     *
     * @param event the action event triggered by the login button
     */
    @FXML
    private void handleLogin(ActionEvent event) {
    	String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }
        
        staffToCheck.setUsername(username);
        staffToCheck.setPassword(password);
        ClientUI.chat.accept(new BistroMessage(Action.STAFF_IDENTIFICATION, staffToCheck));
        //Only for check
        System.out.println("Staff login request sent for: " + username);
        if(BistroClient.staffInstance != null) {
        	SceneLoader.loadScene(event, "/gui/StaffDashboard.fxml", "Staff Dashboard");
        }
        else {
        	errorLabel.setText("Some details are wrong!");
        	usernameField.clear();
        	passwordField.clear();
        	staffToCheck = new Staff(null,null,null,false);
        	ClientUI.chat.accept(new BistroMessage(Action.DISCONNECT, null));
        	return;
        }

    }
    
    /**
     * Handles navigation back to the main page.
     * <p>
     * Triggered when the user clicks the "Back" button.
     *
     * @param event the action event triggered by the back button
     */
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/MainPage.fxml", "Main Page");
    }
    
    /**
     * Handles a failed staff login attempt.
     * <p>
     * Displays an informational alert describing the login error
     * and disconnects the client from the server.
     *
     * @param message the error message received from the server
     */
    public void staffNotLogged(String message) {
    	Platform.runLater(() -> {
    		Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Log In Error");
            alert.setHeaderText(null);
            alert.setContentText("There was an error logging in! Error: " +message);
            alert.showAndWait();
            ClientUI.chat.accept(new BistroMessage(Action.DISCONNECT, null));
        });
    }

}
