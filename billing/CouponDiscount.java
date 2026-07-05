package billing;

public class CouponDiscount implements DiscountPolicy {
    private double percent;
    public CouponDiscount(double percent){ this.percent = percent; }
    @Override
    public double applyDiscount(double total){
        return total - (total * percent / 100.0);
    }
}
