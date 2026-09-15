import java.math.BigDecimal;

public final class Bike extends Vehicle {
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
