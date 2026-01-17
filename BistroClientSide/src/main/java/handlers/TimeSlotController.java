package handlers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.layout.TilePane;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.time.Instant;
import java.time.LocalDate;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.DateTime;
import dataLayer.Guest;
import dataLayer.Member;
import dataLayer.Reservation;

/**
 * Controller responsible for displaying and managing available time slots
 * for creating a new reservation.
 * <p>
 * This controller:
 * <ul>
 *   <li>Receives reservation details from the previous screen</li>
 *   <li>Requests available time slots from the server</li>
 *   <li>Displays selectable time slots to the user</li>
 *   <li>Handles reservation creation upon time selection</li>
 * </ul>
 */
public class TimeSlotController {

	/**
     * Label displaying the selected reservation date.
     */
    @FXML
    private Label selectedDateLabel;

    /**
     * Container holding the dynamically generated time slot buttons.
     */
    @FXML
    private TilePane timeSlotsPane;

    /**
     * Selected reservation date.
     */
    private LocalDate reservationDate = LocalDate.now();
    /**
     * Guest email address.
     */
    private String email;

    /**
     * Guest phone number.
     */
    private String phone;

    /**
     * Guest full name.
     */
    private String fullName;

    /**
     * Number of diners for the reservation.
     */
    private int numberOfDiners;

    /**
     * List of available time slots returned from the server.
     */
    private List<String> availableTimes = new ArrayList<String>();

    public static Reservation newReservation = new Reservation(null,null,null,null);
    /**
     * Initializes reservation data passed from the previous screen.
     *
     * @param date        reservation date
     * @param fullName    guest full name
     * @param email       guest email address
     * @param phone       guest phone number
     * @param diners      number of diners
     */
    public void initData(LocalDate date, String fullName, String email, String phone, int diners) {
        this.reservationDate = date;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.numberOfDiners = diners;
        setSelectedDateText(date.toString());
        calculateTimeSlots();
    }

    /**
     * Initializes the controller after the FXML is loaded.
     * <p>
     * Registers this controller instance for server callbacks.
     */
    @FXML
    public void initialize() { 
    	BistroClient.timeSlotControllerInstance = this;
    }
    
    /**
     * Updates the list of available time slots received from the server.
     *
     * @param serverValidTimes list of available reservation times
     */
    public void updateAvailableTimes(List<String> serverValidTimes) {
        	Platform.runLater(() -> {
            this.availableTimes = serverValidTimes;
            displayTimeSlots(); // show the buttons again
        });
    }

    /**
     * Displays the available time slots as selectable buttons.
     */
    private void displayTimeSlots() {
        timeSlotsPane.getChildren().clear();
        if (availableTimes == null || availableTimes.isEmpty()) {
            Label noSlots = new Label("No slots available.");
            timeSlotsPane.getChildren().add(noSlots);
            return;
        }

        for (String time : availableTimes) {
            Button timeButton = new Button(time);
            
            // force the button to never change size
            timeButton.setMinWidth(80);
            timeButton.setMaxWidth(80);
            timeButton.setPrefWidth(80);
            
            timeButton.setMinHeight(40);
            timeButton.setMaxHeight(40);
            timeButton.setPrefHeight(40);

            String defaultStyle = "-fx-background-color: white; " +
                                  "-fx-border-color: #1976d2; " +
                                  "-fx-border-width: 1px; " +
                                  "-fx-border-radius: 5; " +
                                  "-fx-background-radius: 5; " +
                                  "-fx-cursor: hand;";

            String hoverStyle = "-fx-background-color: #e3f2fd; " + 
                                "-fx-border-color: #1976d2; " +
                                "-fx-border-width: 1px; " +
                                "-fx-border-radius: 5; " +
                                "-fx-background-radius: 5; " +
                                "-fx-cursor: hand;";

            timeButton.setStyle(defaultStyle);
            timeButton.setOnMouseEntered(e -> timeButton.setStyle(hoverStyle));
            timeButton.setOnMouseExited(e -> timeButton.setStyle(defaultStyle));
            
            timeButton.setOnAction(event -> handleTimeSelection(time, event));
            timeSlotsPane.getChildren().add(timeButton);
        }
    }
    
    /**
     * Requests available reservation time slots from the server.
     */
    private void calculateTimeSlots() {
    	Guest guest = new Guest(fullName, phone, email);
    	DateTime resDateTime = new DateTime(reservationDate.toString(), "12:00");
        Reservation newReservation = new Reservation(resDateTime, numberOfDiners, null, guest);
        BistroMessage msg = new BistroMessage(Action.CHECK_RESERVATION_AVAILABILITY, newReservation);
        BistroClient.awaitResponse = true; // waits for response
        ClientUI.chat.accept(msg);
    }
    
    /**
     * Handles selection of a specific time slot and creates a reservation.
     *
     * @param selectedTime the selected reservation time
     * @param event        the action event triggered by the time button
     */
    private void handleTimeSelection(String selectedTime, ActionEvent event) {
        System.out.println("Time selected: " + selectedTime);
        DateTime resDateTime = new DateTime(reservationDate.toString(), selectedTime);
        Guest guest = new Guest(fullName, phone, email);
        Member member = BistroClient.memberInstance; 
        newReservation = new Reservation(resDateTime, numberOfDiners, null, guest);
        
        if(BistroClient.memberInstance != null) {
            newReservation = new Reservation(resDateTime, numberOfDiners, member.getMemberId(), member);
        } 
        else {
            newReservation = new Reservation(resDateTime, numberOfDiners, null, guest);
        }
        
        // disable UI in order to prevent double click
        timeSlotsPane.setDisable(true);
        BistroClient.reservationInstance = TimeSlotController.newReservation;
        goToReservationDetails();

    }
    
    /**
     * Navigates to the reservation details screen after successful creation.
     */
    public void goToReservationDetails() {
        	Platform.runLater(() -> {
            timeSlotsPane.setDisable(false);
            Stage stage = (Stage) timeSlotsPane.getScene().getWindow();
            SceneLoader.loadSceneAgain(stage, "/gui/ReservationDetails.fxml", "Reservation Details");
        });
    }

    /**
     * Handles navigation back to the reservation form screen.
     *
     * @param event the action event triggered by the back button
     */
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/MakeReservation.fxml", "New Reservation");
    }

    /**
     * Updates the date label text shown to the user.
     *
     * @param date formatted date string
     */
    public void setSelectedDateText(String date) {
        selectedDateLabel.setText("Available slots for: " + date);
    }
}