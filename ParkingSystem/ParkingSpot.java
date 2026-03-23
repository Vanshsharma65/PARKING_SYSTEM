package ParkingSystem;

public class ParkingSpot {
    private int spotId;
    private String type; // "Car" or "Bike"
    private boolean isOccupied;

    public ParkingSpot(int spotId, String type, boolean isOccupied) {
        this.spotId = spotId;
        this.type = type;
        this.isOccupied = isOccupied;
    }

    public int getSpotId() {
        return spotId;
    }

    public String getType() {
        return type;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean isOccupied) {
        this.isOccupied = isOccupied;
    }

    @Override
    public String toString() {
        return "Spot ID: " + spotId + " | Type: " + type + " | Occupied: " + isOccupied;
    }
}
