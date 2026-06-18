package ParkingSystem;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ParkingLot lot = new ParkingLot();
        int ch;

        while (true) {
            System.out.println("\n--- Parking Management System ---");
            System.out.println("1. Admin Login");
            System.out.println("2. Park Car");
            System.out.println("3. Park Bike");
            System.out.println("4. Unpark Vehicle");
            System.out.println("5. View Spots");
            System.out.println("6. Exit");
            System.out.print("Choice: ");

            try {
                ch = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
                continue;
            }

            switch (ch) {
                case 1:
                    System.out.print("Enter admin password: ");
                    String pass = sc.nextLine();
                    if (pass.equals("admin123")) {
                        adminMenu(sc, lot);
                    } else {
                        System.out.println("Wrong password!");
                    }
                    break;
                case 2:
                    System.out.print("Enter car plate (eg MH12AB1234): ");
                    String plate = sc.nextLine();
                    System.out.print("Enter email: ");
                    String email = sc.nextLine();
                    // Use Bike parameter type to match parkVehicle signature
                    lot.parkVehicle(new Bike(plate), email);
                    break;
                case 3:
                    System.out.print("Enter bike plate (eg MH12AB1234): ");
                    String plate2 = sc.nextLine();
                    System.out.print("Enter email: ");
                    String email2 = sc.nextLine();
                    lot.parkVehicle(new Bike(plate2), email2);
                    break;
                case 4:
                    System.out.print("Enter plate to unpark: ");
                    String plate3 = sc.nextLine();
                    lot.unparkVehicle(plate3);
                    break;
                case 5:
                    lot.showSpots();
                    break;
                case 6:
                    System.out.println("Bye!");
                    sc.close();
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }

    static void adminMenu(Scanner sc, ParkingLot lot) {
        OUTER:
        while (true) {
            System.out.println("\n-- Admin Menu --");
            System.out.println("1. Revenue & Spots");
            System.out.println("2. Currently Parked");
            System.out.println("3. Parking History");
            System.out.println("4. Logout");
            System.out.print("Choice: ");
            int ch;
            try {
                ch = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid.");
                continue;
            }
            switch (ch) {
                case 1:
                    lot.showSpots();
                    break;
                case 2:
                    lot.showSpots();
                    break;
                case 3:
                    lot.showSpots();
                    break;
                case 4:
                    System.out.println("Logged out.");
                    break OUTER;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }
}
