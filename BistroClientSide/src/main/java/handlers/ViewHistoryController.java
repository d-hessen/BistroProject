package handlers;

import java.net.URL;
import java.util.ResourceBundle;
import client.BistroClient;
import dataLayer.Reservation;
import dataLayer.Visit;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ViewHistoryController implements Initializable {
    @FXML private TableView<Reservation> resTable;
    @FXML private TableColumn<Reservation, String> resDateCol, resTimeCol, resStatusCol, resGuestsCol;
    
    @FXML private TableView<Visit> visitsTable;
    @FXML private TableColumn<Visit, String> visitDateCol, visitTimeCol, visitTableCol, visitTotalCol;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
    	// Define orders cols
        resDateCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getReservationDate().getDate()));
        resTimeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getReservationDate().getTime()));
        resGuestsCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getNumberOfGuests())));
        resStatusCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().toString()));
        
        // Define visits cols
        visitDateCol.setCellValueFactory(cell -> cell.getValue().getStartTime() != null ? new SimpleStringProperty(cell.getValue().getStartTime().getDate()) : new SimpleStringProperty("-"));
        visitTimeCol.setCellValueFactory(cell -> cell.getValue().getStartTime() != null ? new SimpleStringProperty(cell.getValue().getStartTime().getTime()) : new SimpleStringProperty("-"));
        visitTableCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getTable().getTableNumber())));
        visitTotalCol.setCellValueFactory(cell -> cell.getValue().getBillOfVisit() != null ? new SimpleStringProperty(String.valueOf(cell.getValue().getBillOfVisit().getFinalAmount())) : new SimpleStringProperty("0.0"));
    }

    private void loadData() {
        if (BistroClient.historyReservations != null) {
            resTable.setItems(FXCollections.observableArrayList(BistroClient.historyReservations));
        }
        
        if (BistroClient.historyVisits != null) {
            visitsTable.setItems(FXCollections.observableArrayList(BistroClient.historyVisits));
        }
    }
}