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

public class TimeSlotController {

    @FXML private Label selectedDateLabel;
    @FXML private TilePane timeSlotsPane;

    private LocalDate reservationDate = LocalDate.now();
    private String email;
    private String phone;
    private String fullName;
    private int numberOfDiners;
    private List<String> availableTimes = new ArrayList<String>();

    public void initData(LocalDate date, String fullName, String email, String phone, int diners) {
        this.reservationDate = date;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.numberOfDiners = diners;
        setSelectedDateText(date.toString());
        calculateTimeSlots();
    }

    @FXML
    public void initialize() { 
    	BistroClient.timeSlotControllerInstance = this;
    }
    public void updateAvailableTimes(List<String> serverValidTimes) {
        	Platform.runLater(() -> {
            this.availableTimes = serverValidTimes;
            displayTimeSlots(); // show the buttons again
        });
    }

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
    
    private void calculateTimeSlots() {
    	Guest guest = new Guest(fullName, phone, email);
    	DateTime resDateTime = new DateTime(reservationDate.toString(), "12:00");
        Reservation newReservation = new Reservation(resDateTime, numberOfDiners, null, guest);
        BistroMessage msg = new BistroMessage(Action.CHECK_RESERVATION_AVAILABILITY, newReservation);
        BistroClient.awaitResponse = true; // waits for response
        ClientUI.chat.accept(msg);
    }
    
    
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
            newReservation = new Reservation(resDateTime, numberOfDiners, null, guest);
        }
        
        BistroMessage msg = new BistroMessage(Action.CREATE_RESERVATION, newReservation);
        ClientUI.chat.accept(msg);
        // disable UI in order to prevent double click
        timeSlotsPane.setDisable(true);
    }
    public void goToReservationDetails() {
        	Platform.runLater(() -> {
            timeSlotsPane.setDisable(false);
            Stage stage = (Stage) timeSlotsPane.getScene().getWindow();
            SceneLoader.loadSceneAgain(stage, "/gui/ReservationDetails.fxml", "Reservation Details");
        });
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/MakeReservation.fxml", "New Reservation");
    }

    public void setSelectedDateText(String date) {
        selectedDateLabel.setText("Available slots for: " + date);
    }
}