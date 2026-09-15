import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public final class RentalCompanyTest {
    private static int checks;

    private RentalCompanyTest() {
    }

    public static void main(String[] args) {
        testPricing();
        testValidation();
        testTransactions();
        testCollectionOwnership();
        testPolymorphismAndFailure();
        testConsole();
        System.out.println("Passed " + checks + " checks.");
    }

    private static void testPricing() {
        Vehicle[] vehicles = {
            new Bike("B", "City", money("20")),
            new Car("C", "Compact", money("50")),
            new Truck("T", "Cargo", money("120"))
        };
        String[] threeDayCosts = {"60.00", "195.00", "450.00"};
        for (int index = 0; index < vehicles.length; index++) {
            Vehicle vehicle = vehicles[index];
            equalMoney(threeDayCosts[index], vehicle.calculateRentalCost(3));
            expect(IllegalArgumentException.class, () -> vehicle.calculateRentalCost(0));
            expect(IllegalArgumentException.class, () -> vehicle.calculateRentalCost(-1));
            RentalCompany company = new RentalCompany();
            company.addVehicle(vehicle);
            RentalBill bill = company.rentVehicle(vehicle.getId(), 3);
            equalMoney(threeDayCosts[index], bill.getTotal());
            equalMoney("0.00", bill.getDiscount());
        }
        RentalCompany company = new RentalCompany();
        company.addVehicle(new Bike("B", "City", money("20")));
        RentalBill sevenDays = company.rentVehicle("B", 7);
        equalMoney("140.00", sevenDays.getSubtotal());
        equalMoney("0.00", sevenDays.getDiscount());
        equalMoney("140.00", sevenDays.getTotal());
        company.returnVehicle("B");
        RentalBill eightDays = company.rentVehicle("B", 8);
        equalMoney("160.00", eightDays.getSubtotal());
        equalMoney("16.00", eightDays.getDiscount());
        equalMoney("144.00", eightDays.getTotal());
        equalMoney("284.00", company.getTotalRevenue());

        Vehicle fractional = new Bike("F", "Fractional", money("1.005"));
        equalMoney("1.01", fractional.getDailyRate());
        company.addVehicle(fractional);
        RentalBill rounded = company.rentVehicle("F", 8);
        equalMoney("8.08", rounded.getSubtotal());
        equalMoney("0.81", rounded.getDiscount());
        equalMoney("7.27", rounded.getTotal());
        company.addVehicle(new Bike("ZERO", "Free", money("0")));
        equalMoney("0.00", company.rentVehicle("ZERO", 8).getTotal());
        equalMoney("42949672940.00", vehicles[0].calculateRentalCost(Integer.MAX_VALUE));
    }

    private static void testValidation() {
        expect(IllegalArgumentException.class, () -> new Bike(null, "Model", money("1")));
        expect(IllegalArgumentException.class, () -> new Bike(" \t", "Model", money("1")));
        expect(IllegalArgumentException.class, () -> new Bike("B", null, money("1")));
        expect(IllegalArgumentException.class, () -> new Bike("B", "\u2003", money("1")));
        expect(IllegalArgumentException.class, () -> new Bike("B", "Model", null));
        expect(IllegalArgumentException.class, () -> new Bike("B", "Model", money("-0.001")));
        Vehicle bike = new Bike(" b-1 ", " City ", money("20"));
        check("B-1".equals(bike.getId()), "ID is normalized");
        check("City".equals(bike.getModel()), "Model is stripped");
        check(bike.isAvailable(), "New vehicles are available");
        RentalCompany company = new RentalCompany();
        expect(IllegalArgumentException.class, () -> company.addVehicle(null));
        company.addVehicle(bike);
        expect(IllegalArgumentException.class,
                () -> company.addVehicle(new Car("b-1", "Other", money("50"))));
        check(company.findVehicleById(" b-1 ").orElseThrow() == bike, "Consistent ID matching");
        check(!company.findVehicleById("missing").isPresent(), "Absent search is Optional.empty");
        expect(IllegalArgumentException.class, () -> company.findVehicleById(null));
        expect(IllegalArgumentException.class, () -> company.findVehicleById(" "));
        expect(IllegalArgumentException.class, () -> company.rentVehicle(null, 1));
        expect(IllegalArgumentException.class, () -> company.returnVehicle(" "));
        Vehicle rented = new Bike("RENTED", "City", money("20"));
        rented.rent();
        expect(IllegalStateException.class, () -> company.addVehicle(rented));
        check(company.listAvailableVehicles().size() == 1, "Rejected additions leave fleet intact");
    }

    private static void testTransactions() {
        RentalCompany company = new RentalCompany();
        Vehicle car = new Car("C", "Compact", money("50"));
        company.addVehicle(car);
        expect(IllegalArgumentException.class, () -> company.rentVehicle("missing", 3));
        expect(IllegalArgumentException.class, () -> company.rentVehicle("C", 0));
        expect(IllegalArgumentException.class, () -> company.rentVehicle("C", -3));
        equalMoney("0.00", company.getTotalRevenue());
        check(car.isAvailable(), "Failed rentals leave vehicle available");
        RentalBill bill = company.rentVehicle("C", 3);
        check(!car.isAvailable(), "Successful rental changes availability");
        check(company.listAvailableVehicles().isEmpty(), "Rented vehicle excluded");
        equalMoney("195.00", company.getTotalRevenue());
        expect(IllegalStateException.class, () -> company.rentVehicle("C", 3));
        equalMoney("195.00", company.getTotalRevenue());
        check(!car.isAvailable(), "Duplicate rental leaves state unchanged");
        company.returnVehicle("C");
        check(car.isAvailable(), "Return restores availability");
        check(company.listAvailableVehicles().size() == 1, "Returned vehicle reappears");
        equalMoney("195.00", company.getTotalRevenue());
        expect(IllegalStateException.class, () -> company.returnVehicle("C"));
        expect(IllegalArgumentException.class, () -> company.returnVehicle("missing"));
        equalMoney("195.00", company.getTotalRevenue());
        check(car.isAvailable(), "Failed return leaves state unchanged");
        check("C".equals(bill.getVehicleId()), "Bill snapshots identity");
        check("Compact".equals(bill.getVehicleModel()), "Bill snapshots model");
        check("Car".equals(bill.getVehicleType()), "Bill snapshots type");
        check(bill.getDays() == 3, "Bill snapshots duration");
        equalMoney("195.00", bill.getTotal());
        company.rentVehicle("C", 8);
        equalMoney("663.00", company.getTotalRevenue());
        equalMoney("195.00", bill.getTotal());
        expect(IllegalArgumentException.class,
                () -> new RentalBill(car, 1, money("1"), money("2")));
        expect(IllegalArgumentException.class,
                () -> new RentalBill(car, 1, money("-1"), money("0")));
        expect(ArithmeticException.class,
                () -> new RentalBill(car, 1, money("1.001"), money("0")));
    }

    private static void testCollectionOwnership() {
        RentalCompany company = new RentalCompany();
        Vehicle car = new Car("C", "Compact", money("50"));
        Vehicle bike = new Bike("B", "City", money("20"));
        company.addVehicle(car);
        company.addVehicle(bike);
        List<Vehicle> listed = company.listAvailableVehicles();
        listed.sort(Comparator.comparing(Vehicle::getDailyRate));
        check(listed.get(0) == bike, "Derived results can be sorted");
        check(company.listAvailableVehicles().get(0) == car, "Sorting preserves fleet order");
        listed.clear();
        check(company.listAvailableVehicles().size() == 2, "Clearing results preserves fleet");
    }

    private static void testPolymorphismAndFailure() {
        RentalCompany company = new RentalCompany();
        Vehicle special = new Vehicle("SPECIAL", "Custom pricing", money("1")) {
            @Override
            public String getType() {
                return "Special";
            }

            @Override
            public BigDecimal calculateRentalCost(int days) {
                validateDays(days);
                return new BigDecimal("10.00").multiply(BigDecimal.valueOf(days));
            }
        };
        company.addVehicle(special);
        equalMoney("72.00", company.rentVehicle("SPECIAL", 8).getTotal());
        Vehicle broken = new Vehicle("BROKEN", "Invalid pricing", money("1")) {
            @Override
            public String getType() {
                return "Broken";
            }

            @Override
            public BigDecimal calculateRentalCost(int days) {
                return new BigDecimal("1.001");
            }
        };
        company.addVehicle(broken);
        expect(ArithmeticException.class, () -> company.rentVehicle("BROKEN", 3));
        check(broken.isAvailable(), "Bill validation failure does not rent vehicle");
        equalMoney("72.00", company.getTotalRevenue());
    }

    private static void testConsole() {
        RentalCompany company = new RentalCompany();
        company.addVehicle(new Car("C", "Compact", money("50")));
        String transcript = runConsole(company, String.join("\n",
                "letters", "99", "2", "C", "oops", "0", "2", "C", "3",
                "6", "c", "2", "C", "3", "3", "C", "3", "C",
                "6", "missing", "6", " ", "4", "0") + "\n");
        check(transcript.contains("Please enter a whole number"), "Invalid integer recovers");
        check(transcript.contains("listed menu option"), "Invalid menu choice recovers");
        check(transcript.contains("greater than zero"), "Non-positive days rejected");
        check(transcript.replaceAll("\\s+", " ").contains("Total: INR 195.00"), "Console prints bill");
        check(transcript.contains("already rented"), "Duplicate rental explained");
        check(transcript.contains("already available"), "Duplicate return explained");
        check(transcript.contains("No vehicle found"), "Missing search explained");
        check(transcript.contains("must not be blank"), "Blank ID explained");
        check(transcript.replaceAll("\\s+", " ").contains("Total revenue: INR 195.00"),
                "Console prints revenue");
        check(transcript.contains("Goodbye."), "Explicit exit works");
        equalMoney("195.00", company.getTotalRevenue());

        String additions = runConsole(company, String.join("\n",
                "5", "2", "B", "City", "bad", "1.005",
                "5", "1", "c", "Duplicate", "30",
                "5", "3", "T", "Cargo", "-1", "1", "0") + "\n");
        check(additions.contains("plain decimal"), "Invalid amount recovers");
        check(additions.contains("Vehicle added: B"), "Console adds vehicle");
        check(additions.contains("already exists"), "Console rejects duplicate ID");
        check(additions.contains("non-negative"), "Console rejects negative rate");
        equalMoney("1.01", company.findVehicleById("B").orElseThrow().getDailyRate());
        check(!company.findVehicleById("T").isPresent(), "Failed addition has no effect");
        String normalizedListing = additions.replaceAll("\\s+", " ");
        check(normalizedListing.indexOf("B Bike") >= 0
                && normalizedListing.indexOf("B Bike") < normalizedListing.indexOf("C Car"),
                "Display sorted");
        for (String partial : new String[] {"", "2\n", "2\nC\n", "5\n2\nB\n"}) {
            String ended = runConsole(company, partial);
            check(ended.contains("Input ended."), "EOF handled at each prompt");
            equalMoney("195.00", company.getTotalRevenue());
            check(company.findVehicleById("C").orElseThrow().isAvailable(), "EOF leaves state");
        }
        String empty = runConsole(new RentalCompany(), "1\n0\n");
        check(empty.contains("No vehicles are currently available."), "Empty fleet explained");
    }

    private static String runConsole(RentalCompany company, String script) {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try (Scanner input = new Scanner(script);
                PrintStream output = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
            new ConsoleMenu(company, input, output).run();
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private static void equalMoney(String expected, BigDecimal actual) {
        check(new BigDecimal(expected).equals(actual), "Expected " + expected + ", got " + actual);
    }

    private static void expect(Class<? extends RuntimeException> type, Runnable operation) {
        try {
            operation.run();
        } catch (RuntimeException exception) {
            check(type.isInstance(exception), "Expected " + type.getSimpleName() + ", got " + exception);
            return;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
        checks++;
    }
}
