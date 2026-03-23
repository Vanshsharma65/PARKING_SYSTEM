package ParkingSystem;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ParkingLot parkingLot = new ParkingLot();

        while (true) {
            System.out.println("\n=== Parking Management System ===");
            System.out.println("1. Admin Login");
            System.out.println("2. Park a Car");
            System.out.println("3. Park a Bike");
            System.out.println("4. Unpark Vehicle");
            System.out.println("5. View All Spots");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                 case 1:
                    System.out.print("Enter Admin Password: ");
                    String password = scanner.nextLine();
                    if (password.equals("admin123")) {
                        adminMenu(scanner, parkingLot);
                    } else {
                        System.out.println("Incorrect Password!");
                    }
                    break;
                case 2:
                    System.out.print("Enter Car License Plate (e.g. MH12AB1234): ");
                    String carPlate = scanner.nextLine();
                    System.out.print("Enter customer email for receipt: ");
                    String carEmail = scanner.nextLine();
                    Vehicle car = new Car(carPlate);
                    parkingLot.parkVehicle(car, carEmail);
                    break;
                case 3:
                    System.out.print("Enter Bike License Plate (e.g. MH12AB1234): ");
                    String bikePlate = scanner.nextLine();
                    System.out.print("Enter customer email for receipt: ");
                    String bikeEmail = scanner.nextLine();
                    Vehicle bike = new Bike(bikePlate);
                    parkingLot.parkVehicle(bike, bikeEmail);
                    break;
                case 4:
                    System.out.print("Enter License Plate to unpark: ");
                    String plateToExit = scanner.nextLine();
                    System.out.print("Was it a Car or Bike?: ");
                    String type = scanner.nextLine();
                    parkingLot.unparkVehicle(plateToExit, type);
                    break;
                case 5:
                    parkingLot.displayAllSpots(false); 
                    break;
                case 6:
                    System.out.println("Exiting System. Goodbye!");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void adminMenu(Scanner scanner, ParkingLot parkingLot) {
        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. System Audit (Revenue & Spots)");
            System.out.println("2. Logout");
            System.out.print("Enter admin choice: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            if (choice == 1) {
                parkingLot.adminDisplayAllTickets();
            } else if (choice == 2) {
                System.out.println("Logging out of Admin Mode.");
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}
