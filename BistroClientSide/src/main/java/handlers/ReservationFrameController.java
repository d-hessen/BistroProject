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
    private Button exitBtn;
    
    private String getVerificationCodeInput() {
        return orderNumberField.getText();
    }
    
    public void Find_Reservation(ActionEvent event) throws Exception {
        String codeInput = getVerificationCodeInput();
        FXMLLoader loader = new FXMLLoader();
        
        try {
            if (codeInput == null || codeInput.trim().isEmpty()) {
                SceneLoader.showAlert(Alert.AlertType.WARNING, "Input Error", "You must enter a verification code.");
                return;
            }
            
            // 1. Reset static instance
            BistroClient.reservationInstance = null;

            // 2. Request from server
            ClientUI.chat.accept(new BistroMessage(Action.GET_VERIFICATION_CODE, codeInput));
            
            // 3. Check result
            if (BistroClient.reservationInstance != null) {
                System.out.println("Reservation Found");
                
                // Hide current window
                ((Node)event.getSource()).getScene().getWindow().hide();
                
                // Load ReservationDetails.fxml
                Stage primaryStage = new Stage();
                Pane root = loader.load(getClass().getResource("/gui/ReservationDetails.fxml").openStream());
                
                Scene scene = new Scene(root);          
                primaryStage.setTitle("Reservation Details");
                primaryStage.setScene(scene);       
                primaryStage.show();
                
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
    
    public void getExitBtn(ActionEvent event) throws Exception {
        if (BistroClient.memberInstance != null) {
            SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
        } else {
            SceneLoader.closeWindow(event);
        }
    }
}