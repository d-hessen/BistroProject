package handlers;

import client.BistroClient;
import dataLayer.Visit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * ReceiptScreenController manages the receipt display screen.
 * <p>
 * This controller is responsible for:
 * <ul>
 *   <li>Displaying visit and payment details after a successful payment</li>
 *   <li>Showing the visit number, table number, and total amount paid</li>
 *   <li>Handling navigation after the receipt is acknowledged</li>
 * </ul>
 */
public class ReceiptScreenController {

	/**
     * Label displaying the visit (reservation) number.
     */
    @FXML
    private Label reservationIdLabel;

    /**
     * Label displaying the table number.
     */
    @FXML
    private Label tableIdLabel;

    /**
     * Label displaying the total amount paid.
     */
    @FXML
    private Label amountLabel;

    /**
     * The visit instance associated with the completed payment.
     */
    private Visit paid;

    /**
     * Initializes the receipt screen after the FXML has been loaded.
     * <p>
     * Retrieves the paid visit from the client state and
     * populates the receipt labels accordingly.
     */
    @FXML
    public void initialize() {
    	paid = BistroClient.visitInstance;
        if (paid == null) {
            return;
        }

        reservationIdLabel.setText("Visit Number: " + paid.getVisitId());
        tableIdLabel.setText("Table Number: " + paid.getTable().getTableNumber());
        amountLabel.setText("Total Paid: ₪" + paid.getBillOfVisit().getFinalAmount());
    }

    /**
     * Handles the action when the "Finish" button is clicked.
     * <p>
     * If the user is a staff member, closes the current window.
     * Otherwise, navigates the client back to the dashboard.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void handleFinish(ActionEvent event) {
    	if(BistroClient.staffInstance != null) {
    		SceneLoader.closeWindow(event);
    	} else {
          	SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    	}

    }
    
    /**
     * Sets the paid visit instance manually.
     *
     * @param paid the {@link Visit} that was paid
     */
    public void visitPaid(Visit paid) {
    	this.paid = paid;
    }
}
