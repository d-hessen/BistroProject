package handlers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Visit;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller for the Staff Waiting List management screen.
 * Allows staff to view the current queue, refresh it, and remove customers (mark as seated).
 */
public class StaffWaitingListController implements Initializable {

    @FXML private TableView<Visit> waitingListTable;
    @FXML private TableColumn<Visit, String> codeCol;
    @FXML private TableColumn<Visit, String> nameCol;
    @FXML private TableColumn<Visit, Integer> sizeCol;
    @FXML private TableColumn<Visit, String> contactCol;

    // Observable list to hold the data for the table
    private ObservableList<Visit> waitingList = FXCollections.observableArrayList();

    /**
     * Initializes the controller class.
     * Sets up the table columns and loads the initial data.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Register this controller instance in the client to receive updates from server
        BistroClient.staffWaitingListControllerInstance = this;

        setupTableColumns();       
        handleRefresh(null); // Load data when opening the screen
    }

    /**
     * Configures the TableView columns to map data from the Visit object.
     */
    private void setupTableColumns() {
        // Map "Verification Code" to the Visit ID 
        codeCol.setCellValueFactory(cellData -> {
            // If Visit doesn't have a dedicated "code" field, we use its ID or a placeholder
            return new SimpleStringProperty(String.valueOf(cellData.getValue().getVisitId()));
        });

        // Map "Customer Name" from the nested Guest object
        nameCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getGuest() != null) {
                return new SimpleStringProperty(cellData.getValue().getGuest().getFullName());
            }
            return new SimpleStringProperty("Unknown");
        });

        // Map "Diners" directly from the Visit property
        sizeCol.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getPartySize()));

        // Map "Contact Info" (Phone or Email) from the nested Guest object
        contactCol.setCellValueFactory(cellData -> {
            var guest = cellData.getValue().getGuest();
            if (guest != null) {
                if (guest.getPhoneNumber() != null && !guest.getPhoneNumber().isEmpty()) {
                    return new SimpleStringProperty(guest.getPhoneNumber());
                } else if (guest.getEmail() != null) {
                    return new SimpleStringProperty(guest.getEmail());
                }
            }
            return new SimpleStringProperty("-");
        });

        // Bind the data list to the table
        waitingListTable.setItems(waitingList);
    }

    /**
     * Sends a request to the server to get the latest waiting list.
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        // Create a message with the action to get the waiting list
        BistroMessage msg = new BistroMessage(Action.GET_WAITING_LIST, null);
        ClientUI.chat.accept(msg);
    }

    /**
     * Handles the removal of a customer from the list (for example, when they are seated).
     */
    @FXML
    private void handleRemoveCustomer(ActionEvent event) {
        Visit selectedVisit = waitingListTable.getSelectionModel().getSelectedItem();

        if (selectedVisit == null) {
            SceneLoader.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a customer to remove.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Removal");
        confirmAlert.setHeaderText("Remove Customer from Queue?");
        confirmAlert.setContentText("Are you sure you want to remove " + selectedVisit.getGuest().getFullName() + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Send request to remove the customer/visit using its ID
                BistroMessage msg = new BistroMessage(Action.REMOVE_FROM_WAITING_LIST, selectedVisit.getVisitId());
                ClientUI.chat.accept(msg);
                
                // Optimistically remove from the view (or wait for refresh)
                waitingList.remove(selectedVisit);
            }
        });
    }

    /**
     * Navigation back to the Staff Dashboard.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        // Unregister this controller to avoid memory leaks or unwanted updates
        BistroClient.staffWaitingListControllerInstance = null;
        SceneLoader.closeWindow(event);
    }

    /**
     * Method called by BistroClient when the server sends the updated waiting list.
     * @param list The list of Visit objects representing the queue.
     */
    public void updateWaitingList(List<Visit> list) {
        Platform.runLater(() -> {
            waitingList.clear();
            if (list != null && !list.isEmpty()) {
                waitingList.addAll(list);
            }
        });
    }
}