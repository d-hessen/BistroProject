package handlers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Member;

public class StaffDashboardController implements Initializable {

    // --- FXML Injections ---

    @FXML private Label dashboardTitleLabel;
    @FXML private Label welcomeLabel;
    @FXML private TabPane mainTabPane;

    // Operations Tab
    @FXML private TextField reservationCodeField;
    @FXML private Label checkInStatusLabel;

    // Table Management Tab
    @FXML private GridPane tablesGrid;

    // Register Member Tab
    @FXML private TextField regFullName;
    @FXML private TextField regPhone;
    @FXML private TextField regEmail;
    @FXML private PasswordField PasswordField;
    @FXML private TextField PasswordFieldVisible;
    @FXML private CheckBox showPasswordTick;

    // Reports Tab
    @FXML private Tab managerReportsTab;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private ComboBox<String> monthCombo;
    @FXML private TableView<ReportData> reportsTable;

    // Admin Tab
    @FXML private Tab managerAdminTab;
    @FXML private TableView<LogEntry> logsTable;
    
    // Regular expression for basic email validation
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    // --- Initialization ---

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupTables();
        welcomeLabel.setText("Welcome, " +BistroClient.staffInstance.getFullName());
    }

    private void setupComboBoxes() {
        reportTypeCombo.setItems(FXCollections.observableArrayList("Reservations", "Waiting Lists", "Guest's arrival and leaving times"));
        monthCombo.setItems(FXCollections.observableArrayList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"));
    }

    private void setupTables() {
        // Setup Reports Table Columns
        if (!reportsTable.getColumns().isEmpty()) {
            reportsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("category"));
            reportsTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("value"));
            reportsTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("date"));
        }

        // Setup Logs Table Columns
        if (!logsTable.getColumns().isEmpty()) {
            logsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("time"));
            logsTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("user"));
            logsTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("action"));
        }
    }

    // --- Event Handlers ---
    
    @FXML
    void handleNewReservation(ActionEvent event) {
    	SceneLoader.openNewWindow("/gui/MakeReservation.fxml", "Make new reservation");
    }
    
    @FXML
    void handleFindReservation(ActionEvent event) {
    	SceneLoader.openNewWindow("/gui/ReservationFrame.fxml", "Reservation finder");
    }
    
    @FXML
    void handleInsertParty(ActionEvent event) {
    	SceneLoader.openNewWindow("/gui/VisitNow.fxml", "Insert Party to Waiting List");
    }
    
    @FXML
    void handleWaitingList(ActionEvent event) {
    	
    }
    
    @FXML
    void handleCustomerArrival(ActionEvent event) {
    	
    }

    @FXML
    void handleLogout(ActionEvent event) {
    	BistroClient.staffInstance = null;
    	ClientUI.chat.accept(new BistroMessage(Action.DISCONNECT, null));
    	SceneLoader.loadScene(event, "/gui/StaffLoginGUI.fxml", "Member Login");
    }

    @FXML
    void handleMemberCheckIn(ActionEvent event) {
        String code = reservationCodeField.getText();
        if (code == null || code.trim().isEmpty()) {
            checkInStatusLabel.setText("Error: Please enter a code.");
            checkInStatusLabel.setStyle("-fx-text-fill: red;");
        } else {
            // Mock validation logic
            checkInStatusLabel.setText("Success: Reservation " + code + " checked in.");
            checkInStatusLabel.setStyle("-fx-text-fill: green;");
            reservationCodeField.clear();
        }
    }
    
    @FXML
    void openTableInfo(ActionEvent event) {
    	if(event.getSource() instanceof Button) {
    		Button button = (Button) event.getSource();
    		
    		System.out.println(button.getText()+" pressed");
    	}
    	
    }

    @FXML
    void handleRegisterMember(ActionEvent event) {
        String phone = regPhone.getText().trim();
        String email = regEmail.getText().trim();
        String name = regFullName.getText().trim();
        
        // Get password from the currently active field
        String pass = showPasswordTick.isSelected() ? PasswordFieldVisible.getText() : PasswordField.getText();
        
        // Validate Full Name: Must contain at least two words
        String[] nameParts = name.split("\\s+");
        if (nameParts.length < 2) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Full name must contain at least two words.");
            return;
        }

        // Validate Phone Number: Must contain digits only
        if (!phone.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Phone number must contain numbers only.");
            return;
        }

        // Validate Email: Must follow standard email format
        if (!Pattern.compile(EMAIL_REGEX).matcher(email).matches()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Invalid email format.");
            return;
        }

        // Validate Password: Must not be empty
        if (pass.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Password cannot be empty.");
            return;
        }

        // If all validations pass, proceed with registration logic
        System.out.println("Validation successful for user: " + name);
        
        Member memberToCreate = new Member(name,phone,email,pass);
        memberToCreate.setCardCode("CARD-" + (int)(Math.random() * 9000 + 1000));
        ClientUI.chat.accept(new BistroMessage(Action.CREATE_MEMBER, memberToCreate));
        regPhone.clear();
        regEmail.clear();
        regFullName.clear();
        PasswordField.clear();
    }
    
    // Toggles the password visibility between masked and plain text.
    @FXML
    private void togglePasswordVisible(ActionEvent event) {
        if (showPasswordTick.isSelected()) {
            // Switch to visible text field
            PasswordFieldVisible.setText(PasswordField.getText());
            PasswordFieldVisible.setVisible(true);
            PasswordFieldVisible.setManaged(true);
            PasswordField.setVisible(false);
            PasswordField.setManaged(false);
        } else {
            // Switch back to masked password field
            PasswordField.setText(PasswordFieldVisible.getText());
            PasswordField.setVisible(true);
            PasswordField.setManaged(true);
            PasswordFieldVisible.setVisible(false);
            PasswordFieldVisible.setManaged(false);
        }
    }

    @FXML
    void handleGenerateReport(ActionEvent event) {
        String type = reportTypeCombo.getValue();
        String month = monthCombo.getValue();

        if (type == null || month == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Selection", "Please select both Report Type and Month.");
            return;
        }

        // Mock Data Generation
        ObservableList<ReportData> data = FXCollections.observableArrayList(
            new ReportData(type, "1500.00", "01-" + month),
            new ReportData(type, "2300.50", "15-" + month)
        );
        reportsTable.setItems(data);
    }

    // --- Helper Methods ---

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- Data Models (Inner Classes for TableViews) ---

    public static class ReportData {
        private final String category;
        private final String value;
        private final String date;

        public ReportData(String category, String value, String date) {
            this.category = category;
            this.value = value;
            this.date = date;
        }

        public String getCategory() { return category; }
        public String getValue() { return value; }
        public String getDate() { return date; }
    }

    public static class LogEntry {
        private final String time;
        private final String user;
        private final String action;

        public LogEntry(String time, String user, String action) {
            this.time = time;
            this.user = user;
            this.action = action;
        }

        public String getTime() { return time; }
        public String getUser() { return user; }
        public String getAction() { return action; }
    }

	public void memberCreated(boolean isCreated, String message) {
    	Platform.runLater(() -> {
            if(isCreated) {
            	showAlert(Alert.AlertType.INFORMATION, "Registration Confirmation", "Member has been signed up successfully!");
            } else {
            	showAlert(Alert.AlertType.ERROR, "Registration Error", "There was an error signing up member! Error: " + message);
            }
        });
		
	}
}