package handlers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Guest; 
import dataLayer.Member; 
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
 * Controller responsible for managing the Staff Waiting List screen.
 * <p>
 * This controller allows staff members to:
 * <ul>
 *   <li>View the current waiting list</li>
 *   <li>Refresh the list from the server</li>
 *   <li>Remove customers from the queue</li>
 * </ul>
 */
public class StaffWaitingListController implements Initializable {

	/**
     * TableView displaying the list of visits currently waiting.
     */
    @FXML
    private TableView<Visit> waitingListTable;

    /**
     * Column displaying the verification or waiting code of the visit.
     */
    @FXML
    private TableColumn<Visit, String> codeCol;

    /**
     * Column displaying the guest or member name.
     */
    @FXML
    private TableColumn<Visit, String> nameCol;

    /**
     * Column displaying the party size for the visit.
     */
    @FXML
    private TableColumn<Visit, Integer> sizeCol;

    /**
     * Column displaying the contact information (phone or email).
     */
    @FXML
    private TableColumn<Visit, String> contactCol;

    // Observable list to hold the data for the table
    private ObservableList<Visit> waitingList = FXCollections.observableArrayList();


    /**
     * Initializes the controller after the FXML has been loaded.
     * <p>
     * Registers the controller instance and prepares the table layout.
     *
     * @param location the location used to resolve relative paths
     * @param resources the resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Register this controller instance in the client to receive updates from server
        BistroClient.staffWaitingListControllerInstance = this;

        setupTableColumns();        
        handleRefresh(null); // Load data when opening the screen
    }

    /**
     * Configures the table columns and binds them to Visit properties.
     */
    private void setupTableColumns() {
        // Map Verification Code
        codeCol.setCellValueFactory(cellData -> {
            String vCode = cellData.getValue().getVerificationCode();
            // If verification code is missing, fallback to ID
            if (vCode != null && !vCode.isEmpty()) {
                return new SimpleStringProperty(vCode);
            }
            return new SimpleStringProperty(String.valueOf(cellData.getValue().getWaitingId()));
        });

        // Map Customer Name - Check if the guest is a Member
        nameCol.setCellValueFactory(cellData -> {
            Visit v = cellData.getValue();
            Guest g = v.getGuest(); 
            
            if (g instanceof Member) {
                return new SimpleStringProperty(g.getFullName() + " (Member)");
            } else if (g != null) {
                return new SimpleStringProperty(g.getFullName());
            }
            return new SimpleStringProperty("Unknown");
        });

        // Map Diners directly from the Visit property
        sizeCol.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getPartySize()));

        // Map Contact Info
        contactCol.setCellValueFactory(cellData -> {
            Visit v = cellData.getValue();
            Guest g = v.getGuest();
            String contactInfo = "-";
            
            if (g != null) {
                if (g.getPhoneNumber() != null && !g.getPhoneNumber().isEmpty()) {
                    contactInfo = g.getPhoneNumber();
                } else if (g.getEmail() != null && !g.getEmail().isEmpty()) {
                    contactInfo = g.getEmail();
                }
            }
            return new SimpleStringProperty(contactInfo);
        });

        // Bind the data list to the table
        waitingListTable.setItems(waitingList);
    }

    /**
     * Requests the updated waiting list from the server
     * and refreshes the table content.
     *
     * @param event the action event triggered by the refresh button
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        // Create a message with the action to get the waiting list
        BistroMessage msg = new BistroMessage(Action.GET_WAITING_LIST, null);
        ClientUI.chat.accept(msg);
        this.waitingList.clear();
        if (BistroClient.waitingList != null && !BistroClient.waitingList.isEmpty()) {
            waitingList.addAll(BistroClient.waitingList);
        }

    }

    /**
     * Removes the selected customer from the waiting list.
     * <p>
     * Prompts the user for confirmation before sending
     * the removal request to the server.
     *
     * @param event the action event triggered by the remove button
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
        
        String name = "Unknown";
        if (selectedVisit.getGuest() != null) {
            name = selectedVisit.getGuest().getFullName();
        }
        
        confirmAlert.setContentText("Are you sure you want to remove " + name + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Send request to remove the customer using waiting ID
                BistroMessage msg = new BistroMessage(Action.REMOVE_FROM_WAITING_LIST, selectedVisit.getWaitingId());
                ClientUI.chat.accept(msg);                
                waitingList.remove(selectedVisit);
            }
        });
    }

    /**
     * Handles navigation back to the Staff Dashboard screen.
     * <p>
     * Closes the current window and unregisters this controller.
     *
     * @param event the action event triggered by the back button
     */
    @FXML
    private void handleBack(ActionEvent event) {
        // Unregister this controller to avoid memory leaks or unwanted updates
        BistroClient.staffWaitingListControllerInstance = null;
        SceneLoader.closeWindow(event);
    }

    /**
     * Updates the waiting list table with data received from the server.
     * <p>
     * This method is called asynchronously by the client communication layer.
     *
     * @param list the updated list of visits currently waiting
     */
    public void updateWaitingList(List<Visit> list) {
        Platform.runLater(() -> {

        });
    }
}