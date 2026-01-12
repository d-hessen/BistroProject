package handlers;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ReservationFrameController {

    @FXML
    private TextField orderNumberField; // Holds Verification Code
    
    @FXML
    private Button findButton;
    
    @FXML
    private Button backBtn;
    
    private String getVerificationCodeInput() {
        return orderNumberField.getText();
    }
    
    public void Find_Reservation(ActionEvent event) throws Exception {
        String codeInput = getVerificationCodeInput();
        
        try {
            if (codeInput == null || codeInput.trim().isEmpty()) {
                SceneLoader.showAlert(Alert.AlertType.WARNING, "Input Error", "You must enter a verification code.");
                return;
            }
            BistroClient.reservationInstance = null;

            ClientUI.chat.accept(new BistroMessage(Action.FIND_RESERVATION, codeInput));
            if (BistroClient.reservationInstance != null) {
                System.out.println("Reservation Found");
                SceneLoader.loadScene(event, "/gui/ReservationDetails.fxml", "Reservation Details");
                
            } else {
                SceneLoader.showAlert(Alert.AlertType.ERROR, "Not Found", "No reservation found with this verification code.");
                orderNumberField.clear();
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            SceneLoader.showAlert(Alert.AlertType.ERROR, "System Error", "An error occurred while searching.");
        }
    }

    public void start(Stage primaryStage) throws Exception {    
        Parent root = FXMLLoader.load(getClass().getResource("/gui/ReservationFrame.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("Reservation Finder");
        primaryStage.setScene(scene);
        primaryStage.show();           
    }
    
    public void handleBackBtn(ActionEvent event) throws Exception {
    	if(BistroClient.staffInstance != null) {
    		SceneLoader.closeWindow(event);
    	}else {
    		SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    	}
    }
}