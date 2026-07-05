package product;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Product {
    private String name;
    private String category;
    private double pricePerUnit;
    private int stock;
    private List<Double> priceHistory;

    public Product(String name, String category, double pricePerUnit, int stock) {
        this.name = name;
        this.category = category;
        this.pricePerUnit = pricePerUnit;
        this.stock = stock;
        this.priceHistory = new ArrayList<>();
        this.priceHistory.add(pricePerUnit);
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPricePerUnit() { return pricePerUnit; }
    public int getStock() { return stock; }
    public List<Double> getPriceHistory() { return priceHistory; }

    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }

    public void setPricePerUnit(double newPrice) {
        // record to history
        priceHistory.add(newPrice);
        this.pricePerUnit = newPrice;
    }

    public void setStock(int stock) { this.stock = stock; }

    public void reduceStock(int qty) {
        if (qty <= stock) stock -= qty;
    }

    @Override
    public String toString() {
        // persisted format: name,category,price,stock
        return String.format("%s,%s,%.2f,%d",
                name.replace(",", " "), category.replace(",", " "), pricePerUnit, stock);
    }

    public static Product fromString(String line) {
        try {
            String[] p = line.split(",");
            if (p.length < 4) return null;
            String name = p[0];
            String category = p[1];
            double price = Double.parseDouble(p[2]);
            int stock = Integer.parseInt(p[3]);
            Product prod = new Product(name, category, price, stock);
            return prod;
        } catch (Exception e) {
            return null;
        }
    }

    public static String priceHistoryLine(String productName, double oldPrice, double newPrice) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return String.format("%s|%.2f|%.2f|%s", productName.replace(",", " "), oldPrice, newPrice, sdf.format(new Date()));
    }
}
