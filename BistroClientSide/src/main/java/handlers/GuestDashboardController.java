package handlers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;


public class GuestDashboardController {

    // Handles the action when "Current Visit" button is clicked.
    @FXML
    private void handleCurrentVisit(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/VisitIdentification.fxml", "Current Visit Identification");
    }

    // Handles the action when "Get Table Now" button is clicked.
    @FXML
    private void handleGetTableNow(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/VisitNow.fxml", "Get Table Now");
    }

    // Handles the action when "New Reservation" button is clicked.
    @FXML
    private void handleNewReservation(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/MakeReservation.fxml", "New Reservation");
    }

    // Handles the action when "Find Reservation" button is clicked.
    @FXML
    private void handleFindReservation(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/ReservationFrame.fxml", "Find Reservation");
    }

    // Handles the action when "Back" button is clicked.
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/IsMemberGUI.fxml", "Membership Options");
    }
}