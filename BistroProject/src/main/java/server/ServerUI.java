package server;

import javafx.application.Application;
import javafx.stage.Stage;
import gui.ServerFrameController;

public class ServerUI extends Application {
    
    public static void main(String args[]) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ServerFrameController serverFrame = new ServerFrameController();
        serverFrame.start(primaryStage);
    }
}