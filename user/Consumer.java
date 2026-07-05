package user;

import billing.Basket;

public class Consumer extends User {
    private Basket basket;
    public Consumer(String username, String password){
        super(username,password);
        this.basket = new Basket();
    }
    public Basket getBasket(){ return basket; }
    @Override public boolean isAdmin(){ return false; }
}
