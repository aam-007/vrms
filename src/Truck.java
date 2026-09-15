import java.math.BigDecimal;

public final class Truck extends Vehicle {
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
