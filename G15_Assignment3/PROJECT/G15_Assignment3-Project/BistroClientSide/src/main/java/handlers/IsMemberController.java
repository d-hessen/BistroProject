package handlers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * IsMemberController manages the client entry decision screen.
 * <p>
 * This controller allows the user to choose whether to log in
 * as a registered member or continue as a guest.
 * <p>
 * Based on the user's selection, the controller navigates
 * to the appropriate next screen.
 */
public class IsMemberController {

	/**
     * Handles the action when the "Log In" button is clicked.
     * <p>
     * Navigates the user to the member login screen.
     *
     * @param event the action event triggered by the button click
     */
	@FXML
    private void openClientOptions(ActionEvent event) {
    	 SceneLoader.loadScene(event, "/gui/MemberLoginGUI.fxml", "Member Login");
    }
    
	/**
     * Handles the action when the "Join As Guest" button is clicked.
     * <p>
     * Navigates the user directly to the client dashboard
     * without requiring authentication.
     *
     * @param event the action event triggered by the button click
     */
	@FXML
    private void openGuestOptions(ActionEvent event) {
    	 SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    }
    
	/**
     * Handles the action when the "Back" button is clicked.
     * <p>
     * Navigates the user back to the main application page.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void handleBack(ActionEvent event) {
    	SceneLoader.loadScene(event, "/gui/MainPage.fxml", "Client Dashboard");
    }

}