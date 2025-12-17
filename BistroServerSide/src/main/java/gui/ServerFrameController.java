package gui;

import java.net.URL;
import java.util.ResourceBundle;

import server.BistroServer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ocsf.server.ConnectionToClient;

public class ServerFrameController implements Initializable {
	
	@FXML
	private Button btnStart;
	@FXML
	private Button btnStop;
	@FXML
	private Label statusLabel;
	@FXML
	private Label title;
	@FXML
	private Label port;
	@FXML
	private Label connClientIPHost;
	@FXML
	private Label serverConLog;
	@FXML
	private TextField portField;
	@FXML
	private TableView<ClientInfo> clientsTable;
	@FXML
	private TableColumn<ClientInfo,String> colIp;
	@FXML
	private TableColumn<ClientInfo,String> colHost;
	@FXML
	private TableColumn<ClientInfo,String> colStatus;
	@FXML
	private TextArea consoleArea;
	
	private BistroServer server;
	private ObservableList<ClientInfo> clientList = FXCollections.observableArrayList();
	
	public void start(Stage primaryStage) throws Exception{
		Parent root = FXMLLoader.load(getClass().getResource("/gui/ServerFrame.fxml"));
		Scene scene = new Scene(root);
		primaryStage.setTitle("Server Configuration");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	//Function to write to consoleArea
	public void addToConsole(String message) {
        Platform.runLater(() -> {
            consoleArea.appendText(message + "\n");
        });
    }
	
	//Function for Start Server button
	public void onClickStart(ActionEvent event) throws Exception {
		String port = getport();
		
		if(port.trim().isEmpty()) {
			addToConsole("You must enter a port number");
		}
		else
		{
			int insertedPort = 5555;
	        try {
	            insertedPort = Integer.parseInt(port);
	        } catch (NumberFormatException e) {
	            addToConsole("Invalid port number");
	            return;
	        }
	        server = new BistroServer(insertedPort, this);

	        try {
	            server.listen(); // Start listening for clients
	            btnStart.setDisable(true);
	            btnStop.setDisable(false);
	            portField.setEditable(false);
	        } catch (Exception ex) {
	            addToConsole("ERROR - Could not listen for clients! - " +ex.getMessage());
	        }
		}
		
	}
	
	//Function for Stop Server button
	public void onClickStop(ActionEvent event) throws Exception {
		if (server != null) {
            try {
            	server.close(); // Stops listening and closes connections
                btnStart.setDisable(false);
                btnStop.setDisable(true);
                portField.setEditable(true);
                consoleArea.setText("");
                
            } catch (Exception e) {
                addToConsole("Error stopping server: " + e.getMessage());
            }
        }
	}
	
	//Function to change server label green/red	
	public void serverStatusChanged(boolean isConnected) {
        Platform.runLater(() -> {
            if (isConnected) {
                statusLabel.setText("Status: Connected");
                statusLabel.setTextFill(Color.GREEN);
            } else {
                statusLabel.setText("Status: Disconnected");
                statusLabel.setTextFill(Color.RED);
            }
        });
    }
	
	//Class for TableView
    public static class ClientInfo {
        private String ip;
        private String host;
        private String status;
        private ConnectionToClient client;

        public ClientInfo(ConnectionToClient client,String ip, String host, String status) {
            this.ip = ip;
            this.host = host;
            this.status = status;
            this.client = client;
        }
        
        public ConnectionToClient getClient() {return client;}
        public String getIp() { return ip; }
        public String getHost() { return host; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
	
	//Function that will update clients
    public void updateClientList(ConnectionToClient client, String status) {
        Platform.runLater(() -> {

            if ("Connected".equals(status)) {
                // When a client connects: add or update row
                String ip   = client.getInetAddress().getHostAddress();
                String host = client.getInetAddress().getHostName();

                ClientInfo existing = null;
                for (ClientInfo c : clientList) {
                    if (c.getClient() == client) {
                        existing = c;
                        break;
                    }
                }

                if (existing == null) {
                    clientList.add(new ClientInfo(client, ip, host, "Connected")); 
                } else {
                    existing.setStatus("Connected");
                }
            } else {
                // When a client disconnects: remove that row
                clientList.removeIf(c -> c.getClient() == client);
            }
            //Tableview refresh
            clientsTable.refresh();
        });
    }
    // Initialize method is called automatically after FXML loading
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up Table Columns
        colIp.setCellValueFactory(new PropertyValueFactory<>("ip"));
        colHost.setCellValueFactory(new PropertyValueFactory<>("host"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        clientsTable.setItems(clientList);
    }
    
    //function to get port from textfield
  	private String getport() {
  		return portField.getText();			
  	}

}
