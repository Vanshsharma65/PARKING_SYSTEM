import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDB {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:parking.db";
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(url)) {
                
                System.out.println("--- SPOTS TABLE ---");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT count(*) as count FROM spots")) {
                    if (rs.next()) {
                        System.out.println("Total spots: " + rs.getInt("count"));
                    }
                }
                
                System.out.println("\n--- RECENT TICKETS ---");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM tickets ORDER BY id DESC LIMIT 5")) {
                    int count = 0;
                    while (rs.next()) {
                        System.out.println("Ticket ID: " + rs.getInt("id") + ", Plate: " + rs.getString("license_plate") + ", Spot ID: " + rs.getInt("spot_id") + ", Entry: " + rs.getString("entry_time") + ", Exit: " + rs.getString("exit_time") + ", Fee: " + rs.getDouble("fee") + ", Email: " + rs.getString("customer_email"));
                        count++;
                    }
                    if (count == 0) {
                        System.out.println("No tickets found.");
                    }
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
