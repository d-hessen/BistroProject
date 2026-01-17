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
 * MainPageController controls the application's main entry screen.
 * <p>
 * This screen allows users to choose their role before entering the system:
 * <ul>
 *   <li>Client (member or guest)</li>
 *   <li>Staff</li>
 * </ul>
 *
 * <p>
 * Based on the user's selection, the controller navigates
 * to the appropriate login or dashboard screen.
 */
public class MainPageController {


    /**
     * Handles the action when the "Join for Clients" button is clicked.
     * <p>
     * Navigates the user to the client login options screen
     * (member or guest selection).
     *
     * @param event the action event triggered by the button click
     */
	@FXML
    private void openClientOptions(ActionEvent event) {
    	 SceneLoader.loadScene(event, "/gui/IsMemberGUI.fxml", "Client Login Options");
    }

	/**
     * Handles the action when the "Join for Staff" button is clicked.
     * <p>
     * Navigates the user to the staff login screen.
     *
     * @param event the action event triggered by the button click
     */
	@FXML
    private void openStaffOptions(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/StaffLoginGUI.fxml", "Staff Login");
    }

	/**
     * Starts and displays the main application page.
     * <p>
     * Loads the main page FXML, sets the scene on the primary stage,
     * and displays it to the user.
     *
     * @param primaryStage the primary stage of the JavaFX application
     * @throws Exception if the FXML file cannot be loaded
     */
	public void start(Stage primaryStage) throws Exception {	
		Parent root = FXMLLoader.load(getClass().getResource("/gui/MainPage.fxml"));
				
		Scene scene = new Scene(root);
		primaryStage.setTitle("Main Page");
		primaryStage.setScene(scene);
		
		primaryStage.show();	 	   
	}
}