package handlers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.util.regex.Pattern;

import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Member;


 //Controller for the Member Sign Up screen.
 
public class MemberSignUpController {

    @FXML private TextField PhoneNumberField;
    @FXML private TextField EmailField;
    @FXML private TextField FullNameField;
    
    // Dual fields for password toggle functionality 
    @FXML private PasswordField PasswordField;
    @FXML private TextField PasswordFieldVisible;
    
    @FXML private CheckBox showPasswordTick;
    @FXML private Label errorLabel;

    // Regular expression for basic email validation
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    // Toggles the password visibility between masked and plain text.
    @FXML
    private void togglePasswordVisible(ActionEvent event) {
        if (showPasswordTick.isSelected()) {
            // Switch to visible text field
            PasswordFieldVisible.setText(PasswordField.getText());
            PasswordFieldVisible.setVisible(true);
            PasswordFieldVisible.setManaged(true);
            PasswordField.setVisible(false);
            PasswordField.setManaged(false);
        } else {
            // Switch back to masked password field
            PasswordField.setText(PasswordFieldVisible.getText());
            PasswordField.setVisible(true);
            PasswordField.setManaged(true);
            PasswordFieldVisible.setVisible(false);
            PasswordFieldVisible.setManaged(false);
        }
    }

    // Validates input fields and submits the registration form.
    @FXML
    private void handleSubmit(ActionEvent event) {
        // Reset error message
        errorLabel.setText(""); 
        
        String phone = PhoneNumberField.getText().trim();
        String email = EmailField.getText().trim();
        String name = FullNameField.getText().trim();
        
        // Get password from the currently active field
        String pass = showPasswordTick.isSelected() ? PasswordFieldVisible.getText() : PasswordField.getText();

        // Validate Phone Number: Must contain digits only
        if (!phone.matches("\\d+")) {
            errorLabel.setText("Phone number must contain numbers only.");
            return;
        }

        // Validate Email: Must follow standard email format
        if (!Pattern.compile(EMAIL_REGEX).matcher(email).matches()) {
            errorLabel.setText("Invalid email format.");
            return;
        }

        // Validate Full Name: Must contain at least two words
        String[] nameParts = name.split("\\s+");
        if (nameParts.length < 2) {
            errorLabel.setText("Full name must contain at least two words.");
            return;
        }

        // Validate Password: Must not be empty
        if (pass.isEmpty()) {
            errorLabel.setText("Password cannot be empty.");
            return;
        }

        // If all validations pass, proceed with registration logic
        System.out.println("Validation successful for user: " + name);
        
        Member memberToCreate = new Member(name,phone,email,pass);
        memberToCreate.setCardCode("CARD-" + (int)(Math.random() * 9000 + 1000));
        ClientUI.chat.accept(new BistroMessage(Action.CREATE_MEMBER, memberToCreate));
        PhoneNumberField.setText("");
        EmailField.setText("");
        FullNameField.setText("");
        PasswordField.setText("");
    }

    public void memberCreated(boolean isCreated, String message) {
    	Platform.runLater(() -> {
    		Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Registration Status");
            alert.setHeaderText(null);
            if(isCreated) {
            	alert.setContentText("Member has been signed up successfully!");
            } else {
            	alert.setContentText("There was an error signing up member! Error: " +message);
            }
            alert.showAndWait();
            ClientUI.chat.accept(new BistroMessage(Action.DISCONNECT, null));
        });
    }
    
        
    // Navigates back to the membership options screen.
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/IsMemberGUI.fxml", "Membership Options");
    }
}