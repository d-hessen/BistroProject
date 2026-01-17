package handlers;

import client.BistroClient;
import client.ClientUI;
import common.Action;
import common.BistroMessage;
import dataLayer.Member;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

// Imports for ZXing library
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * MemberProfileController manages the member profile screen.
 * <p>
 * This controller is responsible for:
 * <ul>
 *   <li>Displaying member personal details</li>
 *   <li>Allowing a member to update contact information</li>
 *   <li>Generating and displaying a digital membership card (QR code)</li>
 *   <li>Handling navigation back to the client dashboard</li>
 * </ul>
 *
 * <p>
 * The controller implements {@link Initializable} to initialize
 * the UI state after the FXML has been loaded.
 */
public class MemberProfileController implements Initializable {

    /**
     * Text field displaying the member's full name (read-only).
     */
    @FXML private TextField nameField;

    /**
     * Text field for editing the member's phone number.
     */
    @FXML private TextField phoneField;

    /**
     * Text field for editing the member's email address.
     */
    @FXML private TextField emailField;

    /**
     * Image view used to display the member's digital card (QR code).
     */
    @FXML
    private ImageView digitalCardImageView;

    /**
     * Regular expression used for validating email addresses.
     */
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    /**
     * Initializes the member profile screen after the FXML has been loaded.
     * <p>
     * Loads member details, disables name editing, and generates
     * a QR code image for the member's digital card if available.
     *
     * @param location the location used to resolve relative paths
     * @param resources the resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	
        // Register this controller instance for client-side callbacks
    	BistroClient.memberProfileControllerInstance = this;
        Member member = BistroClient.memberInstance;
        // Full name is not editable
        nameField.setDisable(true);
        if (member != null) {
            nameField.setText(member.getFullName());
            phoneField.setText(member.getPhoneNumber());
            emailField.setText(member.getEmail());

            String cardCode = member.getCardCode();
            if (cardCode != null && !cardCode.isEmpty()) {
                try {
                    // Generate QR Code image from cardCode
                    Image qrCodeImage = generateQRCodeImage(cardCode, 250, 250);
                    digitalCardImageView.setImage(qrCodeImage);
                } catch (WriterException | IOException e) {
                    e.printStackTrace();
                    SceneLoader.showAlert(Alert.AlertType.ERROR, "Error", "Could not generate digital card.");
                }
            } else {
                SceneLoader.showAlert(Alert.AlertType.WARNING, "Warning", "No card code found for this member.");
            }
        }
    }

    /**
     * Handles the action when the "Back" button is clicked.
     * <p>
     * Navigates the user back to the client dashboard.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    }
    
    /**
     * Handles the action when the "Save" button is clicked.
     * <p>
     * Validates the updated phone number and email address,
     * then sends an update request to the server.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void handleSave(ActionEvent event) {
    	String phone = phoneField.getText().trim();
    	String email = emailField.getText().trim();
    	
    	if (phone.isEmpty() || email.isEmpty()) {
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
        
        Member toUpdate = new Member(null, phone, email, null);
        toUpdate.setMemberId(BistroClient.memberInstance.getMemberId());
        ClientUI.chat.accept(new BistroMessage(Action.UPDATE_MEMBER, toUpdate));
    }
    
    /**
     * Callback method invoked after a member update response is received.
     * <p>
     * Updates the UI with the new member details if the update
     * was successful, otherwise displays an error message.
     *
     * @param updated the updated {@link Member} object,
     *                or {@code null} if the update failed
     */
    public void isUpdated(Member updated) {
    	Platform.runLater(()->{
    		if(updated != null) {
    			BistroClient.memberInstance = updated;
    			phoneField.setText(BistroClient.memberInstance.getPhoneNumber());
    			emailField.setText(BistroClient.memberInstance.getEmail());
    			SceneLoader.showAlert(Alert.AlertType.INFORMATION, "Editing Details", "Details updated successfully!");
    		} else {
    			SceneLoader.showAlert(Alert.AlertType.ERROR, "Editing Details", "Error updating details! Try again.");
    		}
    		
    	});
    }

    /**
     * Generates a QR code {@link Image} from the given text using the ZXing library.
     *
     * @param text   the text to encode in the QR code
     * @param width  the width of the generated image
     * @param height the height of the generated image
     * @return the generated JavaFX {@link Image}
     * @throws WriterException if an error occurs during QR encoding
     * @throws IOException if an error occurs while writing the image data
     */
    private Image generateQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        // Converts the BitMatrix to a PNG image stream
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();
        // Creates a JavaFX Image from the byte array
        return new Image(new ByteArrayInputStream(pngData));
    }
}