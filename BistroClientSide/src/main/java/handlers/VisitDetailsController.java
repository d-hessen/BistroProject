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

/**
 * Controller responsible for managing and displaying visit details.
 * <p>
 * Handles visit lifecycle including:
 * <ul>
 *   <li>Starting a visit</li>
 *   <li>Tracking visit duration (up to 120 minutes)</li>
 *   <li>Ending a visit manually or automatically</li>
 * </ul>
 * This controller supports both staff and client navigation flows.
 */
public class VisitDetailsController {

	/**
     * Label displaying the table number.
     */
    @FXML
    private Label tableIdLabel;

    /**
     * Label displaying the visit ID.
     */
    @FXML
    private Label orderIdLabel;

    /**
     * Label displaying the visit start time or countdown timer.
     */
    @FXML
    private Label startTimeLabel;

    /**
     * Label displaying number of diners.
     */
    @FXML
    private Label dinersLabel;

    /**
     * Button used to start the visit.
     */
    @FXML
    private Button startVisitBtn;

    /**
     * Button used to end the visit.
     */
    @FXML
    private Button endVisitBtn;

    /**
     * Label displaying visit number or title.
     */
    @FXML
    private Label labelForVisitNumber;

    /**
     * Timeline used for countdown timer updates.
     */
    private Timeline countdown;

    /**
     * Static reference to the current visit instance.
     */
    public static Visit visitInstance;
    
    /**
     * Maximum allowed visit duration in seconds (120 minutes).
     */
    private static final int MAX_VISIT_TIME_SECONDS = 120 * 60;
    
    /**
     * Initializes the controller after FXML loading.
     * Registers the controller instance and restores visit state if needed.
     */
    @FXML
    public void initialize() {
        BistroClient.visitDetailsControllerInstance = this;
        if(visitInstance != null) {
            setupVisitState(visitInstance);
        }
    }
    
    /**
     * Configures the UI according to the visit state.
     *
     * @param visit the visit to display
     */
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
        else{
        	if (VisitSessionManager.isVisitStarted()) {
                VisitSessionManager.clear();
                if (countdown != null) {
                    countdown.stop();
                }
            }
        	if (startTimeLabel != null) startTimeLabel.setText("Not Started");
            if (startVisitBtn != null) startVisitBtn.setDisable(false);
            if (endVisitBtn != null) endVisitBtn.setDisable(true);
        }
   }
    
    /**
     * Resumes the visit timer based on a stored database start time.
     *
     * @param dbStartTimeStr start time retrieved from the database
     */
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
    
    /**
     * Updates the UI to reflect an active visit state.
     */
    private void setupActiveVisitUI() {
        startVisitBtn.setDisable(true);
        endVisitBtn.setDisable(false);
        startTimeLabel.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-text-fill: #2e7d32;");
    }
    
    /**
     * Handles logic when the visit time has expired.
     */
    private void handleTimeExpired() {
        startTimeLabel.setText("00:00:00");
        startTimeLabel.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-text-fill: red;");
        startVisitBtn.setDisable(true);
        endVisitBtn.setDisable(false);
        endVisit();
    }

    /**
     * Starts the countdown timer for the visit duration.
     */
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


    /**
     * Updates the countdown timer every second.
     */
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

    /**
     * Handles user action to start a visit.
     */
    @FXML
    private void handleStartVisit() {
        ClientUI.chat.accept(new BistroMessage(Action.START_VISIT, visitInstance));
    }
    
    /**
     * Callback method invoked after server response to start visit request.
     *
     * @param isStarted true if visit started successfully, false otherwise
     */
    public void visitStarted(boolean isStarted) {
    	Platform.runLater(()->{
    		if(isStarted) {
    			VisitSessionManager.setVisitStarted(true);
    			VisitSessionManager.startVisitSession(MAX_VISIT_TIME_SECONDS);
    			setupActiveVisitUI();
                startCountdown();
    		}else {
    			startTimeLabel.setText("Error Starting Visit - speak to manager");
    			startTimeLabel.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-text-fill: red;");
    		}

    	});
    }

    /**
     * Handles user action to end the visit and proceed to payment.
     *
     * @param event the action event triggered by the button
     */
    @FXML
    private void handleEndMeal(ActionEvent event) {
    	if (countdown != null) {
            countdown.stop();
        }
    	VisitSessionManager.clear();
        SceneLoader.loadScene(event, "/gui/PaymentScreen.fxml", "Payment Screen");
    }

    
    /**
     * Automatically ends the visit when maximum time is reached.
     */
    private void endVisit() {
        System.out.println("Reservation canceled: no show");

    }
	
    /**
     * Loads visit data into the controller and updates the UI.
     *
     * @param visit the visit to load
     */
    public void loadVisit(Visit visit) {
        if(visit == null) return;
        visitInstance = visit;
        
        // If the UI is already loaded (controller initialized), update the state immediately.
        if (startVisitBtn != null) {
            setupVisitState(visit);
        }
    }
    
    /**
     * Handles navigation back based on user role.
     *
     * @param event the action event triggered by the Back button
     */
    @FXML void handleBack(ActionEvent event) {
    	if(BistroClient.staffInstance != null) {
    		SceneLoader.closeWindow(event);
    	}else {
    		SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    	}
    }
}
