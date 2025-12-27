package handlers;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Reservation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ReservationFrameController {
	private ReservationFormController rfc;	

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
	
	public void Find_Reservation(ActionEvent event) throws Exception {
		String orderNumber, memberID;
		FXMLLoader loader = new FXMLLoader();
		
		orderNumber = getOrderNumber();
		memberID = getMemberID();
		
		try {
			if(orderNumber.trim().isEmpty())
			{
				ClientUI.chat.display("You must enter an order number");
			}
			else {
				if(memberID.trim().isEmpty())
				{
					ClientUI.chat.display("You must enter your member ID");
				}
				else {
					ClientUI.chat.accept(new BistroMessage(Action.GET_RESERVATION, orderNumber));
					
					if(BistroClient.wantedReservationId != Integer.parseInt(orderNumber))
					{
						ClientUI.chat.display("Reservation ID Not Found");
						notFound();
					}
					else {
						if(BistroClient.reservationInstance.getMemberId() != Integer.parseInt(memberID))
						{
							ClientUI.chat.display("Member ID Not Found");
							notFound();
						}
						else {
							System.out.println("Reservation Found");
							((Node)event.getSource()).getScene().getWindow().hide();
							Stage primaryStage = new Stage();
							Pane root = loader.load(getClass().getResource("/gui/ReservationForm.fxml").openStream());
							ReservationFormController reservationFormController = loader.getController();		
							reservationFormController.loadReservation(BistroClient.reservationInstance);
						
							Scene scene = new Scene(root);			
							primaryStage.setTitle("Reservation Managment");
				
							primaryStage.setScene(scene);		
							primaryStage.show();
						}
					}
				}
			}
		}catch(NullPointerException ex) {
			ex.printStackTrace();
			notFound();
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
		ClientUI.chat.display("exit Reservation Finder");
		Platform.exit();
		
	}
	
	public void notFound() {
		ClientUI.chat.display("Some details are wrong(Reservation ID or Member ID)");
		orderNumberField.setText("");
		memberIdField.setText("");
		ClientUI.chat.accept(new BistroMessage(Action.DISCONNECT,""));
	}
	
	public void loadReservation(Reservation reservation) {
		this.rfc.loadReservation(reservation);
	}	
	
}
