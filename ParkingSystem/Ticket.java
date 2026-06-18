package ParkingSystem;

import java.time.LocalDateTime;

public class Ticket {
    int ticketId;
    String licensePlate;
    int spotId;
    LocalDateTime entryTime;
    LocalDateTime exitTime;
    double fee;
    String customerEmail;

    public Ticket(int ticketId, String licensePlate, int spotId,
                  LocalDateTime entryTime, LocalDateTime exitTime, double fee) {
        this.ticketId = ticketId;
        this.licensePlate = licensePlate;
        this.spotId = spotId;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.fee = fee;
    }

    public int getTicketId() { return ticketId; }
    public String getLicensePlate() { return licensePlate; }
    public int getSpotId() { return spotId; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public double getFee() { return fee; }

    public void setExitTime(LocalDateTime t) { this.exitTime = t; }
    public void setFee(double f) { this.fee = f; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String e) { this.customerEmail = e; }
}
