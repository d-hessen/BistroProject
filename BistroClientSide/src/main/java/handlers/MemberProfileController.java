package handlers;

import client.BistroClient;
import dataLayer.Member;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

// Imports for ZXing library
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class MemberProfileController implements Initializable {

    @FXML
    private Label nameLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private ImageView digitalCardImageView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Member member = BistroClient.memberInstance;
        if (member != null) {
            nameLabel.setText("Name: " + member.getFullName());
            phoneLabel.setText("Phone: " + member.getPhoneNumber());
            emailLabel.setText("Email: " + member.getEmail());

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