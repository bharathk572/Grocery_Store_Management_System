package storage;

import product.Product;
import billing.Order;

import java.io.*;
import java.util.*;

public class FileStorage {
    private static final String PRODUCTS_FILE = "products.txt";
    private static final String ORDERS_FILE = "orders.txt";
    private static final String CATEGORIES_FILE = "categories.txt";
    private static final String PRICE_HISTORY_FILE = "price_history.txt";

    public static List<Product> loadProducts(){
        List<Product> list = new ArrayList<>();
        File f = new File(PRODUCTS_FILE);
        if(!f.exists()) return list;
        try(BufferedReader br = new BufferedReader(new FileReader(f))){
            String line;
            while((line = br.readLine()) != null){
                Product p = Product.fromString(line);
                if(p != null) list.add(p);
            }
        } catch(IOException e){ e.printStackTrace(); }
        return list;
    }

    public static void saveProducts(List<Product> products){
        try(PrintWriter pw = new PrintWriter(new FileWriter(PRODUCTS_FILE))){
            for(Product p: products) pw.println(p.toString());
        } catch(IOException e){ e.printStackTrace(); }
    }

    public static void saveOrder(Order order){
        try(PrintWriter pw = new PrintWriter(new FileWriter(ORDERS_FILE, true))){
            pw.println(order.toFileString());
        } catch(IOException e){ e.printStackTrace(); }
        // also save per-user file
        try{
            String userFile = "orders_" + order.getUsername() + ".txt";
            try(PrintWriter pw2 = new PrintWriter(new FileWriter(userFile, true))){
                pw2.println(order.toFileString());
            }
        } catch(Exception ex){ ex.printStackTrace(); }
    }

    public static List<String> loadOrdersForUser(String username){
        List<String> out = new ArrayList<>();
        String file = "orders_" + username + ".txt";
        File f = new File(file);
        if(!f.exists()) return out;
        try(BufferedReader br = new BufferedReader(new FileReader(f))){
            String line;
            while((line = br.readLine())!=null) out.add(line);
        } catch(IOException e){ e.printStackTrace(); }
        return out;
    }

    public static List<String> loadCategories(){
        List<String> list = new ArrayList<>();
        File f = new File(CATEGORIES_FILE);
        if(!f.exists()) return list;
        try(BufferedReader br = new BufferedReader(new FileReader(f))){
            String line;
            while((line = br.readLine()) != null){
                if(!line.trim().isEmpty()) list.add(line.trim());
            }
        } catch(IOException e){ e.printStackTrace(); }
        return list;
    }

    public static void saveCategories(List<String> categories){
        try(PrintWriter pw = new PrintWriter(new FileWriter(CATEGORIES_FILE))){
            for(String c: categories) pw.println(c);
        } catch(IOException e){ e.printStackTrace(); }
    }

    public static void appendPriceHistory(String historyLine){
        try(PrintWriter pw = new PrintWriter(new FileWriter(PRICE_HISTORY_FILE, true))){
            pw.println(historyLine);
        } catch(IOException e){ e.printStackTrace(); }
    }

    public static Map<String, ReportEntry> generateSalesReport(List<Product> products){
        Map<String, ReportEntry> report = new HashMap<>();
        Map<String, Product> prodMap = new HashMap<>();
        for(Product p: products) prodMap.put(p.getName(), p);

        File f = new File(ORDERS_FILE);
        if(!f.exists()) return report;
        try(BufferedReader br = new BufferedReader(new FileReader(f))){
            String line;
            while((line = br.readLine())!=null){
                String[] parts = line.split("\\|");
                if(parts.length < 4) continue;
                String itemsPart = parts[2];
                String[] items = itemsPart.split(",");
                for(String item : items){
                    String[] pq = item.split(":");
                    if(pq.length < 2) continue;
                    String prodName = pq[0];
                    int qty = Integer.parseInt(pq[1]);
                    Product prod = prodMap.get(prodName);
                    String cat = (prod!=null) ? prod.getCategory() : "Unknown";
                    double price = (prod!=null) ? prod.getPricePerUnit() : 0.0;
                    ReportEntry re = report.getOrDefault(cat, new ReportEntry());
                    re.qtySold += qty;
                    re.revenue += price * qty;
                    report.put(cat, re);
                }
            }
        } catch(IOException e){ e.printStackTrace(); }
        return report;
    }

    public static class ReportEntry {
        public int qtySold = 0;
        public double revenue = 0.0;
    }
}
