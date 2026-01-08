package handlers;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Reservation;
import javafx.application.Platform;
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
        // --- DISABLE READ-ONLY FIELDS ---
        orderIdField.setDisable(true);
        statusField.setDisable(true);
        memberIdField.setDisable(true);
        placedDateField.setDisable(true);
        
        // Load data
        Reservation currentRes = BistroClient.reservationInstance;
        if (currentRes != null) {
            
            // 1. Order ID / Verification Code
            if (currentRes.getVerificationCode() != null) {
                orderIdField.setText(currentRes.getVerificationCode());
            } else if (currentRes.getReservationId() != null) {
                 orderIdField.setText(String.valueOf(currentRes.getReservationId()));
            }
            
            // 2. Date and Time
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
            
            // 3. Guests
            if (currentRes.getNumberOfGuests() != null) {
                dinersField.setText(String.valueOf(currentRes.getNumberOfGuests()));
            }
            
            // 4. Status
            if (currentRes.getStatus() != null) {
                statusField.setText(currentRes.getStatus().name());
            }

            // 5. Member ID
            if (currentRes.getMemberId() != null) {
                memberIdField.setText(String.valueOf(currentRes.getMemberId()));
            } else {
                memberIdField.setText("Guest / N/A");
            }

            // 6. Date of Placing Order
            if (currentRes.getDateOfPlacingReservation() != null) {
                placedDateField.setText(currentRes.getDateOfPlacingReservation());
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            // Collect updated data
            String id = orderIdField.getText();
            String date = (datePicker.getValue() != null) ? datePicker.getValue().toString() : "";
            String time = timeField.getText();
            String guests = dinersField.getText();
            
            // Note: Verification Code does not change on update, so we don't need to show it as "new" here,
            // but we can confirm the update.
            String message = String.format("Changes saved for reservation %s.\nNew Date: %s\nTime: %s\nGuests: %s", 
                                           id, date, time, guests);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reservation Updated");
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            if (BistroClient.staffInstance != null) {
                // Staff logic
                alert.showAndWait();
                SceneLoader.closeWindow(event);
            } else {
                // Client logic
                ButtonType returnBtn = new ButtonType("Return to Main Menu");
                ButtonType exitBtn = new ButtonType("Exit");
                alert.getButtonTypes().setAll(returnBtn, exitBtn);
                
                Optional<ButtonType> res = alert.showAndWait();
                
                if (res.isPresent()) {
                    if (res.get() == returnBtn) {
                        SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
                    } else { 
                        Platform.exit(); 
                        System.exit(0); 
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
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