package ParkingSystem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

public class ParkingLot {
    private DatabaseManager dbManager;
    private static final double CAR_RATE_PER_HOUR = 20.0;
    private static final double BIKE_RATE_PER_HOUR = 10.0;

    public ParkingLot() {
        this.dbManager = new DatabaseManager();
    }

    public boolean isValidIndianPlate(String licensePlate) {
       
        String regex = "^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$";
        return Pattern.matches(regex, licensePlate);
    }

    public void parkVehicle(Vehicle vehicle, String customerEmail) {
        String formattedPlate = vehicle.getLicensePlate().toUpperCase().replaceAll("\\s+", "");
        
        if (!isValidIndianPlate(formattedPlate)) {
            System.out.println("Invalid License Plate format! Must be a valid Indian format (e.g., MH12AB1234).");
            return;
        }

        
        if (vehicle instanceof Car) {
            vehicle = new Car(formattedPlate);
        } else {
            vehicle = new Bike(formattedPlate);
        }
        List<ParkingSpot> availableSpots = dbManager.getAvailableSpots(vehicle.getType());
        
        if (availableSpots.isEmpty()) {
            System.out.println("Sorry, no available spots for " + vehicle.getType() + "s right now.");
            return;
        }

        ParkingSpot spot = availableSpots.get(0);

     
        int ticketId = dbManager.parkVehicle(vehicle, spot.getSpotId(), customerEmail);
        
        if (ticketId != -1) {
            System.out.println("-------------------------------------");
            System.out.println("Vehicle Parked Successfully!");
            System.out.println("License Plate : " + vehicle.getLicensePlate());
            System.out.println("Spot ID       : " + spot.getSpotId());
            System.out.println("Ticket ID     : " + ticketId);
            System.out.println("Receipt Email : " + customerEmail);
            System.out.println("-------------------------------------");
        }
    }

    public void unparkVehicle(String licensePlate, String vehicleType) {
       
        Ticket ticket = dbManager.getActiveTicket(licensePlate);
        
        if (ticket == null) {
            System.out.println("No active parking session found for " + licensePlate);
            return;
        }

        / 2. Calculate Fee using real timestamps
        LocalDateTime exitTime = LocalDateTime.now();
        Duration duration = Duration.between(ticket.getEntryTime(), exitTime);
        long totalMinutes = duration.toMinutes();

    
        double totalFee;
        long billableHours;
        String billedAs;

        if (totalMinutes < 60) {
            totalFee = 10.0;
            billableHours = 0;
            billedAs = "Flat rate (under 1 hour)";
        } else {
            billableHours = (long) Math.ceil(totalMinutes / 60.0);
            totalFee = 20.0 * billableHours;
            billedAs = billableHours + " hr(s) at Rs. 20/hr (rounded up)";
        }
        dbManager.processExit(ticket.getTicketId(), ticket.getSpotId(), totalFee, exitTime);

     
        long dispHours = duration.toHours();
        long dispMins  = totalMinutes % 60;
        String durationStr = dispHours + " hr " + dispMins + " min";

        System.out.println("-------------------------------------");
        System.out.println("       PARKING RECEIPT");
        System.out.println("-------------------------------------");
        System.out.println("Ticket ID     : " + ticket.getTicketId());
        System.out.println("License Plate : " + licensePlate.toUpperCase());
        System.out.println("Entry Time    : " + ticket.getEntryTime());
        System.out.println("Exit Time     : " + exitTime);
        System.out.printf ("Duration      : %s%n", durationStr);
        System.out.println("Billed As     : " + billedAs);
        System.out.println("Total Fee     : Rs. " + (int) totalFee);
        System.out.println("-------------------------------------");

      
        String email = ticket.getCustomerEmail();
        if (email != null && !email.isEmpty()) {
            System.out.println("Sending receipt to " + email + "...");
            EmailService.sendReceipt(
                email,
                licensePlate.toUpperCase(),
                ticket.getEntryTime().toString(),
                exitTime.toString(),
                durationStr,
                billedAs,
                ticket.getTicketId(),
                totalFee
            );
        }
    }



    public void displayAllSpots(boolean isAdmin) {
        List<ParkingSpot> spots = dbManager.getAllSpots();
        System.out.println("--- Parking Spots Status ---");
        for (ParkingSpot spot : spots) {
            if (isAdmin) {
                System.out.println(spot.toString());
            } else {
             
                System.out.println("Spot Type: " + spot.getType() + " | Occupied: " + spot.isOccupied());
            }
        }
        System.out.println("----------------------------");
    }

    public void adminDisplayAllTickets() {
        double totalRevenue = dbManager.getTotalRevenue();
        System.out.println("\n--- Admin: Complete System Audit ---");
        System.out.println("Total Revenue Collected: Rs. " + totalRevenue);
        System.out.println("------------------------------------");
        displayAllSpots(true);
    }
}
