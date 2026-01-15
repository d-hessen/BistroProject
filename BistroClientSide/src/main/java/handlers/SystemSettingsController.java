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

public class SystemSettingsController implements Initializable {

    @FXML private GridPane gridDays;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtSpecialOpen, txtSpecialClose;   
    @FXML private TableView<Map.Entry<LocalDate, String[]>> tblSpecialDates;
    @FXML private TableColumn<Map.Entry<LocalDate, String[]>, String> colDate, colOpen, colClose;   
    @FXML private TableColumn<Map.Entry<LocalDate, String[]>, Void> colDelete;

    private Map<String, TextField[]> dayFields = new HashMap<>();
    private RestaurantConfig currentConfig;
    private static SystemSettingsController instance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        setupTable();
        ClientUI.chat.accept(new BistroMessage(Action.GET_RESTAURANT_CONFIG, null));
    }

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
    
    public void setConfigData(RestaurantConfig config) {
        Platform.runLater(() -> {
            this.currentConfig = config;
            buildWeeklyGrid();
            
            // ObservableList so we can delete from it 
            ObservableList<Map.Entry<LocalDate, String[]>> list = FXCollections.observableArrayList(config.getSpecialHours().entrySet());
            tblSpecialDates.setItems(list);
        });
    }
    
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

    @FXML
    void closeWindow(ActionEvent event) {
        Stage stage = (Stage) gridDays.getScene().getWindow();
        stage.close();
    }
    
    public static SystemSettingsController getInstance() {
        return instance;
    }
}