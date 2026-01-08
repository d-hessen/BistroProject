package handlers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Member;
import dataLayer.Table;

public class StaffDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private TabPane mainTabPane;
    
    //Check-In TAB
    @FXML private TextField memberCodeField; // Renamed to match FXML id
    @FXML private Label checkInStatusLabel;

    //Tables TAB
    @FXML private GridPane tablesGrid;
    @FXML private TextField capacityField;
    @FXML private TextField tableNumberField;
    @FXML private ComboBox<String> statusComboBox;

    //Register member TAB
    @FXML private TextField regFullName, regPhone, regEmail;
    @FXML private PasswordField PasswordField;
    @FXML private TextField PasswordFieldVisible;
    @FXML private CheckBox showPasswordTick;

    // Reports & Admin Tabs TAB
    @FXML private Tab managerReportsTab;
    @FXML private Tab managerAdminTab;
    @FXML private ComboBox<String> reportTypeCombo, monthCombo;
    @FXML private TableView<?> reportsTable, logsTable; // Generic for now

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private ArrayList<Table> tables;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BistroClient.staffDashControllerInstance = this;
        tables = null;

        if (BistroClient.staffInstance != null) {
            welcomeLabel.setText("Welcome, " + BistroClient.staffInstance.getFullName());
            
            // Role Check: Hide Manager Tabs if worker
            if (!"manager".equalsIgnoreCase(BistroClient.staffInstance.getRole())) { 
                mainTabPane.getTabs().remove(managerReportsTab);
            }
        }
        setupComboBoxes();
        refreshTables();
    }

    private void setupComboBoxes() {
    	statusComboBox.getItems().addAll("Available", "Occupied", "Reserved", "Deleted");
        if(reportTypeCombo != null) 
            reportTypeCombo.setItems(FXCollections.observableArrayList("Reservations", "Waiting Lists"));
        if(monthCombo != null)
            monthCombo.setItems(FXCollections.observableArrayList("January", "February", "March", "April"));
    }

    // --- TABLE MANAGEMENT ---
    
    private void refreshTables() {
        ClientUI.chat.accept(new BistroMessage(Action.GET_ALL_TABLES, null));
    }

    // Called by BistroClient
    public void updateTableGrid(ArrayList<Table> tables) {
        Platform.runLater(() -> {
        	this.tables = tables;
            tablesGrid.getChildren().clear(); 
            int col = 0;
            int row = 0;
            
            for (Table t : tables) {
                if(!t.isActive()) continue; // Skip deleted tables
                
                ToggleButton btn = new ToggleButton("Table " + t.getTableNumber() + "\nCapacity:(" + t.getTableCapacity() + " ppl)");
                btn.setPrefSize(100, 80);
                
                if (t.isOccupied()) {
                    btn.setStyle("-fx-background-color: #ff6666; -fx-text-fill: white;"); // Red
                    btn.setSelected(true);
                } else {
                    btn.setStyle("-fx-background-color: lightgreen; -fx-text-fill: black;"); // Green
                    btn.setSelected(false);
                }
                
                btn.setOnAction(e -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/TableManagement.fxml"));
                        Parent root = loader.load();
                        
                        TableManagementController controller = loader.getController();
                        controller.setTableDetails(t);
                        
                       Stage stage = new Stage();
                       stage.setScene(new Scene(root));
                       stage.setTitle("Manage Table " + t.getTableNumber());
                       stage.show();
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                
                tablesGrid.add(btn, col, row);
                col++;
                if (col > 2) { 
                	col = 0;
                	row++; 
                }
            }
        });
    }
    
    @FXML
    void handleAddTable(ActionEvent event) {
    	if(tableNumberField.getText().trim().isEmpty() || capacityField.getText().trim().isEmpty()) {
    		SceneLoader.showAlert(Alert.AlertType.ERROR, "Table Management", "You must enter table number and it's capacity!");
			return;
    	}
    	Integer tableNum = Integer.valueOf(tableNumberField.getText().trim());
    	for (Table table : this.tables) {
			if(table.getTableNumber().equals(tableNum)) {
				SceneLoader.showAlert(Alert.AlertType.ERROR, "Table Management", "Table: "+tableNum+" already exists!");
				return;
			}
		}
    	Integer capacity = Integer.valueOf(capacityField.getText().trim());
    	if (capacity < 2) {
			SceneLoader.showAlert(Alert.AlertType.ERROR, "Table Management", "Table capacity must be bigger than 2");
			return;
    	}
    	
    	if(statusComboBox.getValue() == null) {
    		SceneLoader.showAlert(Alert.AlertType.ERROR, "Table Management", "You must choose table status");
			return;
    	}
    	
    	Table toCreate = new Table(tableNum,capacity,false);
    	if (statusComboBox.getValue().equals("Available")) {
    		toCreate.setActive(true);
    		toCreate.setOccupied(false);
    	}
    	ClientUI.chat.accept(new BistroMessage(Action.ADD_TABLE,toCreate));
    	if(BistroClient.operationSuccess) {
    		BistroClient.operationSuccess = false;
    		BistroClient.tables = new ArrayList<>();
        	ClientUI.chat.accept(new BistroMessage(Action.GET_ALL_TABLES,null));
        	tableNumberField.clear();
        	capacityField.clear();
    	}
    }

    // --- CHECK IN ---

    @FXML
    void handleMemberCheckIn(ActionEvent event) {
        String code = memberCodeField.getText(); 
        if (code == null || code.trim().isEmpty()) {
            checkInStatusLabel.setText("Please enter member code.");
            checkInStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        checkInStatusLabel.setText("Verifying...");
        checkInStatusLabel.setStyle("-fx-text-fill: black;");
        
        // Send request
        ClientUI.chat.accept(new BistroMessage(Action.VERIFY_MEMBER_ARRIVAL, code.trim()));
    }
    
    // Add this to update UI based on response
    public void updateCheckInStatus(boolean success, String message) {
        Platform.runLater(() -> {
            checkInStatusLabel.setText(message);
            if (success) {
                checkInStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                memberCodeField.clear();
                // Refresh tables to show the new red table
                ClientUI.chat.accept(new BistroMessage(Action.GET_ALL_TABLES, null)); 
            } else {
                checkInStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });
    }
    // --- MEMBER REGISTRATION ---

    @FXML
    void handleRegisterMember(ActionEvent event) {
        String name = regFullName.getText().trim();
        String phone = regPhone.getText().trim();
        String email = regEmail.getText().trim();
        String pass = showPasswordTick.isSelected() ? PasswordFieldVisible.getText() : PasswordField.getText();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty()) {
        	SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
            return;
        }
        
        // Validate Phone Number: Must contain 9-10 digits only
        if (!phone.matches("\\d{9,10}")) {
        	SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Phone number must contain numbers only.");
            return;
        }

        // Validate Email: Must follow standard email format
        if (!Pattern.compile(EMAIL_REGEX).matcher(email).matches()) {
        	SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Invalid email format.");
            return;
        }

        // Validate Full Name: Must contain at least two words
        String[] nameParts = name.split("\\s+");
        if (nameParts.length < 2) {
        	SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Full name must contain at least two words.");
            return;
        }

        Member memberToCreate = new Member(name, phone, email, pass);
        memberToCreate.setCardCode("CARD-" + (int)(Math.random() * 9000 + 1000)); //TODO: Move logic from controller to server (We must check if code is already exists)
        
        ClientUI.chat.accept(new BistroMessage(Action.CREATE_MEMBER, memberToCreate));
    }
    //Method when member created or there's an error
    public void memberCreated(boolean isCreated, String message) {
        Platform.runLater(() -> {
            if(isCreated) {
            	SceneLoader.showAlert(Alert.AlertType.INFORMATION, "Success", "Member Registered!");
                regFullName.clear(); regPhone.clear(); regEmail.clear(); PasswordField.clear();
            } else {
                SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Registration Failed: " + message);
            }
        });
    }

    // --- NAVIGATION ---
    @FXML void handleNewReservation(ActionEvent event) { SceneLoader.openNewWindow("/gui/MakeReservation.fxml", "New Reservation"); }
    @FXML void handleFindReservation(ActionEvent event) { SceneLoader.openNewWindow("/gui/ReservationFrame.fxml", "Find Reservation"); }
    @FXML void handleInsertParty(ActionEvent event) { SceneLoader.openNewWindow("/gui/VisitNow.fxml", "Waiting List"); }
    @FXML void handleWaitingList(ActionEvent event) { SceneLoader.openNewWindow("/gui/ClientWaiting.fxml", "Waiting List View"); } // Check FXML name
    @FXML void handleCustomerArrival(ActionEvent event) { SceneLoader.openNewWindow("/gui/VisitIdentification.fxml", "Visit Identification"); }
    @FXML void handleCurrentVisits(ActionEvent event) { SceneLoader.openNewWindow("/gui/ViewVisits.fxml", "Current Dinning Sessions");}
    
    @FXML
    void handleLogout(ActionEvent event) {
        BistroClient.staffInstance = null;
        BistroClient.staffDashControllerInstance = null;
        BistroClient.tables = new ArrayList<>();
        ClientUI.chat.accept(new BistroMessage(Action.DISCONNECT, null));
        SceneLoader.loadScene(event, "/gui/StaffLoginGUI.fxml", "Staff Login");
    }

    @FXML
    private void togglePasswordVisible(ActionEvent event) {
        if (showPasswordTick.isSelected()) {
            PasswordFieldVisible.setText(PasswordField.getText());
            PasswordFieldVisible.setVisible(true); PasswordFieldVisible.setManaged(true);
            PasswordField.setVisible(false); PasswordField.setManaged(false);
        } else {
            PasswordField.setText(PasswordFieldVisible.getText());
            PasswordField.setVisible(true); PasswordField.setManaged(true);
            PasswordFieldVisible.setVisible(false); PasswordFieldVisible.setManaged(false);
        }
    }

    @FXML void openTableInfo(ActionEvent event) {} // Legacy support if needed
    @FXML void handleGenerateReport(ActionEvent event) {} // Stub
}