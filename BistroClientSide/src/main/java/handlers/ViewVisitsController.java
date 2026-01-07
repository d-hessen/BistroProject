package handlers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Visit;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ViewVisitsController implements Initializable {

    @FXML private TableView<Visit> visitsTable;
    @FXML private TableColumn<Visit, String> dateColumn;
    @FXML private TableColumn<Visit, String> timeColumn;
    @FXML private TableColumn<Visit, Integer> dinersColumn;
    @FXML private TableColumn<Visit, String> statusColumn;
    @FXML private Label welcomeLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadData();
        if(BistroClient.staffInstance != null) {
        	welcomeLabel.setText("Active Dinning Sessions");
        } else {
        	welcomeLabel.setText("My Visits History");
        }
    }

    private void setupTableColumns() {
        //Get date from the Visit startTime 
        dateColumn.setCellValueFactory(cellData -> {
            Visit visit = cellData.getValue();
            if (visit.getStartTime() != null) {
                return new SimpleStringProperty(visit.getStartTime().getDate());
            }
            return new SimpleStringProperty("N/A");
        });

        //Get time from the Visit startTime
        timeColumn.setCellValueFactory(cellData -> {
            Visit visit = cellData.getValue();
            if (visit.getStartTime() != null) {
                return new SimpleStringProperty(visit.getStartTime().getTime());
            }
            return new SimpleStringProperty("N/A");
        });

        //Get number of guests from the Reservation object if it exists
        dinersColumn.setCellValueFactory(cellData -> {
            Visit visit = cellData.getValue();
            if (visit.getReservation() != null) {
                return new SimpleIntegerProperty(visit.getReservation().getNumberOfGuests()).asObject();
            } else {
                return new SimpleIntegerProperty(visit.getPartySize()).asObject(); 
            }
        });

        //Convert boolean isActive to String
        statusColumn.setCellValueFactory(cellData -> {
            boolean active = cellData.getValue().isActive();
            return new SimpleStringProperty(active ? "Active" : "Not Active");
        });
    }

    private void loadData() {
        ArrayList<Visit> visits = new ArrayList<>();

        if (BistroClient.memberInstance != null) {
            //Request Member's History
            System.out.println("Requesting visits history for member: " + BistroClient.memberInstance.getFullName());
            ClientUI.chat.accept(new BistroMessage(Action.GET_MEMBER_VISITS, BistroClient.memberInstance.getMemberId()));
            //Get the list from the client static variable
            if (BistroClient.visitsList != null) {
                visits.addAll(BistroClient.visitsList);
            }

        } else if (BistroClient.staffInstance != null) {
            //Request Active Sessions
            System.out.println("Requesting active sessions for staff: " + BistroClient.staffInstance.getFullName());
            ClientUI.chat.accept(new BistroMessage(Action.GET_ACTIVE_VISITS, null));
            //Get the list from the client static variable
            if (BistroClient.visitsList != null) {
                visits.addAll(BistroClient.visitsList);
            }
        }
        // Set the data to the table
        ObservableList<Visit> observableVisits = FXCollections.observableArrayList(visits);
        visitsTable.setItems(observableVisits);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        // Navigate back based on who is logged in
        if (BistroClient.staffInstance != null) {
            SceneLoader.closeWindow(event);
        } else {
            SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
        }
    }
}