import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public abstract class Vehicle {
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
