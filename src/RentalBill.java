import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class RentalBill {
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
