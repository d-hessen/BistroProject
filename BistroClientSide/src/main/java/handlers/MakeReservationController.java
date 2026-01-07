package handlers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import client.BistroClient;
import dataLayer.Member;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class MakeReservationController implements Initializable {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker datePicker;
    @FXML private TextField dinersField;
    @FXML private Label errorLabel;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Auto-fill member details if logged in
        if (BistroClient.memberInstance != null) {
            Member member = BistroClient.memberInstance;

            fullNameField.setText(member.getFullName());
            emailField.setText(member.getEmail());
            phoneField.setText(member.getPhoneNumber());

            // Lock fields – member data is source of truth
            fullNameField.setDisable(true);
            emailField.setDisable(true);
            phoneField.setDisable(true);
        }
    }

    @FXML
    private void handleSelectTime(ActionEvent event) {

        errorLabel.setText("");

        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        LocalDate date = datePicker.getValue();
        String diners = dinersField.getText().trim();

        // Full name validation
        if (fullName.isEmpty() || fullName.length() < 2) {
            errorLabel.setText("Please enter your full name.");
            return;
        }
        if (!fullName.matches("[A-Za-z ]+")) {
            errorLabel.setText("Full name must contain letters only.");
            return;
        }

        // Email validation
        if (email.isEmpty() || !Pattern.compile(EMAIL_REGEX).matcher(email).matches()) {
            errorLabel.setText("Please enter a valid email address.");
            return;
        }

        // Phone validation
        if (phone.isEmpty() || !phone.matches("\\d+")) {
            errorLabel.setText("Phone number must contain digits only.");
            return;
        }

        // Date validation
        if (date == null) {
            errorLabel.setText("Please select a date.");
            return;
        }
        if (date.isBefore(LocalDate.now())) {
            errorLabel.setText("Reservation date cannot be in the past.");
            return;
        }

        // Diners validation
        int numDiners;
        try {
            numDiners = Integer.parseInt(diners);
            if (numDiners <= 0) {
                errorLabel.setText("Number of diners must be at least 1.");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Diners must be numeric.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/TimeSlot.fxml"));
            Parent root = loader.load();

            TimeSlotController controller = loader.getController();
            controller.initData(date, fullName, email, phone, numDiners);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Select Time Slot");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load time selection screen.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        if (BistroClient.memberInstance != null) {
            SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
        } else {
            SceneLoader.closeWindow(event);
        }
    }
}
