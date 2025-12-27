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
 * Controller for the Main Page where users select their role (Client or Staff).
 */
public class MainPageController {


     // Handles the action when "Join for Clients" button is clicked.
    @FXML
    private void openClientOptions(ActionEvent event) {
    	 SceneLoader.loadScene(event, "/gui/IsMemberGUI.fxml", "Client Login Options");
    }


     // Handles the action when "Join for Staff" button is clicked.
    @FXML
    private void openStaffOptions(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/StaffLoginGUI.fxml", "Staff Login");
    }

	public void start(Stage primaryStage) throws Exception {	
		Parent root = FXMLLoader.load(getClass().getResource("/gui/MainPage.fxml"));
				
		Scene scene = new Scene(root);
		primaryStage.setTitle("Main Page");
		primaryStage.setScene(scene);
		
		primaryStage.show();	 	   
	}
}