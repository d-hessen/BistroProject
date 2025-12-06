package server;

import javafx.application.Application;
import javafx.stage.Stage;


import java.util.Vector;

import gui.ServerFrameController;
import server.BistroServer;
//
public class ServerUI extends Application {
	final public static int DEFAULT_PORT = 5555;

	public static void main( String args[] ) throws Exception
	   {   
		 launch(args);
	  } // end main
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub				  		
		ServerFrameController serverFrame = new ServerFrameController(); // create StudentFrame
		 
		serverFrame.start(primaryStage);
	}
	
	public static void runServer(String p)
	{
		 int port = 0; //Port to listen on

	        try
	        {
	        	port = Integer.parseInt(p); //Set port to 5555
	          
	        }
	        catch(Throwable t)
	        {
	        	System.out.println("ERROR - Could not connect!");
	        }
	    	
	        BistroServer sv = new BistroServer(port);
	        
	        try 
	        {
	          sv.listen(); //Start listening for connections
	        } 
	        catch (Exception ex) 
	        {
	          System.out.println("ERROR - Could not listen for clients!");
	        }
	}
	

}
