package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class MainPageController {

	@FXML
	private void openGuestUI(ActionEvent event) {
	    openUI(event, "GuestLoginGUI.fxml", "Guest Login");
	}

	@FXML
	private void openMemberUI(ActionEvent event) {
	    openUI(event, "MemberLoginGUI.fxml", "Member Login");
	}

	@FXML
	private void openStaffUI(ActionEvent event) {
	    openUI(event, "StaffLoginGUI.fxml", "Staff Login");
	}

	@FXML
	private void openManagerUI(ActionEvent event) {
	    openUI(event, "ManagerLoginGUI.fxml", "Manager Login");
	}


    private void openUI(ActionEvent event, String fxmlFile, String title) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/gui/" + fxmlFile)
            );

            Stage stage = new Stage();
            stage.setTitle(title + " Screen");
            stage.setScene(new Scene(root));
            stage.show();

            ((Node) event.getSource()).getScene().getWindow().hide();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	public void start(Stage primaryStage) throws Exception {	
		Parent root = FXMLLoader.load(getClass().getResource("/gui/mainPage.fxml"));
				
		Scene scene = new Scene(root);
		primaryStage.setTitle("Main Page");
		primaryStage.setScene(scene);
		
		primaryStage.show();	 	   
	}
}
