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

    @FXML private Label tableIdLabel;
    @FXML private Label orderIdLabel;
    @FXML private Label startTimeLabel;
    @FXML private Label dinersLabel;
    @FXML private Button startVisitBtn;
    @FXML private Button endVisitBtn;
    @FXML private Label labelForVisitNumber;

    private Timeline countdown;
    private int secondsLeft = 15 * 60; // 15 minutes
    private boolean visitStarted = false;
    public static Visit visitInstance;
    
    @FXML
    public void initialize() {
    	BistroClient.visitDetailsControllerInstance = this;
    	endVisitBtn.setDisable(true);
    	if (VisitSessionManager.hasActiveTimer()) {
    		labelForVisitNumber.setText("Visit Number:");
            startTimeLabel.setText("Started");
            startTimeLabel.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #2e7d32;");
    	    orderIdLabel.setText(String.valueOf(visitInstance.getVisitId()));
    	    dinersLabel.setText(String.valueOf(visitInstance.getPartySize()));
    	    if(visitInstance.getTable().getTableNumber() != null) {
    	    	tableIdLabel.setText(String.valueOf(visitInstance.getTable().getTableNumber()));
    	    }
            startVisitBtn.setDisable(true);
            endVisitBtn.setDisable(false);
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
        if (countdown != null) {
            countdown.stop();
        }
        ClientUI.chat.accept(new BistroMessage(Action.START_VISIT, visitInstance));
    }
    
    public void visitStarted(boolean isStarted) {
    	Platform.runLater(()->{
    		if(isStarted) {
    	        visitStarted = true;
    	        VisitSessionManager.setVisitStarted(true);
    			Platform.runLater(() -> EmailSend.sendConfirmationNotifications("Table Assigned : " + visitInstance.getTable().getTableNumber()));
                startTimeLabel.setText("Started");
                startTimeLabel.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #2e7d32;");
                startVisitBtn.setDisable(true);
                endVisitBtn.setDisable(false);
    		}else {
    			startTimeLabel.setText("Error Starting Visit - speak to manager");
    			startTimeLabel.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-text-fill: red;");
    		}

    	});
    }

    // User clicked "End Meal"
    @FXML
    private void handleEndMeal(ActionEvent event) {
    	visitStarted = false;
        VisitSessionManager.setVisitStarted(false);
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

//	public void loadReservation(Reservation reservation) {
//		if (reservation == null) {
//	        return;
//	    }
//
//	    orderIdLabel.setText(String.valueOf(reservation.getReservationId()));
//	    dinersLabel.setText(String.valueOf(reservation.getNumberOfGuests()));
//        //TODO: tableIdLabel.setText();
//	}

	public static void visitCreated(Integer visitId) {
		Platform.runLater(() -> {
    		if(visitId == null) {
    			String msg = "";
        		SceneLoader.showAlert(Alert.AlertType.ERROR, "Create visit failed",msg);
    		}else {
    		}
    	});
		
	}
	
	public void loadVisit(Visit visit) {
		if(visit == null) return;
		
		orderIdLabel.setText(String.valueOf(visit.getVisitId()));
		dinersLabel.setText(String.valueOf(visit.getPartySize()));
		tableIdLabel.setText(String.valueOf(visit.getTable().getTableNumber()));
		visitInstance = visit;
	}
}
