package gui;



import client.BistroClient;
import client.ClientUI;
import dataLayer.Reservation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

public class ReservationFrameController {
	
	@FXML
	private TextField orderNumberField;
	
	@FXML
	private TextField memberIdField;
	
	@FXML
	private Button findButton;
	
	@FXML
	private Button exitBtn;
	
	private String getOrderNumber() {
		return orderNumberField.getText();
	}
	
	private String getMemberID() {
		return memberIdField.getText();
	}
	
	public void FindReservation(ActionEvent event) throws Exception {
	    String orderNumber, memberID;

	    orderNumber = getOrderNumber();
	    memberID = getMemberID();

	    if(orderNumber.trim().isEmpty()) {
	        System.out.println("You must enter an order number");
	        return;
	    }

	    if(memberID.trim().isEmpty()) {
	        System.out.println("You must enter your subscriber ID");
	        return;
	    }

	    ClientUI.chat.accept(orderNumber);
	    ClientUI.chat.accept(memberID);

	    if(BistroClient.r1.getReservationId() != Integer.parseInt(orderNumber)) {
	        System.out.println("Reservation ID Not Found");
	        return;
	    }

	    if(BistroClient.r1.getMemberId() != Integer.parseInt(memberID)) {
	        System.out.println("Member ID Not Found");
	        return;
	    }

	    System.out.println("Reservation Found");

	    ((Node)event.getSource()).getScene().getWindow().hide();
	    Stage primaryStage = new Stage();

	    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ReservationForm.fxml"));
	    Pane root = loader.load();   // ✔️ התיקון הקריטי

	    ReservationFormController reservationFormController = loader.getController();
	    reservationFormController.loadReservation(BistroClient.r1);

	    Scene scene = new Scene(root);
	    primaryStage.setTitle("Reservation Management");
	    primaryStage.setScene(scene);
	    primaryStage.show();
	}

	public void start(Stage primaryStage) throws Exception {	
		Parent root = FXMLLoader.load(getClass().getResource("/gui/ReservationFrame.fxml"));
				
		Scene scene = new Scene(root);
		primaryStage.setTitle("Reservation Finder");
		primaryStage.setScene(scene);
		
		primaryStage.show();	 	   
	}
	
	public void getExitBtn(ActionEvent event) throws Exception {
		System.out.println("exit Reservation Finder");
		Platform.exit();
		
	}
	
}
