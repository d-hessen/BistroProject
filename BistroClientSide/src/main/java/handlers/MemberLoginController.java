package handlers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class MemberLoginController {

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

        //TODO: Server Logic
        
        //Only for check
        System.out.println("Login attempted with: " + username);
    }


    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/IsMemberGUI.fxml", "Membership Options");
    }
}