package handlers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class PaymentScreenController {

    @FXML
    private TextField cardNumberField;

    @FXML
    private TextField expiryDateField;

    @FXML
    private TextField cvvField;

    @FXML
    private TextField idNumberField;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        statusLabel.setVisible(false);
    }

    /**
     * Handles "Pay Now" button click
     */
    @FXML
    private void handleProcessPayment(ActionEvent event) {

        String cardNumber = cardNumberField.getText().trim();
        String expiryDate = expiryDateField.getText().trim();
        String cvv = cvvField.getText().trim();
        String idNumber = idNumberField.getText().trim();

        // Basic validation
        if (cardNumber.isEmpty() || expiryDate.isEmpty()
                || cvv.isEmpty() || idNumber.isEmpty()) {

            showError("Please fill in all fields");
            return;
        }

        if (!cardNumber.matches("\\d{16}")) {
            showError("Card number must be 16 digits");
            return;
        }

        if (!expiryDate.matches("\\d{2}/\\d{2}")) {
            showError("Expiry date must be MM/YY");
            return;
        }

        if (!cvv.matches("\\d{3}")) {
            showError("CVV must be 3 digits");
            return;
        }

        // ✔ Payment accepted (mock)
        showSuccess("Payment completed successfully");

        // TODO: Send to server and move to recipe screen
    }


    // Handles "Back" button click
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/VisitDetails.fxml", "Visit Details");
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.RED);
        statusLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.web("#2e7d32"));
        statusLabel.setVisible(true);
    }
}
