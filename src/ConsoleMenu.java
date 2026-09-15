import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

public final class ConsoleMenu {
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
