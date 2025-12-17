package gui;

import java.io.IOException;
import java.time.LocalDate;
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
import javafx.stage.Stage;
import dataLayer.Reservation;
import javafx.scene.control.DatePicker;


public class ReservationFormController {
	@FXML
	private Label orderNumberLabel;
	@FXML 
	private Label confirmationCodeLabel;
	@FXML
	private Label MemberLabel;
	@FXML 
	private Label placingOrderDateLabel;
	@FXML
	private TextField numberOfGuestsField;
	@FXML 
	private DatePicker orderDatePicker;
	@FXML
	private Button btnSave_Changes;
    @FXML
    private Button btnBack;
    
    private Reservation reservation; //reservation for controller


	public void loadReservation(Reservation reservation) {
		this.reservation = reservation;
		if(reservation == null) {
			return;
		}
		orderNumberLabel.setText(String.valueOf(reservation.getReservationId()));
		confirmationCodeLabel.setText(String.valueOf(reservation.getVerificationCode()));
		MemberLabel.setText(String.valueOf(reservation.getMemberId()));
		placingOrderDateLabel.setText(reservation.getDateOfPlacingReservation());
		numberOfGuestsField.setText(String.valueOf(reservation.getNumberOfGuests()));
		
		//converts the String date to LocalDate
        try {
            if (reservation.getReservationDate() != null && !reservation.getReservationDate().isEmpty()) {
            	orderDatePicker.setValue(LocalDate.parse(reservation.getReservationDate()));
            }
        } catch (Exception e) {
        	orderDatePicker.setValue(null); // if string not in yyyy-MM-dd
        }
    }
	
	public void buttonBack(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		((Node)event.getSource()).getScene().getWindow().hide(); //hiding primary window
		Stage primaryStage = new Stage();
		Pane root = loader.load(getClass().getResource("/gui/ReservationFrame.fxml").openStream());
	
		Scene scene = new Scene(root);			
		primaryStage.setTitle("Reservation Finder");
		primaryStage.setScene(scene);		
		primaryStage.show();
		ClientUI.chat.accept(new BistroMessage(Action.DISCONNECT,""));
	}
	
	public void save(ActionEvent event) {
        Integer guests = Integer.parseInt(numberOfGuestsField.getText().trim());
        if(guests.equals(null) || guests == 0) { //If user tries to set guest number as 0
            System.out.println("Guest Number can not be null");
            numberOfGuestsField.setText(String.valueOf(reservation.getNumberOfGuests()));
            return;
        }
        else {
        	reservation.setNumberOfGuests(guests);
            reservation.setReservationDate(orderDatePicker.getValue().toString());
            ClientUI.chat.accept(new BistroMessage(Action.UPDATE_RESERVATION,reservation));
        }
	}
}
