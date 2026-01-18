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
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * MemberLoginController manages the member login screen.
 * <p>
 * This controller is responsible for:
 * <ul>
 *   <li>Handling member authentication requests</li>
 *   <li>Validating login input (email or phone number)</li>
 *   <li>Sending identification requests to the server</li>
 *   <li>Navigating to the client dashboard upon successful login</li>
 * </ul>
 */
public class MemberLoginController {
    /**
     * Temporary {@link Member} object used to validate login credentials.
     */
	private Member memberToCheck = new Member(null,null,null,null);
	
    /**
     * Regular expression for validating email addresses.
     */
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /**
     * Regular expression for validating phone numbers (10 digits).
     */
    private static final String PHONE_REGEX = "^\\d{10}$"; 

    /**
     * Text field for entering username (email or phone number).
     */
    @FXML
    private TextField usernameField;

    /**
     * Password field for entering the member password.
     */
    @FXML
    private PasswordField passwordField;

    /**
     * Label used to display validation or error messages.
     */
    @FXML
    private Label errorLabel;

    /**
     * Handles the action when the "Login" button is clicked.
     * <p>
     * Validates user input, determines whether the username is an
     * email or phone number, sends a login request to the server,
     * and navigates to the client dashboard if authentication succeeds.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    void handleLogin(ActionEvent event) {
    	String rawInput = usernameField.getText();
        String password = passwordField.getText();

        if (rawInput.isEmpty() || password.isEmpty()) {
            errorMessage("Please enter both username and password.");
            return;
        }
        memberToCheck = new Member(null,null,null,null);
        checkInputType(rawInput);
        memberToCheck.setPassword(password);
        ClientUI.chat.accept(new BistroMessage(Action.MEMBER_IDENTIFICATION, memberToCheck));
        //Only for check
        System.out.println("Login request sent for: " + rawInput);
        if(BistroClient.memberInstance != null) {
        	SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
        }
        else {
        	errorMessage("Some details are wrong!");
        	memberToCheck = new Member(null,null,null,null);
        	ClientUI.chat.accept(new BistroMessage(Action.DISCONNECT, null));
        	return;
        }

    }
    
    /**
     * Displays an error message using an alert dialog.
     *
     * @param message the error message to display
     */
    private void errorMessage(String message) {
    	SceneLoader.showAlert(Alert.AlertType.ERROR, "Login error", message);
    }
    
    /**
     * Checks whether the given string is a valid email address.
     *
     * @param email the input string
     * @return {@code true} if the string matches the email pattern,
     *         {@code false} otherwise
     */
    public static boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Checks whether the given string is a valid phone number (10 digits).
     * <p>
     * Non digit characters are removed before validation.
     *
     * @param phoneNumber the input string
     * @return {@code true} if the string matches the phone number pattern,
     *         {@code false} otherwise
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        // Remove any common formatting like dashes or spaces before validation
        String normalizedNumber = phoneNumber.replaceAll("[\\D]", ""); 
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(normalizedNumber);
        return matcher.matches();
    }
    
    /**
     * Determines the type of user input and assigns it to the
     * corresponding field in the {@link Member} object.
     * <p>
     * The input may represent either an email address or a phone number.
     *
     * @param input the user input string
     */
    public void checkInputType(String input) {
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
    
    /**
     * Handles the action when the "Back" button is clicked.
     * <p>
     * Navigates the user back to the membership options screen.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/IsMemberGUI.fxml", "Membership Options");
    }
}