package handlers;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Table;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class TableManagementController {

    @FXML private TextField tableNumberField;
    @FXML private TextField capacityField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button deleteTableButton;
    
    // Visit Details
    @FXML private VBox visitBox;
    @FXML private TextField visitNumberField;
    @FXML private TextField partySizeField;
    @FXML private TextField startTimeField;
    @FXML private TextField billField;

    private Table currentTable;

    @FXML
    public void initialize() {
        // Initialize Combo Box
        statusComboBox.getItems().addAll("Available", "Occupied", "Reserved", "Deleted");
        statusComboBox.setDisable(true);
        capacityField.setEditable(false);
        capacityField.setMouseTransparent(true);
        visitBox.setVisible(false);
        visitBox.setManaged(false);
        deleteTableButton.setVisible(false);
        deleteTableButton.setManaged(false);
    }

    // Method called by StaffDashboardController to pass data
    public void setTableDetails(Table table) {
        this.currentTable = table; //Table that was clicked
        tableNumberField.setText(String.valueOf(table.getTableNumber()));
        capacityField.setText(String.valueOf(table.getTableCapacity()));
        
        if (table.isOccupied()) { 
            visitBox.setVisible(true);
            visitBox.setManaged(true);
            statusComboBox.setValue("Occupied");
            visitNumberField.setText(String.valueOf(table.getCurrentVisit().getVisitId()));
            partySizeField.setText(String.valueOf(table.getCurrentVisit().getPartySize()));
            startTimeField.setText(table.getCurrentVisit().getStartTime().toString());
            DecimalFormat amount = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
            Double finalAmount = table.getCurrentVisit().getBillOfVisit().getFinalAmount();
            billField.setText(amount.format(finalAmount)); 
        } else {
            statusComboBox.setValue("Available");
            
        }
    }

    @FXML
    void handleEdit(ActionEvent event) {
        capacityField.setEditable(true);
        capacityField.setMouseTransparent(false);
        capacityField.requestFocus();
        statusComboBox.setDisable(false);
        deleteTableButton.setVisible(true);
        deleteTableButton.setManaged(true);
    }
    
    @FXML
    void handlePay(ActionEvent event) {
    	SceneLoader.showAlert(Alert.AlertType.INFORMATION, "Bill", "Total bill is: " + currentTable.getCurrentVisit().getBillOfVisit().getFinalAmount());
    	SceneLoader.openNewWindow("/gui/PaymentScreen.fxml", "Payment screen");
    }

    @FXML
    void handleDelete(ActionEvent event) {
    	boolean hasConfirmed = SceneLoader.showConfirmationAlert("Delete table", "Are you sure you want to delete this table?");
    	if(hasConfirmed) {
        	if(currentTable.isOccupied()) {
        		SceneLoader.showAlert(Alert.AlertType.ERROR, "Delete table", "You can't delete table when it's occupied");
        		return;
        	}
    		ClientUI.chat.accept(new BistroMessage(Action.DELETE_TABLE, currentTable));
    		if(BistroClient.operationSuccess) {
    			BistroClient.operationSuccess = false;
    			SceneLoader.closeWindow(event);
    			BistroClient.tables = new ArrayList<>();
    			ClientUI.chat.accept(new BistroMessage(Action.GET_ALL_TABLES,null));
    		}
    		
    	} else {
    		return;
    	}
    }

    @FXML
    void handleSave(ActionEvent event) {
        try {
            Integer newCap = Integer.parseInt(capacityField.getText());
            if(statusComboBox.getValue().equals("Available")) {
            	currentTable.setActive(true);
            } else if(statusComboBox.getValue().equals("Occupied")) {
            	currentTable.setOccupied(true);;
            }else {
            	currentTable.setActive(false);
            }
            currentTable.setTableCapacity(newCap);
            // Send update to Server
            ClientUI.chat.accept(new BistroMessage(Action.UPDATE_TABLE, currentTable));
            
            if(BistroClient.operationSuccess) {
            	BistroClient.operationSuccess = false;
            	SceneLoader.closeWindow(event);
            	BistroClient.tables = new ArrayList<>();
    			ClientUI.chat.accept(new BistroMessage(Action.GET_ALL_TABLES,null));
            }
            
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Capacity must be a number").show();
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        SceneLoader.closeWindow(event);
    }
    
}
