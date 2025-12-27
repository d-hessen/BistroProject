package handlers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import java.util.Arrays;
import java.util.List;


// Controller for the Time Slot selection screen.

public class TimeSlotController {

    @FXML private Label selectedDateLabel;
    @FXML private FlowPane timeSlotsPane;


    //Initializes the controller. 
    @FXML
    public void initialize() {
    	
        //TODO: Get available times from server
    	
        List<String> availableTimes = Arrays.asList(
            "12:00", "12:30", "13:00", "13:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30"
        );

        displayTimeSlots(availableTimes);
    }

    // Dynamically creates buttons for each available time slot and adds them to the FlowPane.
    private void displayTimeSlots(List<String> times) {
        // Clear any existing children in the pane
        timeSlotsPane.getChildren().clear();

        for (String time : times) {
            Button timeButton = new Button(time);
            
            // Apply styling to make it look like a "box/cube"
            timeButton.setPrefSize(80, 40);
            timeButton.setStyle("-fx-background-color: white; -fx-border-color: #1976d2; -fx-border-radius: 5; -fx-cursor: hand;");
            
            // Set the action when a specific time is clicked
            timeButton.setOnAction(event -> handleTimeSelection(time, event));
            
            // Add the button to the dynamic FlowPane
            timeSlotsPane.getChildren().add(timeButton);
        }
    }


    // Handles the event when a user selects a specific time slot.
    private void handleTimeSelection(String selectedTime, ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/ReservationDetails.fxml", "Reservation Details");
        System.out.println("Time selected: " + selectedTime);
        
        // TODO: Save the selected time and proceed to the ReservationDetails screen.

    }

    // Navigates back to the New Reservation screen.
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/MakeReservation.fxml", "New Reservation");
    }
    

    // Sets the text for the date label
    public void setSelectedDateText(String date) {
        selectedDateLabel.setText("Available slots for: " + date);
    }
}