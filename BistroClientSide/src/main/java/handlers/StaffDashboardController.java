package handlers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.Year;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Member;
import dataLayer.Table;
import dataLayer.Visit;
import javafx.util.Callback;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

/**
 * Controller for the Staff Dashboard screen.
 * <p>
 * This controller manages all staff side operations, including:
 * <ul>
 *   <li>Table management and live table status</li>
 *   <li>Member check in and waiting list handling</li>
 *   <li>Member registration</li>
 *   <li>Administration features for managers</li>
 *   <li>Navigation to operational and management screens</li>
 * </ul>
 *
 * <p>
 * The controller periodically refreshes table data and updates the UI
 * accordingly.
 */
public class StaffDashboardController implements Initializable {

	/** Welcome label displaying staff member name */
    @FXML private Label welcomeLabel;

    /** Main tab pane of the dashboard */
    @FXML private TabPane mainTabPane;

    // -------- CHECK-IN TAB --------
    /** Field for entering member or reservation code */
    @FXML private TextField memberCodeField;

    /** Label displaying check-in status messages */
    @FXML private Label checkInStatusLabel;

    // -------- TABLES TAB --------
    /** Grid displaying restaurant tables */
    @FXML private GridPane tablesGrid;

    /** Field for entering table capacity */
    @FXML private TextField capacityField;

    /** Field for entering table number */
    @FXML private TextField tableNumberField;

    /** ComboBox for selecting table status */
    @FXML private ComboBox<String> statusComboBox;

    // -------- MEMBER REGISTRATION TAB --------
    /** Registration fields */
    @FXML private TextField regFullName, regPhone, regEmail;

    /** Password fields */
    @FXML private PasswordField PasswordField;
    @FXML private TextField PasswordFieldVisible;

    /** Checkbox to toggle password visibility */
    @FXML private CheckBox showPasswordTick;

    // -------- REPORTS TAB --------
    @FXML private Tab managerTab;
    @FXML private ComboBox<Integer> reportYearCombo;
    @FXML private ComboBox<String> reportMonthCombo;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private Button btnViewReport;

    // -------- MEMBERS ADMINISTRATION TABLE --------
    @FXML private TableView<Member> membersTable;
    @FXML private TableColumn<Member, String> colMemberName;
    @FXML private TableColumn<Member, String> colMemberPhone;
    @FXML private TableColumn<Member, String> colMemberEmail;
    @FXML private TableColumn<Member, Void> historyCol;

    /** Email validation regex */
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    /** Cached list of restaurant tables */
    private ArrayList<Table> tables;

    /** Scheduled executor for periodic table refresh */
    private ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(1);
    
    /**
     * Initializes the staff dashboard controller.
     * <p>
     * Sets role based UI visibility, initializes combo boxes,
     * configures the members table, and starts periodic table refresh.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BistroClient.staffDashControllerInstance = this;
        tables = null;

        if (BistroClient.staffInstance != null) {
            welcomeLabel.setText("Welcome, " + BistroClient.staffInstance.getFullName());
            
            // Hide manager only tabs for non manager roles
            if (!"manager".equalsIgnoreCase(BistroClient.staffInstance.getRole())) { 
                mainTabPane.getTabs().remove(managerTab);
            }
        }
        setupComboBoxes();
        
        // Initialize Members Table Columns
        setupMembersTable();

        Runnable refreshTables = new Runnable() {
            public void run() {
                BistroClient.tables = new ArrayList<>();
                getAllTables();
            }
        };
        executor.scheduleAtFixedRate(refreshTables, 0, 3, TimeUnit.SECONDS);
    }

    /**
     * Initializes values for combo boxes used in the dashboard.
     */
    private void setupComboBoxes() {
    	if (reportYearCombo != null) {
            int currentYear = Year.now().getValue();
            reportYearCombo.setItems(FXCollections.observableArrayList(
                currentYear, currentYear - 1, currentYear - 2, currentYear - 3
            ));
            reportYearCombo.getSelectionModel().selectFirst();
        }

        if (reportMonthCombo != null) {
            reportMonthCombo.setItems(FXCollections.observableArrayList(
                "01 - January", "02 - February", "03 - March", "04 - April", "05 - May", "06 - June",
                "07 - July", "08 - August", "09 - September", "10 - October", "11 - November", "12 - December"
            ));
        }

        if (reportTypeCombo != null) {
            reportTypeCombo.setItems(FXCollections.observableArrayList(
                "Time & Punctuality Report", 
                "Member Activity Report"
            ));
        }
    }

