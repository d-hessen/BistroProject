package handlers;

import client.BistroClient;
import dataLayer.Guest;
import dataLayer.Member;
import dataLayer.Reservation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * MakeReservationController manages the reservation creation screen.
 * <p>
 * This controller is responsible for:
 * <ul>
 *   <li>Collecting reservation details from the user</li>
 *   <li>Validating user input</li>
 *   <li>Pre filling member information when a member is logged in</li>
 *   <li>Navigating to the time slot selection screen</li>
 * </ul>
 *
 * <p>
 * The controller implements {@link Initializable} to configure
 * the UI state after the FXML has been loaded.
 */
public class MakeReservationController implements Initializable {

	/**
     * Text field for entering or displaying the full name.
     */
    @FXML private TextField fullNameField;

    /**
     * Text field for entering or displaying the email address.
     */
    @FXML private TextField emailField;

    /**
     * Text field for entering or displaying the phone number.
     */
    @FXML private TextField phoneField;

    /**
     * Date picker for selecting the reservation date.
     */
    @FXML private DatePicker datePicker;

    /**
     * Text field for entering the number of diners.
     */
    @FXML private TextField dinersField;

    /**
     * Label used to display validation or error messages.
     */
    @FXML private Label errorLabel;

    /**
     * Regular expression used for email validation.
     */
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    /**
     * Initializes the reservation screen after the FXML has been loaded.
     * <p>
     * If a member is logged in, their personal details are automatically
     * filled in and the corresponding fields are disabled to prevent edits.
     *
     * @param location the location used to resolve relative paths
     * @param resources the resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Auto-fill member details if logged in
        if (BistroClient.memberInstance != null) {
            Member member = BistroClient.memberInstance;

            fullNameField.setText(member.getFullName());
            emailField.setText(member.getEmail());
            phoneField.setText(member.getPhoneNumber());

            // Disable fields so member cannot change registered details here
            fullNameField.setDisable(true);
            emailField.setDisable(true);
            phoneField.setDisable(true);
        }
    }

    /**
     * Handles the action when the "Select Time" button is clicked.
     * <p>
     * Collects user input, validates all reservation fields, and
     * navigates to the time slot selection screen if validation succeeds.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void handleSelectTime(ActionEvent event) {
        // Clear previous error message
        errorLabel.setText("");

        // 1. Collect Data
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        LocalDate date = datePicker.getValue();
        String dinersStr = dinersField.getText().trim();

        // 2. Validation
        if (fullName.isEmpty()) {
            errorLabel.setText("Please enter full name.");
            return;
        }
        if (email.isEmpty() || !Pattern.compile(EMAIL_REGEX).matcher(email).matches()) {
            errorLabel.setText("Invalid email address.");
            return;
        }
        if (phone.isEmpty()) {
            errorLabel.setText("Please enter phone number.");
            return;
        }
        if (date == null) {
            errorLabel.setText("Please select a date.");
            return;
        }
        if (date.isBefore(LocalDate.now())) {
            errorLabel.setText("Date cannot be in the past.");
            return;
        }
        
        int numDiners;
        try {
            numDiners = Integer.parseInt(dinersStr);
            if (numDiners <= 0) {
                errorLabel.setText("Diners must be > 0.");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Diners must be a number.");
            return;
        }

        // 3. Navigate to TimeSlot Screen (Pass data forward)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/TimeSlot.fxml"));
            Parent root = loader.load();
            
            // Get the next controller and pass the data
            TimeSlotController controller = loader.getController();
            controller.initData(date, fullName, email, phone, numDiners);
            
            // Replace the scene in the current window
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Select Time Slot");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Error loading time selection screen.");
        }
    }

    /**
     * Handles the action when the "Back" button is clicked.
     * <p>
     * If the screen was opened by a staff member, the current window
     * is closed. Otherwise, the client is navigated back to the
     * client dashboard.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void handleBack(ActionEvent event) {
        
        // If it is a Staff member, they opened this as a Popup. We should CLOSE it.
        if (BistroClient.staffInstance != null) {
            SceneLoader.closeWindow(event);
        } 
        // If it is a Client (Member or Guest), they are in the Main Window. We go back to Dashboard.
        else {
            SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
        }
    }
}