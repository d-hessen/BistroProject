package handlers;

import java.util.List;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import client.BistroClient;
import dataLayer.Reservation;
import javafx.scene.control.Alert;

public class EmailSend {

    private static final String USERNAME = "Bistro310701@gmail.com";
    private static final String PASSWORD = "ngbm oeqa mvlt jfxi";

    public static void sendConfirmationNotifications(String messageInfo) {
        Recipient recipient = resolveRecipient(
                VisitIdentificationController.created.getReservation()
        );

        if (!recipient.isValid()) return;

        String content =
                "Hello " + recipient.name + ",\n\n" +
                "Your visit has been registered successfully.\n" +
                messageInfo + "\n\n" +
                "Thank you for choosing Bistro!";

        asyncSendText(recipient.email, "Visit Confirmation - Bistro", content);

        SceneLoader.showAlert(
                Alert.AlertType.INFORMATION,
                "Notification",
                "A message was sent to your phone.\nPhone Number: " + recipient.phone
        );
    }

    public static void sendReservationsTableByEmail(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) return;

        Recipient recipient = resolveRecipient(reservations.get(0));
        if (!recipient.isValid()) return;

        String html = buildReservationsHtml(recipient.name, reservations);
        asyncSendHtml(recipient.email, "Your Reservation Codes – Bistro", html);
    }

    private static void asyncSendText(String to, String subject, String text) {
        new Thread(() -> sendEmail(to, subject, text, false)).start();
    }

    private static void asyncSendHtml(String to, String subject, String html) {
        new Thread(() -> sendEmail(to, subject, html, true)).start();
    }

    private static void sendEmail(String to, String subject, String content, boolean isHtml) {
        try {
            Session session = createSession();

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            if (isHtml) {
                message.setContent(content, "text/html; charset=UTF-8");
            } else {
                message.setText(content);
            }

            Transport.send(message);
            System.out.println("Email sent to: " + to);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private static Session createSession() {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(prop, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }

    private static Recipient resolveRecipient(Reservation res) {
        if (BistroClient.memberInstance != null) {
            return new Recipient(
                    BistroClient.memberInstance.getEmail(),
                    BistroClient.memberInstance.getFullName(),
                    BistroClient.memberInstance.getPhoneNumber()
            );
        }
        return new Recipient(
                res.getGuest().getEmail(),
                res.getGuest().getFullName(),
                res.getGuest().getPhoneNumber()
        );
    }

    private static String buildReservationsHtml(String name, List<Reservation> reservations) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><body style='font-family: Arial;'>")
          .append("<h2>Hello ").append(name).append(",</h2>")
          .append("<p>Here is a summary of your reservations:</p>")
          .append("<table border='1' cellpadding='8' cellspacing='0' ")
          .append("style='border-collapse: collapse; width:100%; text-align:center;'>")
          .append("<tr style='background-color:#f2f2f2; font-weight:bold;'>")
          .append("<th>Order #</th>")
          .append("<th>Date & Time</th>")
          .append("<th>Verification Code</th>")
          .append("<th>Guests</th>")
          .append("</tr>");

        for (Reservation r : reservations) {
            sb.append("<tr style='text-align:center;'>")
              .append("<td>").append(r.getReservationId()).append("</td>")
              .append("<td>").append(r.getReservationDate()).append("</td>")
              .append("<td>").append(r.getVerificationCode()).append("</td>")
              .append("<td>").append(r.getNumberOfGuests()).append("</td>")
              .append("</tr>");
        }

        sb.append("</table>")
          .append("<br><p>Thank you for choosing <b>Bistro</b>.</p>")
          .append("</body></html>");

        return sb.toString();
    }

    private static class Recipient {
        String email;
        String name;
        String phone;

        Recipient(String email, String name, String phone) {
            this.email = email;
            this.name = name;
            this.phone = phone;
        }

        boolean isValid() {
            return email != null && !email.isEmpty();
        }
    }
}
