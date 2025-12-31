package handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Member;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class MemberLoginController {
	private static Member memberToCheck = new Member(null,null,null,null);
    // Simple email regex.
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    //Phone number regex for a simple 10-digit number. 
    private static final String PHONE_REGEX = "^\\d{10}$"; 

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
    	String rawInput = usernameField.getText();
        String password = passwordField.getText();

        if (rawInput.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }
        
        checkInputType(rawInput);
        memberToCheck.setPassword(password);
        ClientUI.chat.accept(new BistroMessage(Action.MEMBER_IDENTIFICATION, memberToCheck));
        //Only for check
        System.out.println("Login request sent for: " + rawInput);
        if(BistroClient.memberInstance != null) {
        	SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
        }
        else {
        	errorLabel.setText("Some details are wrong!");
        	usernameField.setText("");
        	passwordField.setText("");
        	memberToCheck = new Member(null,null,null,null);
        	ClientUI.chat.accept(new BistroMessage(Action.DISCONNECT, null));
        	return;
        }

    }
    /**
     * Checks if the input string is a valid email address.
     * @param email The input string.
     * @return true if valid, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Checks if the input string is a valid phone number (10 digits).
     * @param phoneNumber The input string.
     * @return true if valid, false otherwise.
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        // Remove any common formatting like dashes or spaces before validation
        String normalizedNumber = phoneNumber.replaceAll("[\\D]", ""); 
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(normalizedNumber);
        return matcher.matches();
    }
    
    /**
     * Determines the type of user input.
     * @param input The user input string.
     */
    public static void checkInputType(String input) {
        if (isValidEmail(input)) {
        	memberToCheck.setEmail(input);
            System.out.println("The input is an email: " + input);
        } else if (isValidPhoneNumber(input)) {
        	memberToCheck.setPhoneNumber(input);
            System.out.println("The input is a phone number: " + input);
        } else {
            System.out.println("The input is neither a valid email nor a valid 10-digit phone number: " + input);
        }
    }
    
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/IsMemberGUI.fxml", "Membership Options");
    }
}