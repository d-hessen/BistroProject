package handlers;

import java.net.URL;
import java.util.ResourceBundle;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Member;
import dataLayer.Reservation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * ClientDashboardController controls the main dashboard screen
 * presented to clients after login.
 * <p>
 * The controller is responsible for:
 * <ul>
 *   <li>Navigating between client-related screens</li>
 *   <li>Displaying different UI sections for members and guests</li>
 *   <li>Handling logout and session termination</li>
 * </ul>
 *
 * <p>
 * This controller implements {@link Initializable} to configure
 * the UI state after the FXML has been loaded.
 */
public class ClientDashboardController implements Initializable {
	
    /**
     * Label displaying the current login status (member name or guest).
     */
	@FXML
	Label welcomeLabel;
	
    /**
     * UI section visible only to logged-in members.
     */
	
	@FXML
	VBox memberSection;
	
    /**
     * UI section visible only to guest users.
     */
	@FXML
	VBox guestSection;
	
	/**
     * Handles the action when the "Current Visit" button is clicked.
     * <p>
     * Navigates the user to the visit identification screen.
     *
     * @param event the action event triggered by the button click
     */
	@FXML
    private void handleCurrentVisit(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/VisitIdentification.fxml", "Current Visit Identification");
    }

	/**
     * Handles the action when the "Get Table Now" button is clicked.
     * <p>
     * Navigates the user to the walk-in visit screen.
     *
     * @param event the action event triggered by the button click
     */
	@FXML
    private void handleGetTableNow(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/VisitNow.fxml", "Get Table Now");
    }

	/**
     * Handles the action when the "New Reservation" button is clicked.
     * <p>
     * Navigates the user to the reservation creation screen.
     *
     * @param event the action event triggered by the button click
     */
	@FXML
    private void handleNewReservation(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/MakeReservation.fxml", "New Reservation");
    }

	 /**
     * Handles the action when the "Find Reservation" button is clicked.
     * <p>
     * Navigates the user to the reservation search screen.
     *
     * @param event the action event triggered by the button click
     */
	@FXML
    private void handleFindReservation(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/ReservationFrame.fxml", "Find Reservation");
    }

	/**
     * Handles the action when the "Logout" (Back) button is clicked.
     * <p>
     * Logs out the current user, navigates to the appropriate
     * login screen, and sends a disconnect message to the server.
     *
     * @param event the action event triggered by the button click
     */
	@FXML
    private void handleLogout(ActionEvent event) {
    	if(BistroClient.memberInstance != null) {
        	BistroClient.memberInstance = null; 	
        	SceneLoader.loadScene(event, "/gui/MemberLoginGUI.fxml", "Member Login");
    	}
    	else {
    		SceneLoader.loadScene(event, "/gui/IsMemberGUI.fxml", "Client Login Options");
    	}
    	ClientUI.chat.accept(new BistroMessage(Action.DISCONNECT, null));
    }
    
    /**
     * Handles the action when the "My Reservations" button is clicked.
     * <p>
     * Navigates the user to the member reservations screen.
     *
     * @param event the action event triggered by the button click
     */
	@FXML
    private void handleMyReservations(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/MemberReservations.fxml", "My Reservations");
    }
    
	/**
     * Handles the action when the "My Visits" button is clicked.
     * <p>
     * Navigates the user to the visit history screen.
     *
     * @param event the action event triggered by the button click
     */
	@FXML
    private void handleMyVisits(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/ViewVisits.fxml", "My Visits");
    }
    
	/**
     * Handles the action when the "View Profile" button is clicked.
     * <p>
     * Navigates the user to the member profile screen.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void handleViewProfile(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/MemberProfile.fxml", "My Profile");
    }

    /**
     * Initializes the dashboard after the FXML has been loaded.
     * <p>
     * Displays the appropriate UI sections depending on whether
     * the user is logged in as a member or as a guest.
     *
     * @param arg0 the location used to resolve relative paths
     * @param arg1 the resources used to localize the root object
     */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		if(BistroClient.memberInstance != null) {
			welcomeLabel.setText("Logged in as: " +BistroClient.memberInstance.getFullName());
			memberSection.setVisible(true);
			guestSection.setVisible(false);
			guestSection.setManaged(false);
		}else {
			welcomeLabel.setText("Logged in as: Guest");
			guestSection.setVisible(true);
			memberSection.setVisible(false);
			memberSection.setManaged(false);
		}
		
	}
    
    
}
