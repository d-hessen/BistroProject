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
import java.time.LocalDate;
import java.util.Optional;

public class ReservationDetailsController {

    @FXML private TextField orderIdField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField dinersField;
    @FXML private TextField statusField;
    @FXML private TextField memberIdField;
    @FXML private TextField placedDateField;

    @FXML
    public void initialize() {
        orderIdField.setDisable(true);
        statusField.setDisable(true);
        memberIdField.setDisable(true);
        placedDateField.setDisable(true);
        
        Reservation currentRes = BistroClient.reservationInstance;
        if (currentRes != null) {
            if (currentRes.getVerificationCode() != null) {
                orderIdField.setText(currentRes.getVerificationCode());
            } else if (currentRes.getReservationId() != null) {
                 orderIdField.setText(String.valueOf(currentRes.getReservationId()));
            }

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
            
            if (currentRes.getNumberOfGuests() != null) {
                dinersField.setText(String.valueOf(currentRes.getNumberOfGuests()));
            }
            
            if (currentRes.getStatus() != null) {
                statusField.setText(currentRes.getStatus().name());
            }

            if (currentRes.getMemberId() != 0) {
                memberIdField.setText(String.valueOf(currentRes.getMemberId()));
            } else {
                memberIdField.setText("Guest");
            }

            if (currentRes.getDateOfPlacingReservation() != null) {
                placedDateField.setText(currentRes.getDateOfPlacingReservation());
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            // Input Validation
            if (datePicker.getValue() == null || timeField.getText().isEmpty() || dinersField.getText().isEmpty()) {
                SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields");
                return;
            }

            // Update Local Object with UI Data
            Reservation resToUpdate = BistroClient.reservationInstance;
            
            // Update Date and Time
            if (resToUpdate.getReservationDate() != null) {
                resToUpdate.getReservationDate().setDate(datePicker.getValue().toString());
                resToUpdate.getReservationDate().setTime(timeField.getText());
            }
            
            // Update Guest Count
            try {
                int guests = Integer.parseInt(dinersField.getText());
                resToUpdate.setNumberOfGuests(guests);
            } catch (NumberFormatException e) {
                SceneLoader.showAlert(Alert.AlertType.ERROR, "Input Error", "Guests must be a valid number");
                return;
            }

            // Send Update Request to Server 
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
                // Client side logic
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
                        alert.close();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while saving.");
        }
    }

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