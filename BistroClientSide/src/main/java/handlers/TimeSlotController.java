package handlers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
import java.io.IOException;

import client.BistroClient;
// --- Imports for Client-Server Communication and Data ---
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.DateTime;
import dataLayer.Guest;
import dataLayer.Member;
import dataLayer.Reservation;

public class TimeSlotController {

    @FXML private Label selectedDateLabel;
    @FXML private FlowPane timeSlotsPane;

    // --- Variables to store data passed from the previous screen ---
    private LocalDate reservationDate;
    private String email;
    private String phone;
    private String fullName;
    private int numberOfDiners;

    /**
     * This method is called by MakeReservationController to pass the user's input.
     */
    public void initData(LocalDate date, String fullName, String email, String phone, int diners) {
        this.reservationDate = date;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.numberOfDiners = diners;
        
        // Update the UI with the selected date
        setSelectedDateText(date.toString());
    }

    @FXML
    public void initialize() {
        // List of available times
        List<String> availableTimes = Arrays.asList(
            "12:00", "12:30", "13:00", "13:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30"
        );

        displayTimeSlots(availableTimes);
    }

    private void displayTimeSlots(List<String> times) {
        timeSlotsPane.getChildren().clear();

        for (String time : times) {
            Button timeButton = new Button(time);
            timeButton.setPrefSize(80, 40);
            timeButton.setStyle("-fx-background-color: white; -fx-border-color: #1976d2; -fx-border-radius: 5; -fx-cursor: hand;");
            timeButton.setOnAction(event -> handleTimeSelection(time, event));
            timeSlotsPane.getChildren().add(timeButton);
        }
    }

    /**
     * Handles the time selection.
     * Creates the Reservation object and sends it to the server.
     */
    private void handleTimeSelection(String selectedTime, ActionEvent event) {
        System.out.println("Time selected: " + selectedTime);
        DateTime resDateTime = new DateTime(reservationDate.toString(), selectedTime);
        Guest guest = new Guest(fullName, phone, email);
        Member member = BistroClient.memberInstance; 
        Reservation newReservation = new Reservation(resDateTime, numberOfDiners, null, guest);
        if(BistroClient.memberInstance != null) {
        	newReservation = new Reservation(resDateTime, numberOfDiners, member.getMemberId(), member);
        }
        else {
        	
        }
        BistroMessage msg = new BistroMessage(Action.CREATE_RESERVATION, newReservation);
        ClientUI.chat.accept(msg);
        System.out.println("Message sent to server: CREATE_RESERVATION");
        SceneLoader.loadScene(event, "/gui/ReservationDetails.fxml", "Reservation Details");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/MakeReservation.fxml", "New Reservation");
    }

    public void setSelectedDateText(String date) {
        selectedDateLabel.setText("Available slots for: " + date);
    }
}