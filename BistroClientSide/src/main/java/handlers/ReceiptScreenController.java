package handlers;

import client.BistroClient;
import dataLayer.Visit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ReceiptScreenController {

    @FXML
    private Label reservationIdLabel;

    @FXML
    private Label tableIdLabel;

    @FXML
    private Label amountLabel;
    
    private Visit paid;

    @FXML
    public void initialize() {
    	paid = VisitDetailsController.visitInstance;
        if (paid == null) {
            return;
        }

        reservationIdLabel.setText("Visit Number: " + paid.getVisitId());
        tableIdLabel.setText("Table Number: " + paid.getTable().getTableNumber());
        amountLabel.setText("Total Paid: ₪" + paid.getBillOfVisit().getFinalAmount());
    }

    @FXML
    private void handleFinish(ActionEvent event) {
    	//TODO
    	//ADD LOGIC OF LEAVING TABLE
      	SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Payment Screen");
    }
    
    public void visitPaid(Visit paid) {
    	this.paid = paid;
    }
}
