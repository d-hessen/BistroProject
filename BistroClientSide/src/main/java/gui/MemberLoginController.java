package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class MemberLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        if ("member".equals(usernameField.getText())
                && "1234".equals(passwordField.getText())) {

            try {
                Parent root = FXMLLoader.load(
                        getClass().getResource("/gui/MemberGUI.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Member");
                stage.setScene(new Scene(root));
                stage.show();
                ((Node)event.getSource()).getScene().getWindow().hide();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            new Alert(Alert.AlertType.ERROR, "Wrong credentials").showAndWait();
        }
    }
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/gui/MainPage.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Main Page");
            stage.setScene(new Scene(root));
            stage.show();

            ((Node) event.getSource()).getScene().getWindow().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
