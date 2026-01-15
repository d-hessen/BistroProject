package handlers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Reservation;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ViewReservationsController implements Initializable {

    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, Integer> resIdCol;
    @FXML private TableColumn<Reservation, String> dateCol;
    @FXML private TableColumn<Reservation, Integer> guestsCol;
    @FXML private TableColumn<Reservation, String> verifyCodeCol;
    @FXML private TableColumn<Reservation, String> createdDateCol;
    @FXML private TableColumn<Reservation, String> nameCol;
    @FXML private TableColumn<Reservation, String> phoneCol;
    @FXML private TableColumn<Reservation, String> emailCol;
    @FXML private TableColumn<Reservation, String> statusCol;
    @FXML private TableColumn<Reservation, String> isMemberCol;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadData();
    }

    private void setupTableColumns() {
        resIdCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        guestsCol.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        verifyCodeCol.setCellValueFactory(new PropertyValueFactory<>("verificationCode"));
        createdDateCol.setCellValueFactory(new PropertyValueFactory<>("dateOfPlacingReservation"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Date and time combination
        dateCol.setCellValueFactory(cellData -> {
            Reservation r = cellData.getValue();
            if (r.getReservationDate() != null) {
                return new SimpleStringProperty(r.getReservationDate().getDate() + " " + r.getReservationDate().getTime());
            }
            return new SimpleStringProperty("");
        });

        // Guest data
        nameCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getGuest() != null)
                return new SimpleStringProperty(cellData.getValue().getGuest().getFullName());
            return new SimpleStringProperty("N/A");
        });

        phoneCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getGuest() != null)
                return new SimpleStringProperty(cellData.getValue().getGuest().getPhoneNumber()); 
            return new SimpleStringProperty("N/A");
        });
        
        emailCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getGuest() != null)
                return new SimpleStringProperty(cellData.getValue().getGuest().getEmail());
            return new SimpleStringProperty("N/A");
        });

        // Checking if member
        isMemberCol.setCellValueFactory(cellData -> {
            Integer mId = cellData.getValue().getMemberId();
            return new SimpleStringProperty((mId != null && mId > 0) ? "Yes" : "No");
        });
    }

    private void loadData() {
        // Reset the local list
        BistroClient.allReservationsList = null;
        
        // Request from the server
        ClientUI.chat.accept(new BistroMessage(Action.GET_ALL_RESERVATIONS, null));

        // Short wait and loading the data
        new Thread(() -> {
            try {
                Thread.sleep(500); // Give the server time to respond
            } catch (InterruptedException e) { e.printStackTrace(); }
            
            Platform.runLater(() -> {
                if (BistroClient.allReservationsList != null) {
                    reservationsTable.setItems(FXCollections.observableArrayList(BistroClient.allReservationsList));
                }
            });
        }).start();
    }
}