package handlers;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.RestaurantConfig;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller responsible for managing system-wide restaurant settings.
 * <p>
 * This controller allows authorized staff to:
 * <ul>
 *   <li>Configure regular weekly opening hours</li>
 *   <li>Manage special opening dates</li>
 *   <li>Persist configuration changes to the server</li>
 * </ul>
 */
public class SystemSettingsController implements Initializable {

	/**
     * Grid layout containing weekly opening hours fields.
     */
    @FXML
    private GridPane gridDays;

    /**
     * Date picker used to select a special date.
     */
    @FXML
    private DatePicker datePicker;

    /**
     * Text field for entering special opening hour.
     */
    @FXML
    private TextField txtSpecialOpen;

    /**
     * Text field for entering special closing hour.
     */
    @FXML
    private TextField txtSpecialClose;

    /**
     * Table displaying special opening dates and hours.
     */
    @FXML
    private TableView<Map.Entry<LocalDate, String[]>> tblSpecialDates;

    /**
     * Column displaying the special date.
     */
    @FXML
    private TableColumn<Map.Entry<LocalDate, String[]>, String> colDate;

    /**
     * Column displaying the opening hour for a special date.
     */
    @FXML
    private TableColumn<Map.Entry<LocalDate, String[]>, String> colOpen;

    /**
     * Column displaying the closing hour for a special date.
     */
    @FXML
    private TableColumn<Map.Entry<LocalDate, String[]>, String> colClose;

    /**
     * Column containing delete actions for special dates.
     */
    @FXML
    private TableColumn<Map.Entry<LocalDate, String[]>, Void> colDelete;

    /**
     * Map holding weekly opening and closing fields for each day.
     * <p>
     * Key: Day name (e.g., "Monday")  
     * Value: Array of TextFields [open, close]
     */
    private Map<String, TextField[]> dayFields = new HashMap<>();
    
    /**
     * Current restaurant configuration loaded from the server.
     */
    private RestaurantConfig currentConfig;
    
    /**
     * Singleton instance of the SystemSettingsController.
     */
    private static SystemSettingsController instance;

    /**
     * Initializes the controller after its FXML has been loaded.
     * <p>
     * Sets up table configuration and requests current system settings.
     *
     * @param location  the location used to resolve relative paths
     * @param resources the resources used for localization
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        setupTable();
        ClientUI.chat.accept(new BistroMessage(Action.GET_RESTAURANT_CONFIG, null));
    }

    /**
     * Configures the special dates table columns and actions.
     */
    private void setupTable() {
        colDate.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey().toString()));
        colOpen.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue()[0]));
        colClose.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue()[1]));
        
        Callback<TableColumn<Map.Entry<LocalDate, String[]>, Void>, TableCell<Map.Entry<LocalDate, String[]>, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Map.Entry<LocalDate, String[]>, Void> call(final TableColumn<Map.Entry<LocalDate, String[]>, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Delete");
                    {
                        btn.setStyle("-fx-text-fill: red;"); 
                        btn.setOnAction((ActionEvent event) -> {
                            // Remove the row from the table's list 
                            getTableView().getItems().remove(getIndex());
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };
        colDelete.setCellFactory(cellFactory);
    }
    
    /**
     * Loads restaurant configuration data into the UI.
     *
     * @param config the configuration received from the server
     */
    public void setConfigData(RestaurantConfig config) {
        Platform.runLater(() -> {
            this.currentConfig = config;
            buildWeeklyGrid();
            
            // ObservableList so we can delete from it 
            ObservableList<Map.Entry<LocalDate, String[]>> list = FXCollections.observableArrayList(config.getSpecialHours().entrySet());
            tblSpecialDates.setItems(list);
        });
    }
    
    /**
     * Builds the weekly opening hours grid dynamically
     * based on the current configuration.
     */
    private void buildWeeklyGrid() {
        gridDays.getChildren().clear();
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        
        int row = 0;
        for (String day : days) {
            Label lbl = new Label(day + ":");
            String[] times = currentConfig.getRegularHours().getOrDefault(day, new String[]{"",""});
            
            TextField txtOpen = new TextField(times[0]); txtOpen.setPromptText("HH:mm"); txtOpen.setPrefWidth(60);
            TextField txtClose = new TextField(times[1]); txtClose.setPromptText("HH:mm"); txtClose.setPrefWidth(60);
            
            gridDays.add(lbl, 0, row);
            gridDays.add(txtOpen, 1, row);
            gridDays.add(new Label("-"), 2, row);
            gridDays.add(txtClose, 3, row);
            
            dayFields.put(day, new TextField[]{txtOpen, txtClose});
            row++;
        }
    }

    /**
     * Adds a special opening date to the table.
     *
     * @param event the action event triggered by the add button
     */
    @FXML
    void handleAddSpecialDate(ActionEvent event) {
        LocalDate date = datePicker.getValue();
        String open = txtSpecialOpen.getText().trim();
        String close = txtSpecialClose.getText().trim();

        if (date == null || open.isEmpty() || close.isEmpty()) {
            SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields");
            return;
        }
        
        // Add to currentConfig just to create a Map.Entry, then add to Table List
        Map<LocalDate, String[]> tempMap = new HashMap<>();
        tempMap.put(date, new String[]{open, close});
        
        // Add the new entry to the table view
        tblSpecialDates.getItems().add(tempMap.entrySet().iterator().next());
        
        datePicker.setValue(null);
        txtSpecialOpen.clear();
        txtSpecialClose.clear();
    }

    /**
     * Saves all configuration changes and sends them to the server.
     *
     * @param event the action event triggered by the save button
     */
    @FXML
    void saveSettings(ActionEvent event) {
        // Save Regular Hours
        for (Map.Entry<String, TextField[]> entry : dayFields.entrySet()) {
            String open = entry.getValue()[0].getText().trim();
            String close = entry.getValue()[1].getText().trim();
            currentConfig.getRegularHours().put(entry.getKey(), new String[]{open, close});
        }

        // Rebuild special hours map from table data 
        currentConfig.getSpecialHours().clear();
        for (Map.Entry<LocalDate, String[]> entry : tblSpecialDates.getItems()) {
            currentConfig.getSpecialHours().put(entry.getKey(), entry.getValue());
        }

        // Send to Server
        ClientUI.chat.accept(new BistroMessage(Action.UPDATE_RESTAURANT_CONFIG, currentConfig));
        
        SceneLoader.showAlert(Alert.AlertType.INFORMATION, "Success", "Settings sent to server!");
        closeWindow(event);
    }

    /**
     * Closes the system settings window.
     *
     * @param event the action event triggered by the close button
     */
    @FXML
    void closeWindow(ActionEvent event) {
        Stage stage = (Stage) gridDays.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Returns the active instance of this controller.
     *
     * @return the singleton SystemSettingsController instance
     */
    public static SystemSettingsController getInstance() {
        return instance;
    }
}