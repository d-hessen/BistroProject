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
		    launch(args);  
	   } // end main
	 
	@Override
	public void start(Stage primaryStage) throws Exception {
		 chat= new ClientController("localhost", 5555);
		// TODO Auto-generated method stub
						  		
		ReservationFrameController reservationFrame = new ReservationFrameController(); // create StudentFrame
		 
		reservationFrame.start(primaryStage);
	}
	
	
}
