package handlers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.application.Platform;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
    @FXML private FlowPane timeSlotsPane;

    private LocalDate reservationDate;
    private String email;
    private String phone;
    private String fullName;
    private int numberOfDiners;

    public void initData(LocalDate date, String fullName, String email, String phone, int diners) {
        this.reservationDate = date;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.numberOfDiners = diners;
        setSelectedDateText(date.toString());
    }

    @FXML
    public void initialize() {
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

    private void handleTimeSelection(String selectedTime, ActionEvent event) {
        // 1. Create Reservation Object
        DateTime resDateTime = new DateTime(reservationDate.toString(), selectedTime);
        Guest guest = new Guest(fullName, phone, email);
        Member member = BistroClient.memberInstance; 
        
        Reservation newReservation;
        if(BistroClient.memberInstance != null) {
            newReservation = new Reservation(resDateTime, numberOfDiners, member.getMemberId(), member);
        } else {
            newReservation = new Reservation(resDateTime, numberOfDiners, null, guest);
        }
        
        // 2. Send to Server
        BistroClient.reservationInstance = null; // Reset to ensure fresh data
        BistroMessage msg = new BistroMessage(Action.CREATE_RESERVATION, newReservation);
        ClientUI.chat.accept(msg);
        
        // 3. Get Verification Code from response
        Reservation createdRes = BistroClient.reservationInstance;
        String verificationCode = (createdRes != null && createdRes.getVerificationCode() != null) 
                                   ? createdRes.getVerificationCode() : "N/A";

        // 4. Prepare Alert Message
        String message = String.format(
            "Reservation successfully created.\n\nDate: %s\nTime: %s\nVerification Code: %s\n\nPlease save this code for your reference.",
            reservationDate, selectedTime, verificationCode
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reservation Confirmed");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // 5. Buttons Logic - FIX: Check if NOT staff (covers both Member and Guest)
        if (BistroClient.staffInstance == null) {
            // --- CLIENT / GUEST LOGIC ---
            ButtonType returnToMenuBtn = new ButtonType("Return to Main Menu");
            ButtonType exitBtn = new ButtonType("Exit");

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
        } else {
            // --- STAFF LOGIC ---
            // Staff opened this as a popup, so we just close the popup window.
            alert.showAndWait();
            SceneLoader.closeWindow(event);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/MakeReservation.fxml", "New Reservation");
    }

    public void setSelectedDateText(String date) {
        selectedDateLabel.setText("Available slots for: " + date);
    }
}