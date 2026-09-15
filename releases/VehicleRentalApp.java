// Generated from src/: edit the modular source first.
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

public final class VehicleRentalApp {
    private VehicleRentalApp() {
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

abstract class Vehicle {
    private final String id;
    private final String model;
    private final BigDecimal dailyRate;
    private boolean available = true;

    protected Vehicle(String id, String model, BigDecimal dailyRate) {
        this.id = normalizeId(id);
        this.model = requireText(model, "Model");
        if (dailyRate == null || dailyRate.signum() < 0) {
            throw new IllegalArgumentException("Daily rate must be non-negative.");
        }
        this.dailyRate = dailyRate.setScale(2, RoundingMode.HALF_UP);
    }

    public final String getId() {
        return id;
    }

    public final String getModel() {
        return model;
    }

    public final BigDecimal getDailyRate() {
        return dailyRate;
    }

    public final boolean isAvailable() {
        return available;
    }

    final void rent() {
        if (!available) {
            throw new IllegalStateException("Vehicle " + id + " is already rented.");
        }
        available = false;
    }

    final void returnToFleet() {
        if (available) {
            throw new IllegalStateException("Vehicle " + id + " is already available.");
        }
        available = true;
    }

    public abstract String getType();

    /** Returns a non-negative, two-decimal subtotal for a positive number of days. */
    public abstract BigDecimal calculateRentalCost(int days);

    protected final BigDecimal costWithDailyFee(int days, BigDecimal dailyFee) {
        validateDays(days);
        return dailyRate.add(dailyFee).multiply(BigDecimal.valueOf(days));
    }

    static void validateDays(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Rental duration must be greater than zero.");
        }
    }

    static String normalizeId(String id) {
        return requireText(id, "Vehicle ID").toUpperCase(Locale.ROOT);
    }

    static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank.");
        }
        return value.strip();
    }
}

final class Car extends Vehicle {
    private static final BigDecimal DAILY_INSURANCE_FEE = new BigDecimal("15.00");

    public Car(String id, String model, BigDecimal dailyRate) {
        super(id, model, dailyRate);
    }

    @Override
    public String getType() {
        return "Car";
    }

    @Override
    public BigDecimal calculateRentalCost(int days) {
        return costWithDailyFee(days, DAILY_INSURANCE_FEE);
    }
}

final class Bike extends Vehicle {
    public Bike(String id, String model, BigDecimal dailyRate) {
        super(id, model, dailyRate);
    }

    @Override
    public String getType() {
        return "Bike";
    }

    @Override
    public BigDecimal calculateRentalCost(int days) {
        return costWithDailyFee(days, BigDecimal.ZERO);
    }
}

final class Truck extends Vehicle {
    private static final BigDecimal DAILY_HEAVY_LOAD_SURCHARGE = new BigDecimal("30.00");

    public Truck(String id, String model, BigDecimal dailyRate) {
        super(id, model, dailyRate);
    }

    @Override
    public String getType() {
        return "Truck";
    }

    @Override
    public BigDecimal calculateRentalCost(int days) {
        return costWithDailyFee(days, DAILY_HEAVY_LOAD_SURCHARGE);
    }
}

final class RentalBill {
    private final String vehicleId;
    private final String vehicleModel;
    private final String vehicleType;
    private final int days;
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal total;

    RentalBill(Vehicle vehicle, int days, BigDecimal subtotal, BigDecimal discount) {
        Objects.requireNonNull(vehicle, "Vehicle must not be null.");
        Vehicle.validateDays(days);
        this.vehicleId = vehicle.getId();
        this.vehicleModel = vehicle.getModel();
        this.vehicleType = Vehicle.requireText(vehicle.getType(), "Vehicle type");
        this.days = days;
        this.subtotal = requireMoney(subtotal, "Subtotal");
        this.discount = requireMoney(discount, "Discount");
        if (this.discount.compareTo(this.subtotal) > 0) {
            throw new IllegalArgumentException("Discount must not exceed subtotal.");
        }
        this.total = this.subtotal.subtract(this.discount);
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public int getDays() {
        return days;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    private static BigDecimal requireMoney(BigDecimal amount, String label) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException(label + " must be non-negative.");
        }
        // A bill checks the pricing contract; it never silently rounds a broken subtotal.
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }
}

final class RentalCompany {
    private static final int LONG_RENTAL_THRESHOLD_DAYS = 7;
    private static final BigDecimal LONG_RENTAL_DISCOUNT_RATE = new BigDecimal("0.10");

    private final List<Vehicle> fleet = new ArrayList<>();
    private BigDecimal totalRevenue = new BigDecimal("0.00");

