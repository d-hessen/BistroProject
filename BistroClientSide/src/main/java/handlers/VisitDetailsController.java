package handlers;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Visit;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class VisitDetailsController {

    @FXML private Label tableIdLabel;
    @FXML private Label orderIdLabel;
    @FXML private Label startTimeLabel;
    @FXML private Label dinersLabel;
    @FXML private Button startVisitBtn;
    @FXML private Button endVisitBtn;
    @FXML private Label labelForVisitNumber;

    private Timeline countdown;
    public static Visit visitInstance;
    private static final int MAX_VISIT_TIME_SECONDS = 120 * 60;
    
    @FXML
    public void initialize() {
        BistroClient.visitDetailsControllerInstance = this;
        if(visitInstance != null) {
            setupVisitState(visitInstance);
        }
    }
    
    private void setupVisitState(Visit visit) {
        if (orderIdLabel != null) {
            orderIdLabel.setText(String.valueOf(visit.getVisitId()));
            dinersLabel.setText(String.valueOf(visit.getPartySize()));
            if(visit.getTable() != null && visit.getTable().getTableNumber() != null) {
                tableIdLabel.setText(String.valueOf(visit.getTable().getTableNumber()));
            }
        }
        if (visit.getStartTime() != null && !visit.getStartTime().toString().isEmpty()) {
            //It is already started
            startVisitBtn.setDisable(true);
            endVisitBtn.setDisable(false);
            resumeTimerFromDB(visit.getStartTime().getTime().toString());
        } 
        else if (VisitSessionManager.isVisitStarted() && VisitSessionManager.hasActiveTimer()) {
            setupActiveVisitUI();
            startCountdown();
        } 
        else {
            if (startTimeLabel != null) startTimeLabel.setText("Not Started");
            if (startVisitBtn != null) startVisitBtn.setDisable(false);
            if (endVisitBtn != null) endVisitBtn.setDisable(true);
        }
    }
    
    private void resumeTimerFromDB(String dbStartTimeStr) {
        try {
            LocalTime dbTime = LocalTime.parse(dbStartTimeStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
            //Assume the visit is today. 
            LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), dbTime);
            LocalDateTime now = LocalDateTime.now();

            //Handle edge case where 'start' appears to be in the future
            if (startDateTime.isAfter(now)) {
                startDateTime = startDateTime.minusDays(1);
            }
            //Calculate seconds passed since start
            long secondsPassed = ChronoUnit.SECONDS.between(startDateTime, now);
            long secondsRemaining = MAX_VISIT_TIME_SECONDS - secondsPassed;

            if (secondsRemaining > 0) {
                //Determine new session duration based on DB history
                VisitSessionManager.startVisitSession((int) secondsRemaining);
                VisitSessionManager.setVisitStarted(true);
                
                setupActiveVisitUI();
                startCountdown();
            } else {
                //Time already expired
                handleTimeExpired();
            }

        } catch (Exception e) {
            System.err.println("Error parsing start time: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupActiveVisitUI() {
        startVisitBtn.setDisable(true);
        endVisitBtn.setDisable(false);
        startTimeLabel.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #2e7d32;");
    }
    
    private void handleTimeExpired() {
        startTimeLabel.setText("00:00:00");
        startTimeLabel.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-text-fill: red;");
        startVisitBtn.setDisable(true);
        endVisitBtn.setDisable(false);
        endVisit();
    }

    //Starts the 120-minute countdown
    private void startCountdown() {
    	if (countdown != null) {
            countdown.stop();
        }
        countdown = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateTimer())
        );
        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
        
        updateTimer();
    }


    //Updates countdown timer every second
    private void updateTimer() {
    	int currentSecondsLeft = VisitSessionManager.getSecondsLeft();
    	
    	if (currentSecondsLeft <= 0) {
            countdown.stop();
            startTimeLabel.setText("00:00");
            endVisit();
            return;
        }
    	
    	int minutes = currentSecondsLeft / 60;
        int seconds = currentSecondsLeft % 60;
        
        if (minutes >= 60) {
            int hours = minutes / 60;
            int mins = minutes % 60;
            startTimeLabel.setText(String.format("%02d:%02d:%02d", hours, mins, seconds));
        } else {
            startTimeLabel.setText(String.format("%02d:%02d", minutes, seconds));
        }
       
       startTimeLabel.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #2e7d32;");
   }

    // User clicked "Start Visit"
    @FXML
    private void handleStartVisit() {
        ClientUI.chat.accept(new BistroMessage(Action.START_VISIT, visitInstance));
    }
    
    public void visitStarted(boolean isStarted) {
    	Platform.runLater(()->{
    		if(isStarted) {
    			VisitSessionManager.setVisitStarted(true);
    			VisitSessionManager.startVisitSession(MAX_VISIT_TIME_SECONDS);
    			setupActiveVisitUI();
    			Platform.runLater(() -> EmailSend.sendConfirmationNotifications("Table Assigned : " + visitInstance.getTable().getTableNumber().toString()));
                startCountdown();
    		}else {
    			startTimeLabel.setText("Error Starting Visit - speak to manager");
    			startTimeLabel.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-text-fill: red;");
    		}

    	});
    }

    // User clicked "End Meal"
    @FXML
    private void handleEndMeal(ActionEvent event) {
    	if (countdown != null) {
            countdown.stop();
        }
    	VisitSessionManager.clear();
        SceneLoader.loadScene(event, "/gui/PaymentScreen.fxml", "Payment Screen");
    }

    
    //Auto-end if customer didn't end visit after 2 hours
    private void endVisit() {
        System.out.println("Reservation canceled: no show");

    }
	
    public void loadVisit(Visit visit) {
        if(visit == null) return;
        visitInstance = visit;
        
        // If the UI is already loaded (controller initialized), update the state immediately.
        if (startVisitBtn != null) {
            setupVisitState(visit);
        }
    }
    
    @FXML void handleBack(ActionEvent event) {
    	if(BistroClient.staffInstance != null) {
    		SceneLoader.closeWindow(event);
    	}else {
    		SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    	}
    }
}
