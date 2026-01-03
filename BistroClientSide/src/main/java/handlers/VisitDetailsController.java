package handlers;

import java.time.LocalDate;

import dataLayer.Reservation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class VisitDetailsController {

    @FXML
    private Label tableIdLabel;

    @FXML
    private Label orderIdLabel;

    @FXML
    private Label startTimeLabel;

    @FXML
    private Label dinersLabel;

    @FXML
    private Button startVisitBtn;

    private Timeline countdown;
    private int secondsLeft = 15 * 60; // 15 minutes
    private boolean visitStarted = false;


    @FXML
    public void initialize() {
        startTimeLabel.setText("15:00");
        startCountdown();
    }

    // Starts the 15-minute countdown
    private void startCountdown() {
        countdown = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateTimer())
        );
        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
    }


    // Updates countdown timer every second
    private void updateTimer() {
        if (secondsLeft <= 0) {
            countdown.stop();
            if (!visitStarted) {
                cancelTableDueToNoShow();
            }
            return;
        }

        secondsLeft--;

        int minutes = secondsLeft / 60;
        int seconds = secondsLeft % 60;
        startTimeLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }


    // User clicked "Start Visit"
    @FXML
    private void handleStartVisit() {
        visitStarted = true;

        if (countdown != null) {
            countdown.stop();
        }

        startTimeLabel.setText("Started");
        startTimeLabel.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #2e7d32;");
        startVisitBtn.setDisable(true);

        // TODO:
        // 1. Notify server that visit has started
        // 2. Lock table as active
    }

    // User clicked "End Meal"
    @FXML
    private void handleEndMeal(ActionEvent event) {
        if (countdown != null) {
            countdown.stop();
        }

        // TODO:
        // 1. Notify server to close visit
        // 2. Release table
   	 SceneLoader.loadScene(event, "/gui/PaymentScreen.fxml", "Payment Screen");

    }


    // Auto-cancel if customer didn't start visit within 15 minutes
    private void cancelTableDueToNoShow() {
        System.out.println("Reservation canceled: no show");

        // TODO:
        // 1. Notify server: reservation canceled
        // 2. Release table
        // 3. Show alert to user
        // 4. Navigate back to main screen
    }


	public void loadReservation(Reservation reservation) {
		if (reservation == null) {
	        return;
	    }

	    orderIdLabel.setText(String.valueOf(reservation.getReservationId()));
	    dinersLabel.setText(String.valueOf(reservation.getNumberOfGuests()));

		
	}
}
