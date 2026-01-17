package handlers;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

/**
 * ClientWaitingController manages the client waiting state screen.
 * <p>
 * This controller is responsible for:
 * <ul>
 *   <li>Allowing a client to cancel their waiting list registration</li>
 *   <li>Handling navigation back to the appropriate previous screen</li>
 *   <li>Displaying feedback messages regarding waiting list operations</li>
 * </ul>
 *
 * <p>
 * The controller interacts with the server via {@link BistroClient}
 * and updates the client-side waiting session state accordingly.
 */
public class ClientWaitingController {

	/**
     * Handles the action when the "Cancel Waiting" button is clicked.
     * <p>
     * Attempts to remove the client from the waiting list by sending
     * a {@link Action#REMOVE_FROM_WAITING_LIST} request to the server.
     * <p>
     * Displays an appropriate success or error message and navigates
     * back to the previous screen when applicable.
     *
     * @param event the action event triggered by the button click
     */
	@FXML
    void handleCancelWaiting(ActionEvent event) {
        System.out.println("Cancel Waiting clicked: Removing client from queue...");
        
        if (BistroClient.waitingVisit == null) {
            SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "No active waiting session found.");
            handleBack(event);
            return;
        }

        ClientUI.chat.accept(new BistroMessage(Action.REMOVE_FROM_WAITING_LIST, BistroClient.waitingVisit.getWaitingId()));
        
        if (BistroClient.operationSuccess) {
            BistroClient.waitingVisit = null; // Clear the waiting session
            SceneLoader.showAlert(Alert.AlertType.INFORMATION, "Cancelled", "You have been removed from the waiting list.");
            handleBack(event); // Navigate back to dashboard
        } else {
            SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel waiting. Please try again or contact staff.");
        }
	}

	/**
     * Handles navigation back from the waiting screen.
     * <p>
     * If the current user is a staff member, the window is closed.
     * Otherwise, the client is navigated back to the client dashboard.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    void handleBack(ActionEvent event) {
    	if(BistroClient.staffInstance != null) {
    		SceneLoader.closeWindow(event);
    	}else {
    		SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    	}
    }
}