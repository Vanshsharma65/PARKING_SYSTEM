import ParkingSystem.EmailService;

public class TestEmail {
    public static void main(String[] args) {
        System.out.println("Starting email test...");
        EmailService.sendReceipt(
            "test@example.com",
            "TEST1234",
            "2026-03-24T00:00:00",
            "2026-03-24T01:00:00",
            "1 hr 0 min",
            "Flat rate",
            999,
            10.0
        );
        System.out.println("Finished email test.");
    }
}
