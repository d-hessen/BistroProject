package handlers;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import client.BistroClient;
import javafx.scene.control.Alert;
public class EmailSend{
	public static void sendConfirmationNotifications(String messageInfo) {
	        if (BistroClient.memberInstance != null) {
	            String recipientEmail = BistroClient.memberInstance.getEmail();
	            if (recipientEmail != null && !recipientEmail.isEmpty()) {
	                new Thread(() -> {
	                    sendEmail(recipientEmail, "Visit Confirmation - Bistro", 
	                              "Hello " + BistroClient.memberInstance.getFullName() + ",\n\n" +
	                              "Your visit has been registered successfully.\n" +
	                               messageInfo + "\n\n" +
	                              "Thank you for choosing Bistro!");
	                }).start();
	            }
	        }
	        SceneLoader.showAlert(Alert.AlertType.INFORMATION, "Notification", "A message was sent to your phone.\n"
	        		+ "Phone Number : " + BistroClient.memberInstance.getPhoneNumber());
	        
	        
	    }
	    
	    public static void sendEmail(String recipient, String subject, String content) {
	        final String username = "Bistro310701@gmail.com"; 
	        final String password = "ngbm oeqa mvlt jfxi";
	        
	        Properties prop = new Properties();
	        prop.put("mail.smtp.host", "smtp.gmail.com");
	        prop.put("mail.smtp.port", "587");
	        prop.put("mail.smtp.auth", "true");
	        prop.put("mail.smtp.starttls.enable", "true");
	
	        Session session = Session.getInstance(prop,
	                new javax.mail.Authenticator() {
	                    protected PasswordAuthentication getPasswordAuthentication() {
	                        return new PasswordAuthentication(username, password);
	                    }
	                });
	
	        try {
	            Message message = new MimeMessage(session);
	            message.setFrom(new InternetAddress(username));
	            message.setRecipients(
	                    Message.RecipientType.TO,
	                    InternetAddress.parse(recipient)
	            );
	            message.setSubject(subject);
	            message.setText(content);
	
	            Transport.send(message);
	            System.out.println("Email sent successfully to: " + recipient);
	
	        } catch (MessagingException e) {
	            e.printStackTrace();
	            System.err.println("Failed to send email: " + e.getMessage());
	        }
	    }
}