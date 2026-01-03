package handlers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class IsMemberController {

    // Handles the action when "Log In" button is clicked.
    @FXML
    private void openClientOptions(ActionEvent event) {
    	 SceneLoader.loadScene(event, "/gui/MemberLoginGUI.fxml", "Member Login");
    }

    // Handles the action when "Sign Up" button is clicked.
    @FXML
    private void openStaffOptions(ActionEvent event) {
    	 SceneLoader.loadScene(event, "/gui/MemberSignUp.fxml", "Member Sign Up");
    }
    
    // Handles the action when "Join As Guest" button is clicked.
    @FXML
    private void openGuestOptions(ActionEvent event) {
    	 SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    }
    
    @FXML
    private void handleBack(ActionEvent event) {
    	SceneLoader.loadScene(event, "/gui/MainPage.fxml", "Client Dashboard");
    }

}