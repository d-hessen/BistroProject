package handlers;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Reservation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Optional;

/**
 * ReservationDetailsController manages the reservation details screen.
 * <p>
 * This controller allows viewing and editing reservation information,
 * including date, time, number of diners, and status.
 * It supports both staff and client workflows.
 */
public class ReservationDetailsController {

	/** Field displaying the reservation or verification ID (read-only). */
    @FXML private TextField orderIdField;

    /** Date picker for selecting the reservation date. */
    @FXML private DatePicker datePicker;

    /** Field for entering the reservation time. */
    @FXML private TextField timeField;

    /** Field for entering the number of diners. */
    @FXML private TextField dinersField;

    /** Field displaying the reservation status (read-only). */
    @FXML private TextField statusField;

    /** Field displaying the member ID or guest indicator (read-only). */
    @FXML private TextField memberIdField;

    /** Field displaying the reservation placement date (read-only). */
    @FXML private TextField placedDateField;

    /**
     * Initializes the reservation details screen after the FXML is loaded.
     * <p>
     * Populates all fields using the currently selected reservation
     * stored in {@link BistroClient#reservationInstance}.
     */
    @FXML
    public void initialize() {
        orderIdField.setDisable(true);
        statusField.setDisable(true);
        memberIdField.setDisable(true);
        placedDateField.setDisable(true);
        
        Reservation currentRes = BistroClient.reservationInstance;
        if (currentRes != null) {
            // Set reservation or verification identifier
            if (currentRes.getVerificationCode() != null) {
                orderIdField.setText(currentRes.getVerificationCode());
            } else if (currentRes.getReservationId() != null) {
                 orderIdField.setText(String.valueOf(currentRes.getReservationId()));
            }
            
            // Set reservation date and time
            if (currentRes.getReservationDate() != null) {
                try {
                    String dateStr = currentRes.getReservationDate().getDate();
                    if (dateStr != null && !dateStr.isEmpty()) {
                        datePicker.setValue(LocalDate.parse(dateStr));
                    }
                    timeField.setText(currentRes.getReservationDate().getTime());
                } catch (Exception e) {
                    System.out.println("Error parsing date: " + e.getMessage());
                }
            }
            
            // Set number of guests
            if (currentRes.getNumberOfGuests() != null) {
                dinersField.setText(String.valueOf(currentRes.getNumberOfGuests()));
            }
            
            // Set reservation status
            if (currentRes.getStatus() != null) {
                statusField.setText(currentRes.getStatus().name());
            }
            
            // Set member or guest information
            if (currentRes.getMemberId() != null) {
                memberIdField.setText(String.valueOf(currentRes.getMemberId()));
            } else {
                memberIdField.setText("Guest");
            }
            
            // Set date of reservation placement
            if (currentRes.getDateOfPlacingReservation() != null) {
                placedDateField.setText(currentRes.getDateOfPlacingReservation());
            }
        }
    }

    /**
     * Handles the action when the "Save" button is clicked.
     * <p>
     * Validates user input, updates the reservation object,
     * and sends an update request to the server if the user is staff.
     * Client users save changes locally only.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void handleSave(ActionEvent event) {
        try {
            // Input Validation
            if (datePicker.getValue() == null || timeField.getText().isEmpty() || dinersField.getText().isEmpty()) {
                SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields");
                return;
            }

            // Update reservation object
            Reservation resToUpdate = BistroClient.reservationInstance;
            
            // Update Date and Time
            if (resToUpdate.getReservationDate() != null) {
                resToUpdate.getReservationDate().setDate(datePicker.getValue().toString());
                resToUpdate.getReservationDate().setTime(timeField.getText());
            }
            
            // Update number of guests
            try {
                int guests = Integer.parseInt(dinersField.getText());
                resToUpdate.setNumberOfGuests(guests);
            } catch (NumberFormatException e) {
                SceneLoader.showAlert(Alert.AlertType.ERROR, "Input Error", "Guests must be a valid number");
                return;
            }

            // Staff workflow: send update to server
            if (BistroClient.staffInstance != null) {
                // If the user is a staff member, send the update request to the server
                ClientUI.chat.accept(new BistroMessage(Action.UPDATE_RESERVATION, resToUpdate));
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Reservation Updated");
                alert.setHeaderText(null);
                alert.setContentText("Update request sent to server successfully.");
                alert.showAndWait();               
                SceneLoader.closeWindow(event);
                
            } else {
                // Client workflow: local save only
                ButtonType returnBtn = new ButtonType("Return to Main Menu");
                ButtonType exitBtn = new ButtonType("Close");
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Changes Saved");
                alert.setContentText("Changes saved locally.");
                alert.getButtonTypes().setAll(returnBtn, exitBtn);
                
                Optional<ButtonType> res = alert.showAndWait();
                
                if (res.isPresent()) {
                    if (res.get() == returnBtn) {
                        SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");

                    } else { 
                    	SceneLoader.closeWindow(event);
                    }
                }
            }
            if(BistroClient.updateReservation == true) {
                BistroMessage msg = new BistroMessage(Action.UPDATE_RESERVATION, BistroClient.reservationInstance);
                ClientUI.chat.accept(msg);   
            }
            else {
                BistroMessage msg = new BistroMessage(Action.CREATE_RESERVATION, BistroClient.reservationInstance);
                ClientUI.chat.accept(msg);   
            }
        } catch (Exception e) {
            e.printStackTrace();
            SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while saving.");
        }
    }

    /**
     * Handles the action when the "Cancel" button is clicked.
     * <p>
     * Cancels the reservation and sends a delete request to the server.
     * Navigation behavior depends on whether the user is a member or staff.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        Reservation resToDelete = BistroClient.reservationInstance;
        if (resToDelete != null) {
            ClientUI.chat.accept(new BistroMessage(Action.CANCEL_RESERVATION, resToDelete));            
            BistroClient.reservationInstance = null;
        }
        
        if (BistroClient.memberInstance != null) {
            SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
        } else {
             SceneLoader.closeWindow(event);
        }
    }
}