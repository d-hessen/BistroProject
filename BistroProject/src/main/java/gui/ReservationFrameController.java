package gui;

import java.awt.Button;
import java.awt.TextField;

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

public class ReservationFrameController {
	private ReservationFormController rfc;	

	@FXML
	private TextField orderNumberField;
	
	@FXML
	private TextField subscriberIdField;
	
	@FXML
	private Button findButton;
	
	@FXML
	private Button exitBtn;
	
	private String getOrderNumber() {
		return orderNumberField.getText();
	}
	
	private String getSubscriberID() {
		return subscriberIdField.getText();
	}
	
	public void Find_Reservation(ActionEvent event) throws Exception {
		String orderNumber, memberID;
		FXMLLoader loader = new FXMLLoader();
		
		orderNumber = getOrderNumber();
		memberID = getSubscriberID();
		
		if(orderNumber.trim().isEmpty())
		{

			System.out.println("You must enter an order number");	
		}
		else {
			if(memberID.trim().isEmpty())
			{

				System.out.println("You must enter your subscriber ID");	
			}
			else {
				ClientUI.chat.accept(orderNumber);
				ClientUI.chat.accept(memberID);
				
				if(BistroClient.r1.getReservationId() != Integer.parseInt(orderNumber))
				{
					System.out.println("Reservation ID Not Found");
				}
				else {
					if(BistroClient.r1.getMemberId() != Integer.parseInt(memberID))
					{
						System.out.println("Member ID Not Found");
					}
					else {
						System.out.println("Reservation Found");
						((Node)event.getSource()).getScene().getWindow().hide();
						Stage primaryStage = new Stage();
						Pane root = loader.load(getClass().getResource("/gui/ReservationForm.fxml").openStream());
						ReservationFormController reservationFormController = loader.getController();		
						reservationFormController.loadReservation(BistroClient.r1);
					
						Scene scene = new Scene(root);			
						primaryStage.setTitle("Reservation Managment");
			
						primaryStage.setScene(scene);		
						primaryStage.show();
					}
				}
			}
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
		System.out.println("exit Reservation Finder");
		Platform.exit();
		
	}
	
	public void loadReservation(Reservation r1) {
		this.rfc.loadReservation(r1);
	}	
	
}
