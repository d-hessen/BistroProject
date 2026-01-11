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

public class MemberProfileController implements Initializable {

	@FXML private TextField nameField;
	@FXML private TextField phoneField;
	@FXML private TextField emailField;
    @FXML
    private ImageView digitalCardImageView;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	BistroClient.memberProfileControllerInstance = this;
        Member member = BistroClient.memberInstance;
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

    @FXML
    private void handleBack(ActionEvent event) {
        SceneLoader.loadScene(event, "/gui/ClientDashboard.fxml", "Client Dashboard");
    }
    
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
     * Generates a QR code Image from a given text string using the ZXing library.
     * @param text The text to encode in the QR code.
     * @param width The width of the generated image.
     * @param height The height of the generated image.
     * @return The generated JavaFX Image.
     * @throws WriterException If an error occurs during encoding.
     * @throws IOException If an error occurs during image writing.
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