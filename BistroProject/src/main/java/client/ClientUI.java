package client;
import javafx.application.Application;

import javafx.stage.Stage;

import java.util.Vector;
import client.ClientController;
import gui.ReservationFrameController;

public class ClientUI extends Application {
	public static ClientController chat; 

	public static void main( String args[] ) throws Exception
	   { 
			chat = new ClientController("localhost", 5555);
		    launch(args);  
	   } // end main
	 
	@Override
	public void start(Stage primaryStage) throws Exception {
		 
		// TODO Auto-generated method stub
						  		
		ReservationFrameController reservationFrame = new ReservationFrameController(); // create StudentFrame
		 
		reservationFrame.start(primaryStage);
	}
	
	@Override
	public void stop() throws Exception {
	    if (chat != null) {
	        chat.accept("#disconnect");   
	    }

	    super.stop();
	}
	
	
}
