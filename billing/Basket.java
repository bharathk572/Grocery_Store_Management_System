package billing;

import product.Product;
import java.util.*;

public class Basket {
    private Map<String,Integer> items = new LinkedHashMap<>();
    private Map<String,Product> productMap = new HashMap<>();

    public void addItem(Product p, int qty){
        if(p==null || qty<=0) return;
        productMap.put(p.getName(), p);
        items.put(p.getName(), items.getOrDefault(p.getName(),0) + qty);
    }

    public void removeItem(String productName){
        items.remove(productName);
        productMap.remove(productName);
    }

    public Map<String,Integer> getItems(){ return Collections.unmodifiableMap(items); }

    public double calculateTotal(){
        double t = 0.0;
        for(Map.Entry<String,Integer> e : items.entrySet()){
            Product p = productMap.get(e.getKey());
            if(p != null) t += p.getPricePerUnit() * e.getValue();
        }
        return t;
    }

    public void clear(){
        items.clear();
        productMap.clear();
    }
}
