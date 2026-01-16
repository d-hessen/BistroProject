package utils;

import java.util.List;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import dataLayer.Guest;
import dataLayer.Member;
import dataLayer.Reservation;
import dataLayer.Visit;

public class EmailSend {

    private static final String USERNAME = "Bistro310701@gmail.com";
    private static final String PASSWORD = "ngbm oeqa mvlt jfxi";
    
    //Send verification codes to client
    public static void sendReservationsTableByEmail(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) return;

        Recipient recipient = resolveRecipient(reservations.get(0));
        if (!recipient.isValid()) return;

        String html = buildReservationsHtml(recipient.name, reservations);
        asyncSendHtml(recipient.email, "Your Reservation Codes – Bistro", html);
        
        String smsContent = buildReservationsText(recipient.name, reservations);
        sendSMS(recipient, "Your Reservation Codes", smsContent);
    }
    //Send reminder about upcoming reservation to client
    public static void sendReminder(Reservation res) {
        Recipient recipient = resolveRecipient(res);
        if (!recipient.isValid()) return;

        String content = "Hello " + recipient.name + ",\n\n" +
                         "This is a reminder that you have a reservation at Bistro in less than 2 hours.\n" +
                         "Date: " + res.getReservationDate().getDate() + "\n" +
                         "Time: " + res.getReservationDate().getTime() + "\n" +
                         "Guests: " + res.getNumberOfGuests() + "\n\n" +
                         "We look forward to seeing you!";

        asyncSendText(recipient.email, "Upcoming Reservation Reminder - Bistro", content);
        System.out.println("Reminder sent to: " + recipient.email);
        sendSMS(recipient, "Reservation Reminder", content);
    }
    //Send cancellation message (Late for more than 15 mins to reservation)
    public static void sendCancellationNotice(Reservation res) {
        Recipient recipient = resolveRecipient(res);
        if (!recipient.isValid()) return;

        String content = "Hello " + recipient.name + ",\n\n" +
                         "Your reservation has been automatically cancelled because " +
                         "we did not detect an arrival within 15 minutes of the scheduled time.\n\n" +
                         "Reservation ID: " + res.getReservationId() + "\n\n" +
                         "If this is a mistake, please contact the staff.";

        asyncSendText(recipient.email, "Reservation Cancelled - Bistro", content);
        System.out.println("Cancellation notice sent to: " + recipient.email);
        sendSMS(recipient, "Reservation Cancelled", content);
    }
    //Send message that table is ready to waiting visit
    public static void sendTableReadyNotification(Visit visit) {
        Recipient recipient = resolveRecipient(visit); 
        if (!recipient.isValid()) return;

        String content = "Hello " + recipient.name + ",\n\n" +
                         "Good news! Your table at Bistro is now ready.\n" +
                         "Please return to the host stand within 15 minutes to claim your table.\n\n" +
                         "Verification Code: " + visit.getVerificationCode();

        asyncSendText(recipient.email, "Your Table is Ready! - Bistro", content);
        System.out.println("Table ready notification sent to: " + recipient.email);
        sendSMS(recipient, "Table Ready", content);
    }
    //Send that waiting visit is cancelled due to late for more than 15 mins
    public static void sendWaitingListCancellation(Visit visit) {
        Recipient recipient = resolveRecipient(visit);
        if (!recipient.isValid()) return;

        String content = "Hello " + recipient.name + ",\n\n" +
                         "We are sorry, but your table reservation on the waiting list has been cancelled.\n" +
                         "We held the table for 15 minutes, but we did not detect your arrival.\n\n" +
                         "If you are still at the restaurant, please see the host to re-join the list.";

        asyncSendText(recipient.email, "Waiting List Cancellation - Bistro", content);
        System.out.println("Waitlist cancellation notice sent to: " + recipient.email);
        sendSMS(recipient, "Waitlist Cancelled", content);
    }
    //Send bill to client
    public static void sendBillNotification(Visit visit) {
        Recipient recipient = resolveRecipient(visit);
        if (!recipient.isValid()) return;

        double amount = visit.getBillOfVisit() != null ? visit.getBillOfVisit().getFinalAmount() : 0.0;

        String content = "Hello " + recipient.name + ",\n\n" +
                         "Your visit is done.\n" +
                         "Please review your bill:\n" +
                         "Total Amount: ₪" + amount + "\n\n" + 
                         "If you haven't paid it yet,\n" +
                         "Please proceed to payment to complete your visit.";

        asyncSendText(recipient.email, "Your Bill - Bistro", content);
        System.out.println("Bill sent to: " + recipient.email);
        sendSMS(recipient, "Bill Notification", content);
    }
    
    public static void sendMembershipCreation(Member member) {
        Recipient recipient = resolveRecipient(member);
        if (!recipient.isValid()) return;

        String content = "Welcome to Bistro, " + recipient.name + "!\n\n" +
                         "Your membership has been successfully created.\n\n" +
                         "Here are your details:\n" +
                         "Member ID: " + member.getMemberId() + "\n" +
                         "Card Code: " + member.getCardCode() + "\n" +
                         "Password: " + member.getPassword() + "\n\n" +
                         "Please keep your Card Code safe. You can use it to identify yourself at the restaurant.\n" +
                         "Thank you for joining us!";

        // Send Text Email
        asyncSendText(recipient.email, "Welcome to Bistro! Membership Details", content);
        
        // Send SMS
        sendSMS(recipient, "Membership Created", content);
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

        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void sendSMS(Recipient recipient, String subject, String content) {
        if (recipient.phone == null || recipient.phone.isEmpty()) return;

        System.out.println("\n_________________________________________________");
        System.out.println("           [SMS]           ");
        System.out.println("To:" + recipient.phone + " (" + recipient.name + ")");
        System.out.println("Subject: " + subject);
        System.out.println("Message: ");
        System.out.println(content);
        System.out.println("_________________________________________________\n");
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
    
    private static Recipient resolveRecipient(Member member) {
        String email = member.getEmail();
        String name = member.getFullName();
        String phone = member.getPhoneNumber();
        return new Recipient(email, name, phone);
    }

    private static Recipient resolveRecipient(Reservation res) {
        String email = null;
        String name = "Guest";
        String phone = null;

        if (res != null && res.getGuest() != null) {
            email = res.getGuest().getEmail();
            name = res.getGuest().getFullName();
            phone = res.getGuest().getPhoneNumber();
        }

        return new Recipient(email, name, phone);
    }
    
    private static Recipient resolveRecipient(Visit visit) {
        String email = null;
        String name = "Guest";
        String phone = null;

        Guest person = visit.getGuest(); 

        if (person != null) {
            email = person.getEmail();
            name = person.getFullName();
            phone = person.getPhoneNumber();
        }

        return new Recipient(email, name, phone);
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
    
    private static String buildReservationsText(String name, List<Reservation> reservations) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hello ").append(name).append(",\n\n");
        sb.append("Your Reservation Codes:\n");
        
        for (Reservation r : reservations) {
            sb.append("----------------\n");
            sb.append("Date: ").append(r.getReservationDate()).append("\n");
            sb.append("Code: ").append(r.getVerificationCode()).append("\n");
            sb.append("Guests: ").append(r.getNumberOfGuests()).append("\n");
        }
        sb.append("----------------\n");
        sb.append("See you at Bistro!");
        return sb.toString();
    }

    private static class Recipient {
        String email;
        String name;
        String phone;

        Recipient(String email, String name, String phone) {
            this.email = email;
            this.name = name != null ? name : "Customer";
            this.phone = phone;
        }

        boolean isValid() {
            return email != null && !email.isEmpty();
        }
    }
}