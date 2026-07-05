package billing;

public class BillCalculator {
    private Basket basket;
    private DiscountPolicy discount;

    public BillCalculator(Basket basket, DiscountPolicy discount){
        this.basket = basket;
        this.discount = discount;
    }

    public double calculateTotal(){
        double total = basket.calculateTotal();
        if(discount != null) return discount.applyDiscount(total);
        return total;
    }
}
