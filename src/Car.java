import java.math.BigDecimal;

public final class Car extends Vehicle {
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
