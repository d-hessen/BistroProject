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

/**
 * MemberReservationsController manages the "My Reservations" screen for members.
 * <p>
 * This controller is responsible for:
 * <ul>
 *   <li>Displaying the logged-in member's reservations in a table</li>
 *   <li>Requesting reservation data from the server</li>
 *   <li>Handling reservation selection and navigation to reservation details</li>
 *   <li>Providing navigation back to the client dashboard</li>
 * </ul>
 */
public class MemberReservationsController {
    /**
     * Table view displaying the member's reservations.
     */
    @FXML
    private TableView<Reservation> reservationsTable;

    /**
     * Column displaying the reservation ID.
     */
    @FXML
    private TableColumn<Reservation, Integer> orderNumColumn;

    /**
     * Column displaying the reservation date.
     */
    @FXML
    private TableColumn<Reservation, String> dateColumn;

    /**
     * Column displaying the number of diners.
     */
    @FXML
    private TableColumn<Reservation, Integer> dinersColumn;

    /**
     * Column displaying the reservation status.
     */
    @FXML
    private TableColumn<Reservation, String> statusColumn;

    /**
     * Observable list used as the data source for the reservations table.
     */
    private ObservableList<Reservation> reservationsList =
            FXCollections.observableArrayList();

    /**
     * Initializes the reservations table after the FXML has been loaded.
     * <p>
     * Sets up table columns, alignment, selection listeners,
     * and loads the member's reservations from the server.
     */
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

    /**
     * Handles selection of a reservation from the table.
     * <p>
     * Stores the selected reservation in the client state and
     * opens the reservation details window.
     *
     * @param newSelection the selected {@link Reservation}
     */
    private void onReservationSelected(Reservation newSelection) {
        BistroClient.reservationInstance = newSelection;
        SceneLoader.openNewWindow("/gui/ReservationDetails.fxml","Client Dashboard");
	}

    /**
     * Loads the logged in member's reservations from the server.
     * <p>
     * Sends a {@link Action#GET_MEMBER_RESERVATIONS} request and
     * populates the table using the response stored in the client state.
     */
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
    
	/**
     * Updates the reservations table with a new list of reservations.
     *
     * @param reservations the updated list of {@link Reservation} objects
     */
    public void updateReservationsTable(List<Reservation> reservations) {
        reservationsTable.setItems(FXCollections.observableArrayList(reservations));
    }
    
    /**
     * Handles the action when the "Back" button is clicked.
     * <p>
     * Navigates the user back to the client dashboard.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event,"/gui/ClientDashboard.fxml","Client Dashboard");
    }
}
