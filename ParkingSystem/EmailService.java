package ParkingSystem;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService 
{
    private static final String SENDER_EMAIL = "vanshsharmagraphic44@gmail.com";
    private static final String APP_PASSWORD  = "ftqaxhawknrvatmt";

    public static void sendReceipt(String toEmail, String licensePlate,
                                   String entryTime, String exitTime,
                                   String duration, String billedAs,
                                   int ticketId, double totalFee) {
        Properties props = new Properties();
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Parking Receipt - Ticket #" + ticketId);

            String body =
                "========================================\n" +
                "          PARKING RECEIPT\n" +
                "========================================\n" +
                "Ticket ID     : " + ticketId + "\n" +
                "License Plate : " + licensePlate.toUpperCase() + "\n" +
                "Entry Time    : " + entryTime + "\n" +
                "Exit Time     : " + exitTime + "\n" +
                "Duration      : " + duration + "\n" +
                "Billed As     : " + billedAs + "\n" +
                "Total Fee     : Rs. " + (int) totalFee + "\n" +
                "========================================\n" +
                "Thank you for using our Parking System!\n";

            message.setText(body);
            Transport.send(message);
            System.out.println("Receipt sent to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}
