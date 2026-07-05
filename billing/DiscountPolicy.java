package billing;

public interface DiscountPolicy {
    double applyDiscount(double total);
}
