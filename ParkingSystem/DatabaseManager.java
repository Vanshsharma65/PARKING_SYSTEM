package ParkingSystem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:parking.db";

    public DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC"); 
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found. Please ensure sqlite-jdbc.jar is in the classpath.");
        }
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String createSpotsTable = "CREATE TABLE IF NOT EXISTS spots (" +
                    "id INTEGER PRIMARY KEY, " +
                    "type TEXT NOT NULL, " +
                    "is_occupied INTEGER DEFAULT 0)";
            stmt.execute(createSpotsTable);
            String createTicketsTable = "CREATE TABLE IF NOT EXISTS tickets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "license_plate TEXT NOT NULL, " +
                    "spot_id INTEGER NOT NULL, " +
                    "entry_time TEXT NOT NULL, " +
                    "exit_time TEXT, " +
                    "fee REAL, " +
                    "customer_email TEXT, " +
                    "FOREIGN KEY (spot_id) REFERENCES spots(id))";
            stmt.execute(createTicketsTable);
            try {
                stmt.execute("ALTER TABLE tickets ADD COLUMN customer_email TEXT");
            } catch (SQLException ignored) {
            }

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM spots");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Initializing parking spots...");
                try (PreparedStatement insertSpot = conn.prepareStatement(
                        "INSERT INTO spots (id, type, is_occupied) VALUES (?, ?, 0)")) {
                   
                    for (int i = 1; i <= 5; i++) {
                        insertSpot.setInt(1, i);
                        insertSpot.setString(2, "Car");
                        insertSpot.executeUpdate();
                    }
                    for (int i = 6; i <= 10; i++) {
                        insertSpot.setInt(1, i);
                        insertSpot.setString(2, "Bike");
                        insertSpot.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    public List<ParkingSpot> getAvailableSpots(String type) {
        List<ParkingSpot> spots = new ArrayList<>();
        String query = "SELECT * FROM spots WHERE type = ? AND is_occupied = 0";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, type);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                spots.add(new ParkingSpot(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getInt("is_occupied") == 1
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching spots: " + e.getMessage());
        }
        return spots;
    }

    public List<ParkingSpot> getAllSpots() {
        List<ParkingSpot> spots = new ArrayList<>();
        String query = "SELECT * FROM spots";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                spots.add(new ParkingSpot(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getInt("is_occupied") == 1
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching spots: " + e.getMessage());
        }
        return spots;
    }

    public int parkVehicle(Vehicle vehicle, int spotId, String customerEmail) {
        String updateSpot = "UPDATE spots SET is_occupied = 1 WHERE id = ?";
        String insertTicket = "INSERT INTO tickets (license_plate, spot_id, entry_time, customer_email) VALUES (?, ?, ?, ?)";
        int ticketId = -1;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmtSpot = conn.prepareStatement(updateSpot);
             PreparedStatement pstmtTicket = conn.prepareStatement(insertTicket, Statement.RETURN_GENERATED_KEYS)) {
            
            conn.setAutoCommit(false); 
            pstmtSpot.setInt(1, spotId);
            pstmtSpot.executeUpdate();
            pstmtTicket.setString(1, vehicle.getLicensePlate());
            pstmtTicket.setInt(2, spotId);
            pstmtTicket.setString(3, LocalDateTime.now().toString());
            pstmtTicket.setString(4, customerEmail);
            pstmtTicket.executeUpdate();
            ResultSet rs = pstmtTicket.getGeneratedKeys();
            if (rs.next()) {
                ticketId = rs.getInt(1);
            }

            conn.commit(); 
        } catch (SQLException e) {
            System.err.println("Error parking vehicle: " + e.getMessage());
        }
        return ticketId;
    }

    public Ticket getActiveTicket(String licensePlate) {
        String query = "SELECT * FROM tickets WHERE license_plate = ? AND exit_time IS NULL";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, licensePlate);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Ticket ticket = new Ticket(
                        rs.getInt("id"),
                        rs.getString("license_plate"),
                        rs.getInt("spot_id"),
                        LocalDateTime.parse(rs.getString("entry_time")),
                        null,
                        0.0
                );
                ticket.setCustomerEmail(rs.getString("customer_email"));
                return ticket;
            }
        } catch (SQLException e) {
            System.err.println("Error getting ticket: " + e.getMessage());
        }
        return null;
    }

    public void processExit(int ticketId, int spotId, double fee, LocalDateTime exitTime) {
        String updateTicket = "UPDATE tickets SET exit_time = ?, fee = ? WHERE id = ?";
        String updateSpot = "UPDATE spots SET is_occupied = 0 WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmtTicket = conn.prepareStatement(updateTicket);
             PreparedStatement pstmtSpot = conn.prepareStatement(updateSpot)) {
            
            conn.setAutoCommit(false); 

            pstmtTicket.setString(1, exitTime.toString());
            pstmtTicket.setDouble(2, fee);
            pstmtTicket.setInt(3, ticketId);
            pstmtTicket.executeUpdate();

            pstmtSpot.setInt(1, spotId);
            pstmtSpot.executeUpdate();

            conn.commit(); 
        } catch (SQLException e) {
            System.err.println("Error processing exit: " + e.getMessage());
        }
    }

    public double getTotalRevenue() {
        double total = 0;
        String query = "SELECT SUM(fee) FROM tickets WHERE fee IS NOT NULL";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
             if (rs.next()) {
                 total = rs.getDouble(1);
             }
        } catch (SQLException e) {
             System.err.println("Error fetching revenue: " + e.getMessage());
        }
        return total;
    }
}
