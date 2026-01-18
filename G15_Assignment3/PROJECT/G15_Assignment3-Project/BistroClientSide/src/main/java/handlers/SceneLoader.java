package handlers;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * SceneLoader is a utility class responsible for scene and window navigation.
 * <p>
 * This class centralizes all JavaFX navigation logic, including:
 * <ul>
 *   <li>Loading and switching scenes</li>
 *   <li>Opening new windows and modal dialogs</li>
 *   <li>Closing windows</li>
 *   <li>Displaying alert and confirmation dialogs</li>
 * </ul>
 *
 * <p>
 * All methods are static and intended to be used by controllers
 * throughout the application.
 */
public class SceneLoader {
	
	/**
     * Loads a new scene into the current window.
     * <p>
     * Replaces the existing scene of the window that triggered the event.
     *
     * @param event    the action event that initiated the scene change
     * @param fxmlPath the path to the FXML file to load
     * @param title    the title of the window
     */
    public static void loadScene(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }
    
    /**
     * Opens a new non modal window with the specified scene.
     *
     * @param fxmlPath the path to the FXML file to load
     * @param title    the title of the new window
     */
    public static void openNewWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle(title);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Opens a modal window that blocks interaction with its owner.
     *
     * @param owner    the owner stage of the modal window
     * @param fxmlPath the path to the FXML file to load
     * @param title    the title of the modal window
     */
    public static void openModalWindow(Stage owner, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Closes the window that triggered the given action event.
     *
     * @param event the action event that initiated the close request
     */
    public static void closeWindow(ActionEvent event) {
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.close();
    }
    
    /**
     * Displays an alert dialog to the user.
     *
     * @param type    the alert type (information, warning, error, etc.)
     * @param title   the title of the alert window
     * @param content the content message of the alert
     */
    public static void showAlert(Alert.AlertType type, String title, String content) {
    		Alert alert = new Alert(type);
    		alert.setTitle(title);
    		alert.setHeaderText(null);
    		alert.setContentText(content);
    		alert.showAndWait();
    }
    
    /**
     * Displays a confirmation alert dialog and returns the user's response.
     *
     * @param title   the title of the confirmation dialog
     * @param content the message displayed in the dialog
     * @return {@code true} if the user clicked OK, {@code false} otherwise
     */
    public static boolean showConfirmationAlert(String title, String content){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
        	return true;
        }
        return false;
    }
    
    /**
     * Reloads a scene into an existing stage.
     *
     * @param stage    the stage to reload the scene into
     * @param fxmlPath the path to the FXML file to load
     * @param title    the title of the stage
     */
    public static void loadSceneAgain(Stage stage, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Switches the current screen and allows controller initialization logic.
     * <p>
     * This method hides the current window, loads a new scene,
     * applies custom setup logic to the controller, and displays
     * the new window.
     *
     * @param <T>         the type of the controller
     * @param event       the event that initiated the screen switch
     * @param fxmlPath    the path to the FXML file to load
     * @param title       the title of the new window
     * @param setupAction optional logic to run on the loaded controller
     */
    public static <T> void switchScreen(Event event, String fxmlPath, String title, Consumer<T> setupAction) {
        try {
            ((Node) event.getSource()).getScene().getWindow().hide();
            FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            //Get the Controller and run the setup logic
            T controller = loader.getController();
            if (setupAction != null) {
                setupAction.accept(controller);
            }
            //Show the new window
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error (e.g., show an alert)
        }
    }
}