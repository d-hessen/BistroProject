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
    private Label priceLabel;
    
    private Visit visitToPay = null;
    public static boolean billWasPaid = false;
    public static boolean updateVisit = false;
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
        // TODO: Send to server that bill was paid
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

    // Handles "Back" button click
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

	public void payVisit(Visit visitInstance) {
		visitToPay = visitInstance;
		
	}
}
