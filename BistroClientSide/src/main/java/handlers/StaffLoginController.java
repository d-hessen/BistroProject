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

public class StaffLoginController {
	private static Staff staffToCheck = new Staff(null,null,null,false);
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;
    
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
    
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/MainPage.fxml", "Main Page");
    }
    
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
