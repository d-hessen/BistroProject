package client;
import javafx.application.Application;

import javafx.stage.Stage;
import common.Action;
import common.BistroMessage;
import handlers.MainPageController;
import handlers.ReservationFrameController;

/**
 * ClientUI is the main JavaFX application entry point for the client side.
 * <p>
 * This class is responsible for:
 * <ul>
 *   <li>Launching the JavaFX application</li>
 *   <li>Initializing the {@link ClientController} and client-server connection</li>
 *   <li>Starting the initial UI screen</li>
 *   <li>Gracefully disconnecting from the server on application shutdown</li>
 * </ul>
 * <p>
 * The class extends {@link Application} as required by JavaFX applications.
 */
public class ClientUI extends Application {
	
    /**
     * Static reference to the client controller used for communication
     * with the server throughout the application lifecycle.
     */
	public static ClientController chat; 

    /**
     * Application entry point.
     * <p>
     * Initializes the client controller and launches the JavaFX application.
     *
     * @param args optional command-line arguments (first argument may define host)
     */
	public static void main(String[] args)
	{
	    String host = "localhost";

	    if (args.length > 0) {
	        host = args[0];
	    }

	    chat = new ClientController(host, 5555);
	    launch(args);
	}
	
	/**
     * Starts the JavaFX application.
     * <p>
     * This method initializes and displays the first UI screen
     * (currently the main page).
     *
     * @param primaryStage the primary stage for this application
     * @throws Exception if an error occurs during UI initialization
     */
	@Override
	public void start(Stage primaryStage) throws Exception {
		 						  		
		MainPageController mainPage = new MainPageController(); 		
		mainPage.start(primaryStage);
	}
	
    /**
     * Called when the application is about to stop.
     * <p>
     * Sends a disconnect message to the server to ensure
     * a graceful client shutdown.
     *
     * @throws Exception if an error occurs during shutdown
     */
	@Override
	public void stop() throws Exception {
	    if (chat != null) {
	        chat.accept(new BistroMessage(Action.DISCONNECT,""));  
	    }

	    super.stop();
	}
	
	
}
