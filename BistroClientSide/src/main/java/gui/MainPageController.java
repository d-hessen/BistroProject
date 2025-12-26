package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class MainPageController {

    @FXML
    private void openClientOptions(ActionEvent event) {
        openUI(event, "ClientLoginOptions.fxml", "Client Options");
    }

    @FXML
    private void openStaffOptions(ActionEvent event) {
        openUI(event, "StaffLoginOptions.fxml", "Staff Options");
    }

    private void openUI(ActionEvent event, String fxmlFile, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/" + fxmlFile));
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
            ((Node) event.getSource()).getScene().getWindow().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void start(Stage primaryStage) throws Exception {    
        Parent root = FXMLLoader.load(getClass().getResource("/gui/MainPage.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("Main Page");
        primaryStage.setScene(scene);
        primaryStage.show();           
    }
}