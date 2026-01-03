package handlers;

import java.io.IOException;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


// Controller for the Visit Identification screen.

public class VisitIdentificationController {

    @FXML
    private TextField visitCodeField;

    @FXML
    private Button identifyButton;

    @FXML
    private Label statusLabel;


    // Handles the identification process when the user clicks the "Identify Visit" button.
    @FXML
    private void handleIdentification(ActionEvent event) throws IOException {
        String code = visitCodeField.getText().trim();
		FXMLLoader loader = new FXMLLoader();


        // Validate that the code field is not empty
        if (code.isEmpty()) {
            statusLabel.setText("Please enter your visit code.");
            statusLabel.setTextFill(Color.RED);
            return;
        }
        
        ClientUI.chat.accept(new BistroMessage(Action.GET_VERIFICATION_CODE, code));
        
        //TODO: If table ready  move to VisitDetails, if table is not ready move to ClientWaiting

        if(BistroClient.wantedVerCode == null || !BistroClient.wantedVerCode.equals(code))
		{
			ClientUI.chat.display("Wrong verification code");
			return;
		}
		else {
			System.out.println("Reservation Found");
			((Node)event.getSource()).getScene().getWindow().hide();
			Stage primaryStage = new Stage();
			Pane root = loader.load(getClass().getResource("/gui/VisitDetails.fxml").openStream());
			VisitDetailsController visitDetailsController = loader.getController();		
			visitDetailsController.loadReservation(BistroClient.reservationInstance);
		
			Scene scene = new Scene(root);
			primaryStage.setTitle("Visit Details");
			primaryStage.setScene(scene);		
			primaryStage.show();
		}
		
    }

    // Handles the "Forgot my code" action.
    @FXML
    private void handleForgotPassword(ActionEvent event) {
    	// TODO: send the client code via email and SMS
        statusLabel.setText("If the code exists, it has been sent to your Email/SMS.");
        statusLabel.setTextFill(Color.web("#1976d2"));
    }


    // Navigates back to the previous dashboard.
    @FXML
    private void handleBack(ActionEvent event) {
    	SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
        
    }
}