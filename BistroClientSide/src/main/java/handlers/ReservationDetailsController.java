package handlers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import java.time.LocalDate;


// Controller for the Reservation Details screen.

public class ReservationDetailsController {

    @FXML private TextField orderIdField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField dinersField;
    @FXML private TextField statusField;

    /**
     * Fill the fields with reservation data.
     * This method is called from the previous controller.
     */
    public void setReservationData(String id, String date, String time, String diners, String status) {
        orderIdField.setText(id);
        datePicker.setValue(LocalDate.parse(date));
        timeField.setText(time);
        dinersField.setText(diners);
        statusField.setText(status);
    }

    // Handles saving the changes made to the reservation.
    @FXML
    private void handleSave(ActionEvent event) {
        //TODO: Logic to send updated data to the server 
    	
        System.out.println("Saving changes for Order: " + orderIdField.getText());
        
        // After saving, return to dashboard
        SceneLoader.loadScene(event, "/gui/GuestDashboard.fxml", "Guest Dashboard");
    }


    // Cancels the edit operation and returns to the previous screen.
    @FXML
    private void handleCancel(ActionEvent event) {
        // Return to the time slot selection 
        SceneLoader.loadScene(event, "/gui/TimeSlot.fxml", "Select Time Slot");
    }
}