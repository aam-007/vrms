import java.math.BigDecimal;
import java.util.Scanner;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        RentalCompany company = new RentalCompany();
        populateSampleFleet(company);
        try (Scanner input = new Scanner(System.in)) {
            new ConsoleMenu(company, input, System.out).run();
        }
    }

    private static void populateSampleFleet(RentalCompany company) {
        company.addVehicle(new Car("CAR-001", "Aster Compact", new BigDecimal("50.00")));
        company.addVehicle(new Car("CAR-002", "Meridian Sedan", new BigDecimal("75.00")));
        company.addVehicle(new Bike("BIKE-001", "Swift City 125", new BigDecimal("20.00")));
        company.addVehicle(new Bike("BIKE-002", "Trail Explorer 250", new BigDecimal("30.00")));
        company.addVehicle(new Truck("TRUCK-001", "Atlas Cargo", new BigDecimal("120.00")));
        company.addVehicle(new Truck("TRUCK-002", "Titan Hauler", new BigDecimal("160.00")));
    }
}
