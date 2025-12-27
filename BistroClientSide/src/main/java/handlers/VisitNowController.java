package handlers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.util.regex.Pattern;


 // Controller for the VisitNow screen.
 // Handles walk-in customers and validates contact information.

public class VisitNowController {

    @FXML private TextField contactField;
    @FXML private TextField dinersField;
    @FXML private VBox confirmationArea;
    @FXML private Label generatedCodeLabel;
    @FXML private Button generateCodeBtn;

    // Regular expression for basic email validation
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";


    // Handles the "Get Confirmation Code" button click.
    // Validates that the input is either a valid phone number or a valid email.
    @FXML
    private void handleGenerateCode(ActionEvent event) {
        String contact = contactField.getText().trim();
        String diners = dinersField.getText().trim();

        // Validate that fields are not empty
        if (contact.isEmpty() || diners.isEmpty()) {
            showAlert("Input Error", "All fields are required. Please fill in your contact info and number of diners.");
            return;
        }

        // Validate Contact: Check if it's a valid Email or a valid Phone Number
        boolean isEmail = Pattern.compile(EMAIL_REGEX).matcher(contact).matches();
        boolean isPhone = contact.matches("\\d{9,10}"); // Validates phone as 9 or 10 digits

        if (!isEmail && !isPhone) {
            // Provide specific feedback based on what the user tried to enter
            if (contact.contains("@")) {
                showAlert("Invalid Email", "The email format you entered is incorrect.");
            } else {
                showAlert("Invalid Phone", "Phone number must contain only digits (9-10 digits).");
            }
            return;
        }

        // Validate Diners: Must be a positive integer
        try {
            int numDiners = Integer.parseInt(diners);
            if (numDiners <= 0) {
                showAlert("Input Error", "Number of diners must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid number for diners.");
            return;
        }

        //TODO: Logic for Server Communication
        
        System.out.println("Validations passed for: " + contact);

        // Generate a mock code
        String mockCode = "V-" + (int)(Math.random() * 9000 + 1000);
        generatedCodeLabel.setText(mockCode);
        
        confirmationArea.setVisible(true);
        generateCodeBtn.setDisable(true); // Disable button after successful generation
    }


    // Navigates back to the previous screen.
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/GuestDashboard.fxml", "Guest Dashboard");
    }


    // method to show an alert dialog.
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}