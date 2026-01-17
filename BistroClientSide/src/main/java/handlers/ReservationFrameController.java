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

/**
 * ReservationFrameController manages the reservation search screen.
 * <p>
 * This controller allows users (clients or staff) to locate an existing
 * reservation using a verification code. If found, the reservation
 * details screen is displayed.
 */
public class ReservationFrameController {

	/**
     * Text field used to enter the reservation verification code.
     */
    @FXML
    private TextField orderNumberField;

    /**
     * Button used to trigger the reservation search.
     */
    @FXML
    private Button findButton;

    /**
     * Button used to navigate back to the previous screen.
     */
    @FXML
    private Button backBtn;
    
    /**
     * Retrieves the verification code entered by the user.
     *
     * @return the verification code input as a {@link String}
     */
    private String getVerificationCodeInput() {
        return orderNumberField.getText();
    }
    
    /**
     * Handles the action when the "Find Reservation" button is clicked.
     * <p>
     * Validates the verification code input, sends a search request
     * to the server, and navigates to the reservation details screen
     * if a matching reservation is found.
     *
     * @param event the action event triggered by the button click
     * @throws Exception if an unexpected error occurs
     */
    public void Find_Reservation(ActionEvent event) throws Exception {
        String codeInput = getVerificationCodeInput();
        
        try {
            if (codeInput == null || codeInput.trim().isEmpty()) {
                SceneLoader.showAlert(Alert.AlertType.WARNING, "Input Error", "You must enter a verification code.");
                return;
            }
            
            // Clear previous reservation state
            BistroClient.reservationInstance = null;

            // Send search request to server
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

    /**
     * Starts and displays the reservation search window.
     *
     * @param primaryStage the primary stage of the JavaFX application
     * @throws Exception if the FXML file cannot be loaded
     */
    public void start(Stage primaryStage) throws Exception {    
        Parent root = FXMLLoader.load(getClass().getResource("/gui/ReservationFrame.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("Reservation Finder");
        primaryStage.setScene(scene);
        primaryStage.show();           
    }
    
    /**
     * Handles the action when the "Back" button is clicked.
     * <p>
     * If the user is staff, the current window is closed.
     * Otherwise, the client is returned to the dashboard.
     *
     * @param event the action event triggered by the button click
     * @throws Exception if navigation fails
     */
    public void handleBackBtn(ActionEvent event) throws Exception {
    	if(BistroClient.staffInstance != null) {
    		SceneLoader.closeWindow(event);
    	}else {
    		SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    	}
    }
}