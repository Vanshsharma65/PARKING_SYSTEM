package ParkingSystem;

public class ParkingSpot {
    int spotId;
    String type;
    boolean isOccupied;

    public ParkingSpot(int spotId, String type, boolean isOccupied) {
        this.spotId = spotId;
        this.type = type;
        this.isOccupied = isOccupied;
    }

    public int getSpotId() { return spotId; }
    public String getType() { return type; }
    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean val) { this.isOccupied = val; }

    public String toString() {
        return "Spot " + spotId + " | " + type + " | " + (isOccupied ? "Occupied" : "Free");
    }
}
