package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;

public class ManagerGUIController {

    @FXML
    private ComboBox<MonthItem> monthComboBox;

    @FXML
    public void initialize() {
        monthComboBox.setItems(loadMonths());
    }

    private ObservableList<MonthItem> loadMonths() {
        return FXCollections.observableArrayList(
                new MonthItem(1, "January"),
                new MonthItem(2, "February"),
                new MonthItem(3, "March"),
                new MonthItem(4, "April"),
                new MonthItem(5, "May"),
                new MonthItem(6, "June"),
                new MonthItem(7, "July"),
                new MonthItem(8, "August"),
                new MonthItem(9, "September"),
                new MonthItem(10, "October"),
                new MonthItem(11, "November"),
                new MonthItem(12, "December")
        );
    }

    @FXML
    private void viewActivityReports() {

        MonthItem selectedMonth = monthComboBox.getValue();

        if (selectedMonth == null) {
            showError("Please select a month.");
            return;
        }

        int monthNumber = selectedMonth.getMonthNumber();

        System.out.println("Selected month number: " + monthNumber);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
