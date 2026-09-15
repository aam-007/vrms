import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RentalCompany {
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