    /**
     * Configures the members administration table columns.
     */
    private void setupMembersTable() {
        // Combine First and Last name into one column
        colMemberName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFullName()));
        
        // Map phone and email properties
        colMemberPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colMemberEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        addButtonToTable();
    }
    
    /**
     * Adds a "History" button to each row in the members table.
     */
    private void addButtonToTable() {
        Callback<TableColumn<Member, Void>, TableCell<Member, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Member, Void> call(final TableColumn<Member, Void> param) {
                return new TableCell<>() {

                    private final Button btn = new Button("History");

                    {
                        btn.setStyle("-fx-font-size: 11px;");
                        btn.setPrefWidth(80);

                        // Action when pressed
                        btn.setOnAction(event -> {
                        	// Retrieve the object from the current row
                            Member data = getTableView().getItems().get(getIndex());
                            openMemberHistory(data.getMemberId());
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                        	// Centers the button in the cell
                            HBox pane = new HBox(btn);
                            pane.setAlignment(Pos.CENTER);
                            setGraphic(pane);
                        }
                    }
                };
            }
        };

        historyCol.setCellFactory(cellFactory);
    }

    /**
     * Opens the member history window for the given member ID.
     *
     * @param memberId the member identifier
     */
    private void openMemberHistory(int memberId) {
        try {
        	// Reset data
            BistroClient.memberFoundStatus = null;
            BistroClient.historyReservations = null;
            BistroClient.historyVisits = null;

            // Sending a request
            ClientUI.chat.accept(new BistroMessage(Action.GET_MEMBER_HISTORY, memberId));

            // Wait for response
            int retries = 0;
            while (BistroClient.memberFoundStatus == null && retries < 50) {
                try { 
                	Thread.sleep(100); 
                } catch (InterruptedException e) { }
                retries++;
            }

            if (BistroClient.memberFoundStatus == null) {
                SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Server timeout.");
                return;
            }
            
            if (BistroClient.memberFoundStatus == false) {
                 SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Data not found.");
                 return;
            }

            // Opening the window
            SceneLoader.openNewWindow("/gui/ViewHistory.fxml", "History - Member ID: " + memberId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // ----------------------------------------------------------------
    // 					TABLE MANAGEMENT
    // ----------------------------------------------------------------

    /**
     * Sends a request to retrieve all tables from the server.
     */
    private void getAllTables() {
        ClientUI.chat.accept(new BistroMessage(Action.GET_ALL_TABLES, null));
    }

    /**
     * Updates the table grid layout based on server data.
     *
     * @param tables the list of tables to display
     */
    public void updateTableGrid(ArrayList<Table> tables) {
        Platform.runLater(() -> {
            this.tables = tables;
            tablesGrid.getChildren().clear(); 
            int col = 0;
            int row = 0;
            
            for (Table table : tables) {
                if(!table.isActive()) continue; // Skip deleted tables
                
                ToggleButton btn = new ToggleButton(table.getTableNumber().toString());
                btn.setPrefSize(70, 70);
                
                setButtonStatus(table,btn);
                
                btn.setOnAction(e -> {
                    try {
                       setButtonStatus(table,btn);
                       FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/TableManagement.fxml"));
                       Parent root = loader.load();
                       
                       TableManagementController controller = loader.getController();
                       controller.setTableDetails(table);
                       
                       Stage stage = new Stage();
                       stage.setScene(new Scene(root));
                       stage.setTitle("Manage Table " + table.getTableNumber());
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
    
    /**
     * Updates the visual state of a table button.
     *
     * @param table  the table entity
     * @param button the corresponding toggle button
     */
    private void setButtonStatus(Table table, ToggleButton button) {
        if (table.isOccupied()) {
            button.setSelected(true);
        } else {
            button.setSelected(false);
        }
    }
    
    /**
     * Handles adding a new table to the system.
     * <p>
     * Validates table number, capacity, and status,
     * then sends a request to the server to create the table.
     *
     * @param event the action event triggered by the "Add Table" button
     */
    @FXML
    void handleAddTable(ActionEvent event) {
    	if(tableNumberField.getText().trim().isEmpty()) {
            SceneLoader.showAlert(Alert.AlertType.ERROR, "Table Management", "You must enter table number");
            return;
        }
    	
    	if(capacityField.getText().trim().isEmpty()) {
            SceneLoader.showAlert(Alert.AlertType.ERROR, "Table Management", "You must enter table capacity");
            return;
        }
        
        if (!tableNumberField.getText().trim().matches("\\d+")) {
            SceneLoader.showAlert(Alert.AlertType.ERROR, "Table Management","Table number must contain digits only."
            );
            return;
        }

        if (!capacityField.getText().trim().matches("\\d+")) {
            SceneLoader.showAlert(Alert.AlertType.ERROR, "Table Management", "Table capacity must contain digits only."
            );
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
            handleRefreshTables(event);
                tableNumberField.clear();
                capacityField.clear();
        }
    }
    
    /**
     * Refreshes the tables layout by requesting updated table data from the server.
     *
     * @param event the action event triggered by the refresh button
     */
    @FXML
    void handleRefreshTables(ActionEvent event) {
        BistroClient.tables = new ArrayList<>();
        getAllTables();
    }
    
    // ----------------------------------------------------------------
    // 					CHECK-IN
    // ----------------------------------------------------------------

    /**
     * Handles member or reservation check-in action.
     */ 
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
    
    /**
     * Updates check-in status based on server response.
     *
     * @param response the server response object
     */
    public void updateCheckInStatus(Object response) {
        Platform.runLater(() -> {
            if(response instanceof String) {
                String message = (String)response;
                if(message.startsWith("Error")) {
                    checkInStatusLabel.setText(message);
                    checkInStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else { 
                    checkInStatusLabel.setText("Started preparing table for this reservation. Wait when table is ready!");
                    checkInStatusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    memberCodeField.clear();
                }   
            } else {
                Visit created = (Visit)response;
                checkInStatusLabel.setText(created.getGuest().getFullName() +" checked in successfully to table: " +created.getTable().getTableNumber());
                checkInStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                memberCodeField.clear();
            }
        });
    }
    // ----------------------------------------------------------------
    // 				MEMBER'S
    // ----------------------------------------------------------------

    /**
     * Handles new member registration.
     */
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
        memberToCreate.setCardCode("CARD-" + (int)(Math.random() * 9000 + 1000)); 
        
        ClientUI.chat.accept(new BistroMessage(Action.CREATE_MEMBER, memberToCreate));
    }
    
    /**
     * Called when member creation completes.
     *
     * @param isCreated creation success flag
     * @param message   server response message
     */
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
    
    /**
     * Requests the list of all registered members from the server.
     *
     * @param event the action event triggered by the "Show Members" button
     */
    @FXML
    void showMembers(ActionEvent event) {
        // Send request to server to fetch all members
    	ClientUI.chat.accept(new BistroMessage(Action.GET_ALL_MEMBERS, null));
    }

    /**
     * Updates the members table with data received from the server.
     * <p>
     * Runs on the JavaFX Application Thread.
     *
     * @param members list of members to display
     */
    public void updateMembersList(ArrayList<Member> members) {
        Platform.runLater(() -> {
            if (members != null && !members.isEmpty()) {
                membersTable.setItems(FXCollections.observableArrayList(members));
                membersTable.refresh();
            } else {
                membersTable.setItems(FXCollections.observableArrayList());
            }
        });
    }

    

    // ----------------------------------------------------------------
    // 					NAVIGATION & LOGOUT
    // ----------------------------------------------------------------    
    
    /**
     * Opens the "New Reservation" window.
     */
    @FXML void handleNewReservation(ActionEvent event) { SceneLoader.openNewWindow("/gui/MakeReservation.fxml", "New Reservation"); }
    
    /**
     * Opens the "Find Reservation" window.
     */
    @FXML void handleFindReservation(ActionEvent event) { SceneLoader.openNewWindow("/gui/ReservationFrame.fxml", "Find Reservation"); }
    
    /**
     * Opens the waiting list window for walk-in customers.
     */
    @FXML void handleInsertParty(ActionEvent event) { SceneLoader.openNewWindow("/gui/VisitNow.fxml", "Waiting List"); }
   
    /**
     * Opens the waiting list management window.
     */
    @FXML void handleWaitingList(ActionEvent event) { SceneLoader.openNewWindow("/gui/StaffWaitingList.fxml", "Waiting List Management"); } 
    
    /**
     * Opens the visit identification (customer arrival) window.
     */
    @FXML void handleCustomerArrival(ActionEvent event) { SceneLoader.openNewWindow("/gui/VisitIdentification.fxml", "Visit Identification"); }
    
    /**
     * Opens the current dining sessions window.
     */
    @FXML void handleCurrentVisits(ActionEvent event) { SceneLoader.openNewWindow("/gui/ViewVisits.fxml", "Current Dinning Sessions");}
    
    /**
     * Opens the system settings window for managing restaurant configuration.
     *
     * @param event the action event triggered by the settings button
     */
    @FXML
    void openSystemSettings(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/SystemSettings.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("System Settings - Opening Hours");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Could not open System Settings window.");
        }
    }
    
    /**
     * Logs out the staff user and returns to login screen.
     */
    @FXML
    void handleLogout(ActionEvent event) {
        BistroClient.staffInstance = null;
        BistroClient.staffDashControllerInstance = null;
        BistroClient.tables = new ArrayList<>();
        ClientUI.chat.accept(new BistroMessage(Action.DISCONNECT, null));
        SceneLoader.loadScene(event, "/gui/StaffLoginGUI.fxml", "Staff Login");
    }

    /**
     * Toggles password visibility in registration form.
     */
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
    
    /**
     * Opens the window displaying all reservations in the system.
     *
     * @param event the action event triggered by the "View All Reservations" button
     */
    @FXML
    void handleViewAllReservations(ActionEvent event) {
        SceneLoader.openNewWindow("/gui/ViewReservations.fxml", "Management - All Reservations");
    }
    
    /**
     * Send request to find needed report
     * @param event action avenet triggered by View Report button
     */
    @FXML
    void handleViewReport(ActionEvent event) {
        Integer year = reportYearCombo.getValue();
        String monthStr = reportMonthCombo.getValue();
        String typeStr = reportTypeCombo.getValue();

        if (year == null || monthStr == null || typeStr == null) {
            SceneLoader.showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select Year, Month, and Report Type.");
            return;
        }

        //month number
        int month = Integer.parseInt(monthStr.split(" ")[0]);

        //Construct filename based on type
        //Our naming convention: "Time_Report_M_YYYY.pdf" or "Member_Report_M_YYYY.pdf"
        String prefix = typeStr.startsWith("Time") ? "Time_Report" : "Member_Report";
        String filename = prefix + "_" + month + "_" + year + ".pdf";

        System.out.println("Requesting report: " + filename);
        ClientUI.chat.accept(new BistroMessage(Action.GET_REPORT_FILE, filename));
    }
    
    /**
     * Function called from BistroClient to show report to user using default pdf-viewer tool
     * @param data if request succeded - array of bytes, else reecieve null
     */
    public void receiveReport(Object data) {
        Platform.runLater(() -> {
            if (data == null) {
                SceneLoader.showAlert(Alert.AlertType.INFORMATION, "Report Not Found", 
                          "The report you requested is not ready yet.\n" +
                          "Reports are generated automatically on the 1st of each month.");
            } else if (data instanceof byte[]) {
                try {
                    byte[] pdfBytes = (byte[]) data;
                    File tempFile = File.createTempFile("BistroReportView", ".pdf");
                    tempFile.deleteOnExit(); //Not saving on client side
                    
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(pdfBytes);
                    }
                    //Open with default pdf editor
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(tempFile);
                    } else {
                        SceneLoader.showAlert(Alert.AlertType.WARNING, "Error", "Cannot open PDF: Desktop not supported.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Failed to open the report file.");
                }
            } else {
                SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Received invalid data from server.");
            }
        });
    }
}