    public void addVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle must not be null.");
        }
        if (findVehicleById(vehicle.getId()).isPresent()) {
            throw new IllegalArgumentException("Vehicle ID " + vehicle.getId() + " already exists.");
        }
        if (!vehicle.isAvailable()) {
            throw new IllegalStateException("Only an available vehicle can join the fleet.");
        }
        fleet.add(vehicle);
    }

    public Optional<Vehicle> findVehicleById(String id) {
        String normalizedId = Vehicle.normalizeId(id);
        for (Vehicle vehicle : fleet) {
            if (vehicle.getId().equals(normalizedId)) {
                return Optional.of(vehicle);
            }
        }
        return Optional.empty();
    }

    public List<Vehicle> listAvailableVehicles() {
        List<Vehicle> availableVehicles = new ArrayList<>();
        for (Vehicle vehicle : fleet) {
            if (vehicle.isAvailable()) {
                availableVehicles.add(vehicle);
            }
        }
        return availableVehicles;
    }

    public RentalBill rentVehicle(String id, int days) {
        Vehicle.validateDays(days);
        Vehicle vehicle = requireVehicle(id);
        if (!vehicle.isAvailable()) {
            throw new IllegalStateException("Vehicle " + vehicle.getId() + " is already rented.");
        }
        BigDecimal subtotal = vehicle.calculateRentalCost(days);
        BigDecimal discount = days > LONG_RENTAL_THRESHOLD_DAYS
                ? subtotal.multiply(LONG_RENTAL_DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : new BigDecimal("0.00");
        // Prepare every fallible calculation before committing either state change.
        RentalBill bill = new RentalBill(vehicle, days, subtotal, discount);
        BigDecimal updatedRevenue = totalRevenue.add(bill.getTotal());
        vehicle.rent();
        totalRevenue = updatedRevenue;
        return bill;
    }

    public void returnVehicle(String id) {
        requireVehicle(id).returnToFleet();
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    private Vehicle requireVehicle(String id) {
        return findVehicleById(id).orElseThrow(
                () -> new IllegalArgumentException("Vehicle not found: " + id.strip()));
    }
}

final class ConsoleMenu {
    private static final String SECTION_RULE = "-".repeat(72);
    private final RentalCompany company;
    private final Scanner input;
    private final PrintStream output;

    public ConsoleMenu(RentalCompany company, Scanner input, PrintStream output) {
        this.company = Objects.requireNonNull(company, "Company must not be null.");
        this.input = Objects.requireNonNull(input, "Input must not be null.");
        this.output = Objects.requireNonNull(output, "Output must not be null.");
    }

    public void run() {
        printSection("VEHICLE RENTAL MANAGEMENT SYSTEM");
        output.println("  All amounts in Indian Rupees (INR).");
        try {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = readInteger("Choose an option: ");
                try {
                    switch (choice) {
                        case 1:
                            showAvailableVehicles();
                            break;
                        case 2:
                            handleRental();
                            break;
                        case 3:
                            handleReturn();
                            break;
                        case 4:
                            showRevenue();
                            break;
                        case 5:
                            handleAddVehicle();
                            break;
                        case 6:
                            handleSearch();
                            break;
                        case 0:
                            running = false;
                            break;
                        default:
                            output.println("  Please choose a listed menu option (0-6).");
                    }
                } catch (IllegalArgumentException | IllegalStateException exception) {
                    output.println("\n  Cannot complete operation: " + exception.getMessage());
                }
            }
        } catch (NoSuchElementException endOfInput) {
            output.println();
            output.println("Input ended. Closing the application.");
        }
        output.println("\n  Goodbye.");
    }

    private void printMenu() {
        output.println();
        output.println("  MENU");
        output.println("  1. View available vehicles      4. View total revenue");
        output.println("  2. Rent vehicle                 5. Add vehicle");
        output.println("  3. Return vehicle               6. Search by vehicle ID");
        output.println("  0. Exit");
        output.println();
    }

    private void showAvailableVehicles() {
        List<Vehicle> vehicles = company.listAvailableVehicles();
        vehicles.sort(Comparator.comparing(Vehicle::getDailyRate).thenComparing(Vehicle::getId));
        printSection("AVAILABLE VEHICLES");
        if (vehicles.isEmpty()) {
            output.println("  No vehicles are currently available.");
            return;
        }
        output.println("  Daily rates in INR, sorted by base rate (lowest first).");
        output.println();
        printVehicleTable(vehicles);
        output.println("\n  " + vehicles.size() + " vehicle(s) available. With fees excludes rental discounts.");
    }

    private void handleRental() {
        printSection("RENT VEHICLE");
        String id = readLine("Vehicle ID: ");
        int days = readInteger("Rental days: ");
        RentalBill bill = company.rentVehicle(id, days);
        printBill(bill);
    }

    private void handleReturn() {
        printSection("RETURN VEHICLE");
        String id = readLine("Vehicle ID: ");
        company.returnVehicle(id);
        output.println("\n  Vehicle returned successfully.");
    }

    private void showRevenue() {
        printSection("REVENUE");
        printDetail("Total revenue", formatMoney(company.getTotalRevenue()));
    }

    private void handleAddVehicle() {
        printSection("ADD VEHICLE");
        output.println("  Vehicle type: 1. Car  2. Bike  3. Truck");
        int type = readInteger("Type: ");
        if (type < 1 || type > 3) {
            output.println("Please choose vehicle type 1, 2, or 3.");
            return;
        }
        String id = readLine("New vehicle ID: ");
        String model = readLine("Model: ");
        BigDecimal rate = readDailyRate();
        Vehicle vehicle;
        switch (type) {
            case 1:
                vehicle = new Car(id, model, rate);
                break;
            case 2:
                vehicle = new Bike(id, model, rate);
                break;
            default:
                vehicle = new Truck(id, model, rate);
        }
        company.addVehicle(vehicle);
        output.println("\n  Vehicle added: " + vehicle.getId());
    }

    private void handleSearch() {
        printSection("SEARCH VEHICLE");
        Optional<Vehicle> result = company.findVehicleById(readLine("Vehicle ID: "));
        if (result.isPresent()) {
            printVehicle(result.get());
        } else {
            output.println("No vehicle found with that ID.");
        }
    }

    private void printVehicle(Vehicle vehicle) {
        output.println();
        printDetail("Vehicle ID", vehicle.getId());
        printDetail("Type", vehicle.getType());
        printDetail("Model", vehicle.getModel());
        printDetail("Base / day", formatMoney(vehicle.getDailyRate()));
        printDetail("With fees / day", formatMoney(vehicle.calculateRentalCost(1)));
        printDetail("Status", vehicle.isAvailable() ? "Available" : "Rented");
    }

    private void printVehicleTable(List<Vehicle> vehicles) {
        String[][] rows = new String[vehicles.size() + 1][];
        rows[0] = new String[] {"ID", "TYPE", "MODEL", "BASE / DAY", "WITH FEES"};
        for (int index = 0; index < vehicles.size(); index++) {
            Vehicle vehicle = vehicles.get(index);
            rows[index + 1] = new String[] {
                vehicle.getId(), vehicle.getType(), vehicle.getModel(),
                formatAmount(vehicle.getDailyRate()), formatAmount(vehicle.calculateRentalCost(1))
            };
        }
        int[] widths = new int[rows[0].length];
        for (String[] row : rows) {
            for (int column = 0; column < row.length; column++) {
                widths[column] = Math.max(widths[column], row[column].length());
            }
        }
        String rowFormat = "  %-" + widths[0] + "s  %-" + widths[1] + "s  %-" + widths[2]
                + "s  %" + widths[3] + "s  %" + widths[4] + "s%n";
        output.printf(Locale.ROOT, rowFormat, (Object[]) rows[0]);
        String[] separators = new String[widths.length];
        for (int column = 0; column < widths.length; column++) {
            separators[column] = "-".repeat(widths[column]);
        }
        output.printf(Locale.ROOT, rowFormat, (Object[]) separators);
        for (int index = 1; index < rows.length; index++) {
            output.printf(Locale.ROOT, rowFormat, (Object[]) rows[index]);
        }
    }

    private void printBill(RentalBill bill) {
        printSection("RENTAL BILL");
        printDetail("Vehicle ID", bill.getVehicleId());
        printDetail("Model", bill.getVehicleModel());
        printDetail("Type", bill.getVehicleType());
        printDetail("Rental days", Integer.toString(bill.getDays()));
        output.println();
        printCharge("Subtotal (with fees)", bill.getSubtotal());
        printCharge("Discount", bill.getDiscount());
        output.println("  " + "-".repeat(44));
        printCharge("Total", bill.getTotal());
        output.println("\n  Rental successful. Vehicle is now rented.");
    }

    private void printSection(String title) {
        output.println("\n  " + title);
        output.println("  " + SECTION_RULE);
    }

    private void printDetail(String label, String value) {
        output.printf(Locale.ROOT, "  %-22s %s%n", label + ":", value);
    }

    private void printCharge(String label, BigDecimal amount) {
        output.printf(Locale.ROOT, "  %-22s %21s%n", label + ":", formatMoney(amount));
    }

    private String formatMoney(BigDecimal amount) {
        return "INR " + formatAmount(amount);
    }

    private String formatAmount(BigDecimal amount) {
        return String.format(Locale.ROOT, "%,.2f", amount);
    }

    private int readInteger(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException exception) {
                output.println("Please enter a whole number within the integer range.");
            }
        }
    }

    private BigDecimal readDailyRate() {
        while (true) {
            String line = readLine("Base daily rate (INR, e.g. 50.00): ");
            if (!line.matches("[+-]?[0-9]+(?:\\.[0-9]+)?")) {
                output.println("Please enter a plain decimal amount, such as 50.00.");
                continue;
            }
            return new BigDecimal(line);
        }
    }

    private String readLine(String prompt) {
        output.print("  " + prompt);
        output.flush();
        return input.nextLine().strip();
    }
}
