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

/**
 * ReservationFormController manages the reservation edit form.
 * <p>
 * This controller is responsible for:
 * <ul>
 *   <li>Displaying reservation details</li>
 *   <li>Allowing updates to the reservation date and number of guests</li>
 *   <li>Sending update requests to the server</li>
 *   <li>Handling navigation based on user role (member or staff)</li>
 * </ul>
 */
public class ReservationFormController {

	/** Label displaying the reservation number. */
    @FXML
    private Label orderNumberLabel;

    /** Label displaying the reservation confirmation code. */
    @FXML
    private Label confirmationCodeLabel;

    /** Label displaying the member ID associated with the reservation. */
    @FXML
    private Label MemberLabel;

    /** Label displaying the date the reservation was placed. */
    @FXML
    private Label placingOrderDateLabel;

    /** Text field for entering the number of guests. */
    @FXML
    private TextField numberOfGuestsField;

    /** Date picker for selecting the reservation date. */
    @FXML
    private DatePicker orderDatePicker;

    /** Button used to save changes. */
    @FXML
    private Button btnSave_Changes;

    /** Button used to navigate back. */
    @FXML
    private Button btnBack;

    /** Label displaying the reservation status. */
    @FXML
    private Label statusLabel;

    /** The reservation currently being edited. */
    private Reservation reservation; 

    /**
     * Loads a reservation into the form and populates all UI fields.
     *
     * @param reservation the {@link Reservation} to load
     */
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
    
    /**
     * Handles the action when the "Back" button is clicked.
     * <p>
     * Navigation behavior depends on the user role:
     * <ul>
     *   <li>Member: returns to client dashboard</li>
     *   <li>Staff: closes the current window</li>
     *   <li>Other: returns to reservation search screen</li>
     * </ul>
     *
     * @param event the action event triggered by the button click
     * @throws IOException if navigation fails
     */
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
    
    /**
     * Handles saving reservation changes.
     * <p>
     * Validates user input, updates the reservation object,
     * sends an update request to the server, and navigates
     * based on user role.
     *
     * @param event the action event triggered by the button click
     */
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

            // Update reservation data
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