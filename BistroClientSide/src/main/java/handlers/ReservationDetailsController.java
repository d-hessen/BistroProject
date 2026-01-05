package handlers;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Reservation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import java.time.LocalDate;

// Controller for the Reservation Details screen.
public class ReservationDetailsController {

    @FXML private TextField orderIdField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField dinersField;
    @FXML private TextField statusField;

    /**
     * This method is called automatically when the FXML is loaded.
     * It pulls the data from the static instance in BistroClient.
     */
    @FXML
    public void initialize() {
        Reservation currentRes = BistroClient.reservationInstance;
        if (currentRes != null) {
            if (currentRes.getReservationId() != null) {
                orderIdField.setText(String.valueOf(currentRes.getVerificationCode()));
            }
            if (currentRes.getReservationDate() != null) {
                try {
                    String dateStr = currentRes.getReservationDate().getDate();
                    datePicker.setValue(LocalDate.parse(dateStr));
                    timeField.setText(currentRes.getReservationDate().getTime());
                } catch (Exception e) {
                    System.out.println("Error parsing date in controller: " + e.getMessage());
                }
            }
            if (currentRes.getNumberOfGuests() != null) {
                dinersField.setText(String.valueOf(currentRes.getNumberOfGuests()));
            }
            if (currentRes.getStatus() != null) {
                statusField.setText(currentRes.getStatus().name());
            }
        }
    }

    // Handles saving the changes made to the reservation.
    @FXML
    private void handleSave(ActionEvent event) {
        // Logic to send updated data to the server (UPDATE)
        System.out.println("Saving changes for Order: " + orderIdField.getText());
        
        // TODO: Create a Reservation object with updated fields and send Action.UPDATE_RESERVATION
        
        // After saving, return to dashboard
        SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    }

    // Deletes the reservation from DB and returns to the dashboard.
    @FXML
    private void handleCancel(ActionEvent event) {
        Reservation resToDelete = BistroClient.reservationInstance;
        
        if (resToDelete != null && resToDelete.getReservationId() != null) {
        	BistroMessage msg = new BistroMessage(Action.CANCEL_RESERVATION, resToDelete);
            ClientUI.chat.accept(msg);            
            BistroClient.reservationInstance = null;
        }
        SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    }
}