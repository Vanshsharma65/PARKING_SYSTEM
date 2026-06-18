package ParkingSystem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParkingLot {

    DatabaseManager db = new DatabaseManager();

    public boolean isValidPlate(String plate) {
        String regex = "^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$";
        return Pattern.matches(regex, plate);
    }

   
    public Ticket parkVehicle(String plate, String type, int spotId, String email) {
        plate = plate.toUpperCase().replaceAll("\\s+", "");

        if (!isValidPlate(plate)) return null;

        if (db.getActiveTicket(plate) != null) return null;

        Vehicle v = type.equalsIgnoreCase("Car") ? new Car(plate) : new Bike(plate);
        
        int tid = db.parkVehicle(v, spotId, email);
        
        if (tid != -1) {
            return db.getActiveTicket(plate);
        }
        return null;
    }

   
    public Ticket unparkVehicle(String plate) {
        plate = plate.toUpperCase().replaceAll("\\s+", "");
        Ticket t = db.getActiveTicket(plate);

        if (t == null) return null;

        LocalDateTime exitTime = LocalDateTime.now();
        Duration d = Duration.between(t.getEntryTime(), exitTime);
        long mins = d.toMinutes();

        double fee;
        if (mins < 60) {
            fee = 10.0;
        } else {
            long hrs = (long) Math.ceil(mins / 60.0);
            fee = 20.0 * hrs;
        }

        db.processExit(t.getTicketId(), t.getSpotId(), fee, exitTime);
        
        t.setExitTime(exitTime);
        t.setFee(fee);

        if (t.getCustomerEmail() != null && !t.getCustomerEmail().isEmpty()) {
            String durationStr = d.toHours() + "hr " + (mins % 60) + "min";
            EmailService.sendReceipt(t.getCustomerEmail(), plate, t.getEntryTime().toString(),
                    exitTime.toString(), durationStr, "Calculated Fee", t.getTicketId(), fee);
        }

        return t;
    }

    
    public List<Ticket> getUserHistory(String email) {
        return db.getAllTicketsHistory().stream()
                .filter(t -> email.equalsIgnoreCase(t.getCustomerEmail()))
                .collect(Collectors.toList());
    }

   
    public List<ParkingSpot> getGridData() {
        return db.getAllSpots();
    }

    
    public double getRevenue() {
        return db.getTotalRevenue();
    }


    public void showSpots() {
        throw new UnsupportedOperationException("Unimplemented method 'showSpots'");
    }


    public void parkVehicle(Bike bike, String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parkVehicle'");
    }
}