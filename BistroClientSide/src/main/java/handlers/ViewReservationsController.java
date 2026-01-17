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

/**
 * Controller responsible for displaying all reservations in the system.
 * <p>
 * This screen is intended for staff and management use and presents
 * reservation data in a tabular format, including guest details,
 * reservation status, and membership information.
 */
public class ViewReservationsController implements Initializable {

	/**
     * Table displaying all reservations.
     */
    @FXML
    private TableView<Reservation> reservationsTable;

    /**
     * Column displaying reservation ID.
     */
    @FXML
    private TableColumn<Reservation, Integer> resIdCol;

    /**
     * Column displaying reservation date and time.
     */
    @FXML
    private TableColumn<Reservation, String> dateCol;

    /**
     * Column displaying number of guests.
     */
    @FXML
    private TableColumn<Reservation, Integer> guestsCol;

    /**
     * Column displaying reservation verification code.
     */
    @FXML
    private TableColumn<Reservation, String> verifyCodeCol;

    /**
     * Column displaying reservation creation date.
     */
    @FXML
    private TableColumn<Reservation, String> createdDateCol;

    /**
     * Column displaying guest full name.
     */
    @FXML
    private TableColumn<Reservation, String> nameCol;

    /**
     * Column displaying guest phone number.
     */
    @FXML
    private TableColumn<Reservation, String> phoneCol;

    /**
     * Column displaying guest email address.
     */
    @FXML
    private TableColumn<Reservation, String> emailCol;

    /**
     * Column displaying reservation status.
     */
    @FXML
    private TableColumn<Reservation, String> statusCol;

    /**
     * Column indicating whether the reservation was made by a registered member.
     */
    @FXML
    private TableColumn<Reservation, String> isMemberCol;

    /**
     * Initializes the controller after the FXML is loaded.
     * <p>
     * Configures table columns and triggers loading of reservation data.
     *
     * @param location  the location used to resolve relative paths
     * @param resources the resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadData();
    }

    /**
     * Configures the table columns and binds them to Reservation properties.
     */
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

    /**
     * Requests reservation data from the server and populates the table.
     */
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