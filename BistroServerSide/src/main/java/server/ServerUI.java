package server;

import domainLogic.ServerFrameController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point for the Bistro server graphical user interface.
 * <p>
 * This class launches the JavaFX application and initializes
 * the server management GUI.
 */
public class ServerUI extends Application {
    
	/**
     * Main entry point of the server application.
     *
     * @param args command line arguments
     * @throws Exception if application launch fails
     */
    public static void main(String args[]) throws Exception {
        launch(args);
    }

    /**
     * Initializes and displays the server GUI.
     *
     * @param primaryStage the primary JavaFX stage
     * @throws Exception if GUI initialization fails
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        ServerFrameController serverFrame = new ServerFrameController();
        serverFrame.start(primaryStage);
    }
}