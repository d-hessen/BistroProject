package handlers;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Visit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 * PaymentScreenController manages the payment processing screen.
 * <p>
 * This controller is responsible for:
 * <ul>
 *   <li>Displaying visit payment details</li>
 *   <li>Validating credit card input</li>
 *   <li>Sending payment confirmation to the server</li>
 *   <li>Navigating to the receipt or previous screens</li>
 * </ul>
 */
public class PaymentScreenController {

	/**
     * Text field for entering the credit card number.
     */
    @FXML
    private TextField cardNumberField;

    /**
     * Text field for entering the card expiration date (MM/YY).
     */
    @FXML
    private TextField expiryDateField;

    /**
     * Text field for entering the CVV code.
     */
    @FXML
    private TextField cvvField;

    /**
     * Text field for entering the ID number.
     */
    @FXML
    private TextField idNumberField;

    /**
     * Label used to display payment status messages.
     */
    @FXML
    private Label statusLabel;

    /**
     * Label displaying the total price to be paid.
     */
    @FXML
    private Label priceLabel;

    /**
     * Visit instance associated with the current payment.
     */
    private Visit visitToPay = null;

    /**
     * Indicates whether the bill was successfully paid.
     */
    public static boolean billWasPaid = false;

    /**
     * Flag used to indicate whether the visit should be updated.
     */
    public static boolean updateVisit = false;

    /**
     * Initializes the payment screen after the FXML has been loaded.
     * <p>
     * Requests the visit verification code, loads the visit details,
     * and displays the total payment amount.
     */
    @FXML
    public void initialize() {
        statusLabel.setVisible(false);
        updateVisit = true;
        ClientUI.chat.accept(new BistroMessage(Action.GET_VERIFICATION_CODE, VisitDetailsController.visitInstance.getVerificationCode()));
        visitToPay = BistroClient.visitInstance;
        if (visitToPay != null) {
            String price = String.valueOf(visitToPay.getBillOfVisit().getFinalAmount());
            priceLabel.setText("Total Price: ₪"+price);
        }
    }

    /**
     * Handles the action when the "Pay Now" button is clicked.
     * <p>
     * Performs basic validation on payment details, updates the visit
     * bill status, and sends a payment confirmation to the server.
     *
     * @param event the action event triggered by the button click
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
        
        Visit toSend = visitToPay;
        toSend.getBillOfVisit().setPaid(true);
        ClientUI.chat.accept(new BistroMessage(Action.BILL_PAID,toSend));
        if(billWasPaid) {
        	billWasPaid = false;
        	VisitSessionManager.clear();
            showSuccess("Payment completed successfully");
            SceneLoader.loadScene(event, "/gui/ReceiptScreen.fxml", "Receipt");
        }
    }

    /**
     * Handles the action when the "Back" button is clicked.
     * <p>
     * Navigates the user back to the appropriate screen based on
     * whether the user is staff or a client.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void handleBack(ActionEvent event) {
    	updateVisit = false;
    	if(BistroClient.staffInstance != null) {
    		SceneLoader.switchScreen(event
					,"/gui/TableManagement.fxml"
					,"Manage Table " + visitToPay.getTable().getTableNumber()
					,(TableManagementController controller) -> {
						// This code runs after the controller is loaded
						controller.setTableDetails(TableManagementController.currentTable);
					});
    	}
    	else {
    		SceneLoader.switchScreen(
        		    event, 
        		    "/gui/VisitDetails.fxml", 
        		    "Visit Details", 
        		    (VisitDetailsController controller) -> {
        		        // This code runs after the controller is loaded
        		        controller.loadVisit(visitToPay);
        		    }
        		);
    	}
    }
    
    /**
     * Displays an error message on the payment screen.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.RED);
        statusLabel.setVisible(true);
    }
    
    /**
     * Displays a success message on the payment screen.
     *
     * @param message the success message to display
     */
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.web("#2e7d32"));
        statusLabel.setVisible(true);
    }

    /**
     * Sets the visit instance to be paid.
     *
     * @param visitInstance the visit associated with the payment
     */
	public void payVisit(Visit visitInstance) {
		visitToPay = visitInstance;
		
	}
}
