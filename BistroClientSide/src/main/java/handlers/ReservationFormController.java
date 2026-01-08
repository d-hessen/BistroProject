package handlers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.DateTime;
import dataLayer.Reservation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ReservationFormController {

    @FXML
    private Label orderNumberLabel;
    @FXML
    private Label confirmationCodeLabel;
    @FXML
    private Label MemberLabel;
    @FXML
    private Label placingOrderDateLabel;
    @FXML
    private TextField numberOfGuestsField;
    @FXML
    private DatePicker orderDatePicker;
    @FXML
    private Button btnSave_Changes;
    @FXML
    private Button btnBack;
    @FXML
    private Label statusLabel;
    
    private Reservation reservation; 

    public void loadReservation(Reservation reservation) {
        this.reservation = reservation;
        if(reservation == null) {
            return;
        }
        // Set text fields
        orderNumberLabel.setText(reservation.getReservationId() != null ? String.valueOf(reservation.getReservationId()) : "");
        statusLabel.setText(reservation.getStatus() != null ? reservation.getStatus().name() : "");
        confirmationCodeLabel.setText(reservation.getVerificationCode() != null ? reservation.getVerificationCode() : "");
        MemberLabel.setText(reservation.getMemberId() != null ? String.valueOf(reservation.getMemberId()) : "");
        placingOrderDateLabel.setText(reservation.getDateOfPlacingReservation() != null ? reservation.getDateOfPlacingReservation() : "");
        numberOfGuestsField.setText(reservation.getNumberOfGuests() != null ? String.valueOf(reservation.getNumberOfGuests()) : "");
        
        // Converts the String date to LocalDate
        try {
            if (reservation.getReservationDate() != null && reservation.getReservationDate().getDate() != null && !reservation.getReservationDate().getDate().isEmpty()) {
                orderDatePicker.setValue(LocalDate.parse(reservation.getReservationDate().getDate()));
            }
        } catch (Exception e) {
            orderDatePicker.setValue(null); // if string not in yyyy-MM-dd
        }
    }
    
    // Logic for the Back button
    public void buttonBack(ActionEvent event) throws IOException {
        // If logged in as member, go to dashboard, otherwise: back or disconnect 
        if (BistroClient.memberInstance != null) {
             SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
        } else if (BistroClient.staffInstance != null) {
             // If staff opened this window
             SceneLoader.closeWindow(event);
        } else {
             SceneLoader.loadScene(event, "/gui/ReservationFrame.fxml", "Reservation Finder");
        }
    }
    
    // Main Save Logic with the Alert and Navigation
    public void save(ActionEvent event) {
        try {
            String guestsInput = numberOfGuestsField.getText().trim();            
            if (guestsInput.isEmpty()) {
                SceneLoader.showAlert(Alert.AlertType.ERROR, "Input Error", "Number of guests cannot be empty.");
                return;
            }
            
            int guests;
            try {
                guests = Integer.parseInt(guestsInput);
                if (guests <= 0) {
                     SceneLoader.showAlert(Alert.AlertType.ERROR, "Input Error", "Number of guests must be greater than 0.");
                     return;
                }
            } catch (NumberFormatException e) {
                SceneLoader.showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid number format for guests.");
                return;
            }

            if (orderDatePicker.getValue() == null) {
                SceneLoader.showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a date.");
                return;
            }

            // Update Reservation Object and Send to Server
            reservation.setNumberOfGuests(guests);
            reservation.setReservationDate(new DateTime(orderDatePicker.getValue().toString(), "12:00")); // Check if you need to preserve original time
            
            ClientUI.chat.accept(new BistroMessage(Action.UPDATE_RESERVATION, reservation));

            // Success Message
            String message = String.format(
                "Reservation number %s\nDate: %s\nGuests: %d\n\nDetails saved successfully.", 
                reservation.getVerificationCode(),
                orderDatePicker.getValue().toString(), 
                guests
            );

            // Create the Alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reservation Saved");
            alert.setHeaderText(null);
            alert.setContentText(message);

            // Handle Roles (Manager\Client)
            if (BistroClient.staffInstance != null) {
                // STAFF / MANAGER LOGIC 
                // Show alert, wait for OK, then close window
                alert.showAndWait();
                SceneLoader.closeWindow(event);
                
            } else {
                // CLIENT LOGIC 
                // Custom Buttons
                ButtonType returnToMenuBtn = new ButtonType("Return to Main Menu");
                ButtonType exitBtn = new ButtonType("Exit System");

                alert.getButtonTypes().setAll(returnToMenuBtn, exitBtn);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent()) {
                    if (result.get() == returnToMenuBtn) {
                        SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
                    } else if (result.get() == exitBtn) {
                        Platform.exit();
                        System.exit(0);
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            SceneLoader.showAlert(Alert.AlertType.ERROR, "System Error", "An error occurred while saving.");
        }
    }
}