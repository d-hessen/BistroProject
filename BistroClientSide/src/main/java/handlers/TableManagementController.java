package handlers;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Bill;
import dataLayer.Table;
import dataLayer.Visit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller responsible for managing a specific restaurant table.
 * <p>
 * This controller allows staff members to:
 * <ul>
 *   <li>View table details and status</li>
 *   <li>Edit table capacity</li>
 *   <li>Handle active visit billing</li>
 *   <li>Delete tables when permitted</li>
 * </ul>
 */
public class TableManagementController {

	/**
     * Text field displaying the table number.
     */
    @FXML
    private TextField tableNumberField;

    /**
     * Text field displaying and editing the table capacity.
     */
    @FXML
    private TextField capacityField;

    /**
     * ComboBox indicating the current table status.
     */
    @FXML
    private ComboBox<String> statusComboBox;

    /**
     * Button used to delete the table.
     */
    @FXML
    private Button deleteTableButton;

    /**
     * Container displaying visit-related details.
     */
    @FXML
    private VBox visitBox;

    /**
     * Text field displaying the visit number.
     */
    @FXML
    private TextField visitNumberField;

    /**
     * Text field displaying the party size.
     */
    @FXML
    private TextField partySizeField;

    /**
     * Text field displaying the visit start time.
     */
    @FXML
    private TextField startTimeField;

    /**
     * Text field displaying the bill total amount.
     */
    @FXML
    private TextField billField;

    /**
     * Text field displaying the discount percentage.
     */
    @FXML
    private TextField discountField;

    /**
     * The table currently being managed.
     */
    public static Table currentTable;

    /**
     * Initializes the controller after the FXML is loaded.
     * <p>
     * Sets initial UI state and registers this controller instance.
     */
    @FXML
    public void initialize() {
    	BistroClient.tableManagementControllerInstance = this;
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

    /**
     * Loads and displays details of the selected table.
     *
     * @param table the table selected from the staff dashboard
     */
    public boolean setTableDetails(Table table) {
        TableManagementController.currentTable = table; //Table that was clicked
        tableNumberField.setText(String.valueOf(table.getTableNumber()));
        capacityField.setText(String.valueOf(table.getTableCapacity()));
        try {
            if (table.isOccupied()) {
                visitBox.setVisible(true);
                visitBox.setManaged(true);
                statusComboBox.setValue("Occupied");
                visitNumberField.setText(String.valueOf(table.getCurrentVisit().getVisitId()));
                partySizeField.setText(String.valueOf(table.getCurrentVisit().getPartySize()));
                startTimeField.setText(table.getCurrentVisit().getStartTime().toString());
                DecimalFormat amount = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
                Double finalAmount = table.getCurrentVisit().getBillOfVisit().getTotalAmount();
                Double discountAmount = table.getCurrentVisit().getBillOfVisit().getDiscountAmount();
                billField.setText(amount.format(finalAmount)); 
                discountField.setText(amount.format(discountAmount));
            } else {
                statusComboBox.setValue("Available");  
            }
            return true;
        } catch(NullPointerException ex){
        	SceneLoader.showAlert(Alert.AlertType.ERROR, "Table Management", "This table is held for walk-in or not started yet. Wait and try again!");
        	return false;
        }

    }

    /**
     * Enables editing mode for table details when permitted.
     *
     * @param event the action event triggered by the edit button
     */
    @FXML
    void handleEdit(ActionEvent event) {
    	if(!currentTable.isOccupied()) {
            capacityField.setEditable(true);
            capacityField.setMouseTransparent(false);
            capacityField.requestFocus();
            deleteTableButton.setVisible(true);
            deleteTableButton.setManaged(true);
    	} else {
    		SceneLoader.showAlert(Alert.AlertType.ERROR, "Editing Table", "You can not edit table while there is active visit!");
    	}
    }
    
    /**
     * Navigates to the payment screen for the current visit.
     *
     * @param event the action event triggered by the pay button
     */
    @FXML
    void handlePay(ActionEvent event) {
    	VisitDetailsController.visitInstance = currentTable.getCurrentVisit();
    	SceneLoader.switchScreen(event
    							,"/gui/PaymentScreen.fxml"
    							,"Payment screen"
    							,(PaymentScreenController controller) -> {
    								// This code runs after the controller is loaded
    								controller.payVisit(currentTable.getCurrentVisit());
	    });
    }

    /**
     * Handles deletion of the current table after confirmation.
     *
     * @param event the action event triggered by the delete button
     */
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

    /**
     * Saves changes to the table or visit billing information.
     *
     * @param event the action event triggered by the save button
     */
    @FXML
    void handleSave(ActionEvent event) {
    	if(currentTable.isOccupied()) {
    		Double amount = Double.valueOf(billField.getText().trim());
    		Double discount = Double.valueOf(discountField.getText().trim());
    		Double finalAmount = (amount - (discount/100)*amount);
    		Visit toUpdate = currentTable.getCurrentVisit();
    		toUpdate.getBillOfVisit().setTotalAmount(amount);
    		toUpdate.getBillOfVisit().setDiscountAmount(discount);
    		toUpdate.getBillOfVisit().setFinalAmount(finalAmount);
    		ClientUI.chat.accept(new BistroMessage(Action.UPDATE_BILL, toUpdate));
    	} else {
    		Integer capacity = Integer.valueOf(capacityField.getText().trim());
    		Table toUpdate = currentTable;
    		toUpdate.setTableCapacity(capacity);
    		ClientUI.chat.accept(new BistroMessage(Action.UPDATE_TABLE, toUpdate));
    	}
    }
    
    /**
     * Receives update confirmation from the server
     * and refreshes the table or visit details accordingly.
     *
     * @param updated the updated Table or Visit object
     */
    public void updated(Object updated) {
    	Platform.runLater(()->{
    		if(updated == null) {
    			SceneLoader.showAlert(Alert.AlertType.ERROR, "Error during updating table", "There was an error during saving changes!");
    			return;
    		} else {
    			SceneLoader.showAlert(Alert.AlertType.INFORMATION, "Edit details successful", "Details were updated succesfully!");
    		}
    		if(updated instanceof Table) {
    			Table table = (Table)updated;
    			setTableDetails(table);
    			
    		}else if (updated instanceof Visit) {
    			Visit visit = (Visit)updated;
    			TableManagementController.currentTable.setCurrentVisit(visit);
    			setTableDetails(currentTable);
    		}
    	});
    }

    /**
     * Cancels the operation and closes the management window.
     *
     * @param event the action event triggered by the cancel button
     */
    @FXML
    void handleCancel(ActionEvent event) {
        SceneLoader.closeWindow(event);
    }
    
}
