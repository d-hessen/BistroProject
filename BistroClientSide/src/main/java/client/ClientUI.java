package client;
import javafx.application.Application;

import javafx.stage.Stage;
import common.Action;
import common.BistroMessage;
import handlers.MainPageController;
import handlers.ReservationFrameController;


public class ClientUI extends Application {
	public static ClientController chat; 

	public static void main(String[] args)
	{
	    String host = "localhost";

	    if (args.length > 0) {
	        host = args[0];
	    }

	    chat = new ClientController(host, 5555);
	    launch(args);
	}
	 
	@Override
	public void start(Stage primaryStage) throws Exception {
		 
		// TODO Auto-generated method stub
						  		
		//MainPageController mainPage = new MainPageController(); 
		ReservationFrameController rfc = new ReservationFrameController();
		
		
		rfc.start(primaryStage); 
		//mainPage.start(primaryStage);
	}
	
	@Override
	public void stop() throws Exception {
	    if (chat != null) {
	        chat.accept(new BistroMessage(Action.DISCONNECT,""));  
	    }

	    super.stop();
	}
	
	
}
