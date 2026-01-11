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

public class MakeReservationController implements Initializable {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker datePicker;
    @FXML private TextField dinersField;
    @FXML private Label errorLabel; // Ensure this matches fx:id in FXML
    
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

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

    // Matches onAction="#handleSelectTime" in your FXML
    @FXML
    private void handleSelectTime(ActionEvent event) {
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

    // Matches onAction="#handleBack" in your FXML
    @FXML
    private void handleBack(ActionEvent event) {
        // FIX for Double Windows:
        
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