package handlers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Controller for the New Reservation screen.
 * Validates user input for email, phone, date, and table size.
 */
public class MakeReservationController {

    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker datePicker;
    @FXML private TextField dinersField;
    @FXML private Label errorLabel;

    // Regular expression for email validation 
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    
    // Handles the transition to the time selection screen.
    @FXML
    private void handleSelectTime(ActionEvent event) {
        // Clear previous error message 
        errorLabel.setText("");

        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        LocalDate date = datePicker.getValue();
        String diners = dinersField.getText().trim();

        // Validate Email format
        if (email.isEmpty() || !Pattern.compile(EMAIL_REGEX).matcher(email).matches()) {
            errorLabel.setText("Please enter a valid email address (e.g., name@example.com).");
            return;
        }

        // Validate Phone: Must contain only digits
        if (phone.isEmpty() || !phone.matches("\\d+")) {
            errorLabel.setText("Phone number must contain digits only.");
            return;
        }

        // Validate Date: Must be selected and not in the past
        if (date == null) {
            errorLabel.setText("Please select a date for your reservation.");
            return;
        }
        if (date.isBefore(LocalDate.now())) {
            errorLabel.setText("Reservation date cannot be in the past.");
            return;
        }

        // Validate Diners: Must be a valid positive number
        if (diners.isEmpty()) {
            errorLabel.setText("Please enter the number of diners.");
            return;
        }
        try {
            int numDiners = Integer.parseInt(diners);
            if (numDiners <= 0) {
                errorLabel.setText("Number of diners must be at least 1.");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Diners must be a numeric value.");
            return;
        }

        // If all validations pass, proceed to Time Slot selection
        System.out.println("Validation successful. Proceeding to select time for: " + date);
        
        //TODO: Save fields and pass to the next page for ReservationDetails
        
        SceneLoader.loadScene(event, "/gui/TimeSlot.fxml", "Select Time Slot");
    }


     //Navigates back to the Guest Dashboard.
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/GuestDashboard.fxml", "Guest Dashboard");
    }
}