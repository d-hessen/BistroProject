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

    @FXML
    public void initialize() {

        Visit visit = BistroClient.currentVisit;
        if (visit == null) {
            return;
        }

        reservationIdLabel.setText("Reservation ID: " + visit.getReservation().getReservationId());

        tableIdLabel.setText("Table Number: " + visit.getTable().getTableNumber());

        //amountLabel.setText(String.format("Total Paid: ₪%.2f",visit.getBillOfVisit().getFinalAmount()));
    }

    @FXML
    private void handleFinish(ActionEvent event) {
        BistroClient.currentVisit = null;
      	 SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Payment Screen");
    }
}
