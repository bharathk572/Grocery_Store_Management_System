package billing;

import java.util.*;
import java.text.SimpleDateFormat;

public class Order {
    private String username;
    private Map<String,Integer> items;
    private double total;
    private Date date;

    public Order(String username, Map<String,Integer> items, double total){
        this.username = username;
        this.items = new LinkedHashMap<>(items);
        this.total = total;
        this.date = new Date();
    }

    public String getUsername(){ return username; }

    public String toFileString(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append(username).append("|").append(sdf.format(date)).append("|");
        boolean first = true;
        for(Map.Entry<String,Integer> e : items.entrySet()){
            if(!first) sb.append(",");
            sb.append(e.getKey()).append(":").append(e.getValue());
            first = false;
        }
        sb.append("|").append(String.format("%.2f", total));
        return sb.toString();
    }

    public static String humanReadable(String fileLine){
        try{
            String[] parts = fileLine.split("\\|");
            if(parts.length < 4) return fileLine;
            String user = parts[0], time = parts[1], items = parts[2], total = parts[3];
            return String.format("User: %s\nTime: %s\nItems: %s\nTotal: ₹%s\n", user, time, items.replace(",", ", "), total);
        } catch(Exception e){ return fileLine; }
    }
}
