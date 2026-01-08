package handlers;

import client.BistroClient;
import dataLayer.Reservation;
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
    private TableColumn<Reservation, String> timeColumn;

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

        timeColumn.setCellValueFactory(new PropertyValueFactory<>("reservationTime"));

        dinersColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        reservationsTable.setItems(reservationsList);

        loadReservations();
    }

    private void loadReservations() {

        if (BistroClient.memberInstance == null) {
            return;
        }

        // TODO: Get reservations from db
        // TODO: Save in memberInstance
        //if (BistroClient.memberInstance.getReservations() != null) {
           // reservationsList.setAll(BistroClient.memberInstance.getReservations());
      //  }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event,"/gui/ClientDashboard.fxml","Client Dashboard");
    }
}
