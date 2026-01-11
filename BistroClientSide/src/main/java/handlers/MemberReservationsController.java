package handlers;

import java.util.ArrayList;
import java.util.List;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Reservation;
import dataLayer.Visit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class MemberReservationsController {

    @FXML
    private TableView<Reservation> reservationsTable;

    @FXML
    private TableColumn<Reservation, Integer> orderNumColumn;

    @FXML
    private TableColumn<Reservation, String> dateColumn;

    @FXML
    private TableColumn<Reservation, Integer> dinersColumn;

    @FXML
    private TableColumn<Reservation, String> statusColumn;

    private ObservableList<Reservation> reservationsList =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        orderNumColumn.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("reservationDate"));
        dinersColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        orderNumColumn.setStyle("-fx-alignment: CENTER;");
        dateColumn.setStyle("-fx-alignment: CENTER;");
        dinersColumn.setStyle("-fx-alignment: CENTER;");
        statusColumn.setStyle("-fx-alignment: CENTER;");
        
        reservationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        reservationsTable.setItems(reservationsList);
        
        reservationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                onReservationSelected(newSelection);
            }
        });
        
        loadReservations();
        }

    private void onReservationSelected(Reservation newSelection) {
        BistroClient.reservationInstance = newSelection;
        SceneLoader.openNewWindow("/gui/ReservationDetails.fxml","Client Dashboard");
	}

	private void loadReservations() {
    	ArrayList<Reservation> reservations = new ArrayList<>();
        if (BistroClient.memberInstance == null) {
            return;
        }  
        
        if (BistroClient.memberInstance != null) {
            //Request Member's History
            System.out.println("Requesting reservations history for member: " + BistroClient.memberInstance.getFullName());
            ClientUI.chat.accept(new BistroMessage(Action.GET_MEMBER_RESERVATIONS, BistroClient.memberInstance.getPhoneNumber()));
            //Get the list from the client static variable
            if (BistroClient.reservationsList != null) {
            	reservations.addAll(BistroClient.reservationsList);
            }
        }
        // Set the data to the table
        ObservableList<Reservation> observableReservations = FXCollections.observableArrayList(reservations);
        reservationsTable.setItems(observableReservations);      
    }
    
    public void updateReservationsTable(List<Reservation> reservations) {
        reservationsTable.setItems(FXCollections.observableArrayList(reservations));
    }
    
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event,"/gui/ClientDashboard.fxml","Client Dashboard");
    }
}
