package handlers;


import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Reservation;
import dataLayer.Table;
import dataLayer.Visit;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;
import handlers.VisitSessionManager;

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
    
    @FXML
    private Button endtVisitBtn;

    private Timeline countdown;
    private int secondsLeft = 15 * 60; // 15 minutes
    private boolean visitStarted = false;
    private Visit globalVisit = null;
    
    @FXML
    public void initialize() {
    	if (VisitSessionManager.hasActiveTimer()) {
            startTimeLabel.setText("Started");
            startTimeLabel.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #2e7d32;");
    	    orderIdLabel.setText(String.valueOf(BistroClient.reservationInstance.getReservationId()));
    	    dinersLabel.setText(String.valueOf(BistroClient.reservationInstance.getNumberOfGuests()));
            startVisitBtn.setDisable(true);
            //TODO: tableIdLabel.setText();
        } else {
            secondsLeft = 15 * 60;
            VisitSessionManager.setSecondsLeft(secondsLeft);
            startCountdown();
        }
    }

    // Starts the 15-minute countdown
    private void startCountdown() {
        countdown = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateTimer())
        );
        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
        
        VisitSessionManager.startTimer(secondsLeft, countdown);
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
        VisitSessionManager.setSecondsLeft(secondsLeft);

        int minutes = secondsLeft / 60;
        int seconds = secondsLeft % 60;
        startTimeLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }


    // User clicked "Start Visit"
    @FXML
    private void handleStartVisit() {
        visitStarted = true;
        VisitSessionManager.setVisitStarted(true);

        if (countdown != null) {
            countdown.stop();
        }

        startTimeLabel.setText("Started");
        startTimeLabel.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #2e7d32;");
        startVisitBtn.setDisable(true);

        // TODO:
        // 1. Notify server that visit has started - Done
        // 2. Lock table as active
        // 3. Change random logic
        int randomTableNum = (int)(Math.random() * (5 - 1 + 1) + 1);
        Table randomTable = new Table(randomTableNum, 3, true);
        Visit visit = new Visit(BistroClient.reservationInstance,randomTable,true);
        globalVisit = visit;
        BistroMessage msg = new BistroMessage(Action.CREATE_VISIT, visit);
        ClientUI.chat.accept(msg);
        System.out.println("Message sent to server: CREATE_VISIT");
    }

    // User clicked "End Meal"
    @FXML
    private void handleEndMeal(ActionEvent event) {
        visitStarted = false;
        VisitSessionManager.setVisitStarted(false);

        // TODO:
        // 1. Notify server to close visit
        // 2. Release table
     BistroClient.currentVisit = globalVisit;
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
        //TODO: tableIdLabel.setText();
	}

	public static void visitCreated(Integer visitId) {
		Platform.runLater(() -> {
    		if(visitId == null) {
    			String msg = "";
        		SceneLoader.showAlert(Alert.AlertType.ERROR, "Create visit failed",msg);
    		}else {
    		}
    	});
		
	}
}
