package ParkingSystem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    String DB_URL = "jdbc:sqlite:parking.db";

    public DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found: " + e.getMessage());
        }
        setupDB();
    }

    void setupDB() {
        try {
            Connection con = DriverManager.getConnection(DB_URL);
            Statement st = con.createStatement();

            st.execute("CREATE TABLE IF NOT EXISTS spots (id INTEGER PRIMARY KEY, type TEXT, is_occupied INTEGER DEFAULT 0)");
            st.execute("CREATE TABLE IF NOT EXISTS tickets (id INTEGER PRIMARY KEY AUTOINCREMENT, license_plate TEXT, spot_id INTEGER, entry_time TEXT, exit_time TEXT, fee REAL, customer_email TEXT)");

            try {
                st.execute("ALTER TABLE tickets ADD COLUMN customer_email TEXT");
            } catch (SQLException e) {}

            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM spots");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Setting up spots...");
                PreparedStatement ps = con.prepareStatement("INSERT INTO spots (id, type, is_occupied) VALUES (?, ?, 0)");
                for (int i = 1; i <= 5; i++) {
                    ps.setInt(1, i);
                    ps.setString(2, "Car");
                    ps.executeUpdate();
                }
                for (int i = 6; i <= 10; i++) {
                    ps.setInt(1, i);
                    ps.setString(2, "Bike");
                    ps.executeUpdate();
                }
            }
            con.close();
        } catch (SQLException e) {
            System.out.println("DB setup failed: " + e.getMessage());
        }
    }

    public List<ParkingSpot> getAvailableSpots(String type) {
        List<ParkingSpot> list = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(DB_URL);
            PreparedStatement ps = con.prepareStatement("SELECT * FROM spots WHERE type=? AND is_occupied=0");
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ParkingSpot(rs.getInt("id"), rs.getString("type"), rs.getInt("is_occupied") == 1));
            }
            con.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return list;
    }

    public List<ParkingSpot> getAllSpots() {
        List<ParkingSpot> list = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(DB_URL);
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM spots");
            while (rs.next()) {
                list.add(new ParkingSpot(rs.getInt("id"), rs.getString("type"), rs.getInt("is_occupied") == 1));
            }
            con.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return list;
    }

    public int parkVehicle(Vehicle v, int spotId, String email) {
        int ticketId = -1;
        try {
            Connection con = DriverManager.getConnection(DB_URL);
            con.setAutoCommit(false);

            PreparedStatement ps1 = con.prepareStatement("UPDATE spots SET is_occupied=1 WHERE id=?");
            ps1.setInt(1, spotId);
            ps1.executeUpdate();

            PreparedStatement ps2 = con.prepareStatement(
                "INSERT INTO tickets (license_plate, spot_id, entry_time, customer_email) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps2.setString(1, v.getLicensePlate());
            ps2.setInt(2, spotId);
            ps2.setString(3, LocalDateTime.now().toString());
            ps2.setString(4, email);
            ps2.executeUpdate();

            ResultSet rs = ps2.getGeneratedKeys();
            if (rs.next()) ticketId = rs.getInt(1);

            con.commit();
            con.close();
        } catch (SQLException e) {
            System.out.println("Park error: " + e.getMessage());
        }
        return ticketId;
    }

    public Ticket getActiveTicket(String plate) {
        try {
            Connection con = DriverManager.getConnection(DB_URL);
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tickets WHERE license_plate=? AND exit_time IS NULL");
            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Ticket t = new Ticket(rs.getInt("id"), rs.getString("license_plate"),
                        rs.getInt("spot_id"), LocalDateTime.parse(rs.getString("entry_time")), null, 0);
                t.setCustomerEmail(rs.getString("customer_email"));
                con.close();
                return t;
            }
            con.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public void processExit(int ticketId, int spotId, double fee, LocalDateTime exitTime) {
        try {
            Connection con = DriverManager.getConnection(DB_URL);
            con.setAutoCommit(false);

            PreparedStatement ps1 = con.prepareStatement("UPDATE tickets SET exit_time=?, fee=? WHERE id=?");
            ps1.setString(1, exitTime.toString());
            ps1.setDouble(2, fee);
            ps1.setInt(3, ticketId);
            ps1.executeUpdate();

            PreparedStatement ps2 = con.prepareStatement("UPDATE spots SET is_occupied=0 WHERE id=?");
            ps2.setInt(1, spotId);
            ps2.executeUpdate();

            con.commit();
            con.close();
        } catch (SQLException e) {
            System.out.println("Exit error: " + e.getMessage());
        }
    }

    public List<Ticket> getActiveTickets() {
        List<Ticket> list = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(DB_URL);
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM tickets WHERE exit_time IS NULL");
            while (rs.next()) {
                Ticket t = new Ticket(rs.getInt("id"), rs.getString("license_plate"),
                        rs.getInt("spot_id"), LocalDateTime.parse(rs.getString("entry_time")), null, 0);
                t.setCustomerEmail(rs.getString("customer_email"));
                list.add(t);
            }
            con.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return list;
    }

    public List<Ticket> getAllTicketsHistory() {
        List<Ticket> list = new ArrayList<>();
        try {
            Connection con = DriverManager.getConnection(DB_URL);
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM tickets ORDER BY id DESC");
            while (rs.next()) {
                LocalDateTime exitTime = rs.getString("exit_time") != null ? LocalDateTime.parse(rs.getString("exit_time")) : null;
                Ticket t = new Ticket(rs.getInt("id"), rs.getString("license_plate"),
                        rs.getInt("spot_id"), LocalDateTime.parse(rs.getString("entry_time")), exitTime, rs.getDouble("fee"));
                t.setCustomerEmail(rs.getString("customer_email"));
                list.add(t);
            }
            con.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return list;
    }

    public double getTotalRevenue() {
        double total = 0;
        try {
            Connection con = DriverManager.getConnection(DB_URL);
            ResultSet rs = con.createStatement().executeQuery("SELECT SUM(fee) FROM tickets WHERE fee IS NOT NULL");
            if (rs.next()) total = rs.getDouble(1);
            con.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return total;
    }
}
