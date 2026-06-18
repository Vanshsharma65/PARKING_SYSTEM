package ParkingSystem;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {

    static String senderEmail = "vanshsharmagraphic44@gmail.com";
    static String appPassword = "ftqaxhawknrvatmt";

    public static void sendReceipt(String toEmail, String plate, String entryTime,
            String exitTime, String duration, String billed, int ticketId, double fee) {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(senderEmail));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("Parking Receipt - Ticket #" + ticketId);
            

            String body = "Parking Receipt\n"
                    + "Ticket  : " + ticketId + "\n"
                    + "Plate   : " + plate + "\n"
                    + "Entry   : " + entryTime + "\n"
                    + "Exit    : " + exitTime + "\n"
                    + "Duration: " + duration + "\n"
                    + "Billed  : " + billed + "\n"
                    + "Fee     : Rs." + (int) fee + "\n"
                    + "Thank you!";

            msg.setText(body);
            Transport.send(msg);
            System.out.println("Email sent to " + toEmail);
        } catch (MessagingException e) {
            System.out.println("Email failed: " + e.getMessage());
        }
    }
}
