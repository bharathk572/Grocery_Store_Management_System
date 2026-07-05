package ui;

import product.Product;
import product.Category;
import user.*;
import storage.FileStorage;
import storage.FileStorage.ReportEntry;
import billing.*;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.text.DecimalFormat;
import java.util.List;

public class MainFrame extends JFrame {

    private UserManager userManager;
    private List<Product> products;
    private List<String> categories;

    private User loggedUser;
    private DefaultTableModel productTableModel;
    private JTable productTable;

    // sample coupons
    private static final Map<String, Double> COUPONS = new HashMap<>();
    static {
        COUPONS.put("SAVE10", 10.0);
        COUPONS.put("FESTIVE20", 20.0);
        COUPONS.put("BULK5", 5.0);
    }

    public MainFrame(){
        setTitle("Grocery Store Management System ");
        setSize(1000,700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        userManager = new UserManager();
        products = FileStorage.loadProducts();
        categories = FileStorage.loadCategories();
        if(categories.isEmpty()){
            categories.add("Fruits"); categories.add("Vegetables"); categories.add("Dairy");
            FileStorage.saveCategories(categories);
        }
        // ensure admin exists
        if(userManager.getUser("admin") == null) userManager.register("admin","admin123","ADMIN");

        showWelcome();
        checkLowStockAlerts();
    }

    private void showWelcome(){
        getContentPane().removeAll();
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);

        JLabel title = new JLabel("Grocery Store Management System");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        c.gridx=0; c.gridy=0; c.gridwidth=2;
        p.add(title,c);

        JButton login = new JButton("Login");
        JButton register = new JButton("Register");
        JButton exit = new JButton("Exit");
        c.gridwidth=1;
        c.gridy=1; c.gridx=0; p.add(login,c);
        c.gridy=1; c.gridx=1; p.add(register,c);
        c.gridy=2; c.gridx=0; p.add(exit,c);

        login.addActionListener(e -> showLoginDialog());
        register.addActionListener(e -> showRegisterDialog());
        exit.addActionListener(e -> System.exit(0));

        add(p);
        revalidate(); repaint();
    }

    private void showLoginDialog(){
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        Object[] msg = {"Username:", userField, "Password:", passField};
        int res = JOptionPane.showConfirmDialog(this, msg, "Login", JOptionPane.OK_CANCEL_OPTION);
        if(res == JOptionPane.OK_OPTION){
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            User user = userManager.login(u,p);
            if(user == null){ JOptionPane.showMessageDialog(this,"Invalid credentials."); return; }
            loggedUser = user;
            if(user instanceof Admin) showAdminPanel();
            else showCustomerPanel((Consumer)user);
        }
    }

    private void showRegisterDialog(){
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"CUSTOMER","ADMIN"});
        Object[] msg = {"Username:", userField, "Password:", passField, "Role:", roleBox};
        int res = JOptionPane.showConfirmDialog(this, msg, "Register", JOptionPane.OK_CANCEL_OPTION);
        if(res == JOptionPane.OK_OPTION){
            String u = userField.getText().trim(), p = new String(passField.getPassword()), role = (String)roleBox.getSelectedItem();
            if(u.isEmpty() || p.isEmpty()){ JOptionPane.showMessageDialog(this,"Fill all fields."); return; }
            boolean ok = userManager.register(u,p,role);
            if(ok) JOptionPane.showMessageDialog(this,"Registered. Login now.");
            else JOptionPane.showMessageDialog(this,"Username exists.");
        }
    }

    // ---------------- Admin Panel ----------------
    private void showAdminPanel(){
        getContentPane().removeAll();
        JPanel root = new JPanel(new BorderLayout());

        JLabel header = new JLabel("Admin Panel - Manage Products, Categories, Users & Reports", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 16));
        root.add(header, BorderLayout.NORTH);

        String[] cols = {"Name","Category","Price","Stock"};
        productTableModel = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){ return false; } };
        productTable = new JTable(productTableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshProductTable();

        root.add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        JButton saveBtn = new JButton("Save");
        JButton catsBtn = new JButton("Categories");
        JButton usersBtn = new JButton("Users");
        JButton reportBtn = new JButton("Sales Report");
        JButton logoutBtn = new JButton("Logout");

        buttons.add(addBtn); buttons.add(editBtn); buttons.add(delBtn); buttons.add(saveBtn);
        buttons.add(catsBtn); buttons.add(usersBtn); buttons.add(reportBtn); buttons.add(logoutBtn);

        root.add(buttons, BorderLayout.SOUTH);
        add(root);
        revalidate(); repaint();

        addBtn.addActionListener(e -> adminAddProduct());
        editBtn.addActionListener(e -> adminEditProduct());
        delBtn.addActionListener(e -> adminDeleteProduct());
        saveBtn.addActionListener(e -> { FileStorage.saveProducts(products); FileStorage.saveCategories(categories); JOptionPane.showMessageDialog(this,"Saved."); });
        catsBtn.addActionListener(e -> showCategoryManager());
        usersBtn.addActionListener(e -> showUserManager());
        reportBtn.addActionListener(e -> showSalesReport());
        logoutBtn.addActionListener(e -> { loggedUser = null; showWelcome(); });
    }

    private void refreshProductTable(){
        if(productTableModel == null) return;
        productTableModel.setRowCount(0);
        DecimalFormat df = new DecimalFormat("0.00");
        for(Product p: products) productTableModel.addRow(new Object[]{p.getName(), p.getCategory(), df.format(p.getPricePerUnit()), p.getStock()});
    }

    private void adminAddProduct(){
        JTextField name = new JTextField();
        JComboBox<String> catBox = new JComboBox<>();
        for(String c: categories) catBox.addItem(c);
        JTextField price = new JTextField();
        JTextField stock = new JTextField();
        Object[] msg = {"Name:", name, "Category:", catBox, "Price:", price, "Stock:", stock};
        int res = JOptionPane.showConfirmDialog(this, msg, "Add Product", JOptionPane.OK_CANCEL_OPTION);
        if(res == JOptionPane.OK_OPTION){
            try{
                Product p = new Product(name.getText().trim(), (String)catBox.getSelectedItem(), Double.parseDouble(price.getText().trim()), Integer.parseInt(stock.getText().trim()));
                products.add(p);
                refreshProductTable();
                FileStorage.saveProducts(products);
            } catch(Exception ex){ JOptionPane.showMessageDialog(this,"Invalid input."); }
        }
    }

    private void adminEditProduct(){
        int row = productTable.getSelectedRow();
        if(row < 0){ JOptionPane.showMessageDialog(this,"Select product."); return; }
        Product p = findProductByName(productTable.getValueAt(row,0).toString());
        if(p==null) return;
        double oldPrice = p.getPricePerUnit();
        JTextField name = new JTextField(p.getName());
        JComboBox<String> catBox = new JComboBox<>();
        for(String c: categories) catBox.addItem(c);
        catBox.setSelectedItem(p.getCategory());
        JTextField price = new JTextField(String.valueOf(p.getPricePerUnit()));
        JTextField stock = new JTextField(String.valueOf(p.getStock()));
        Object[] msg = {"Name:", name, "Category:", catBox, "Price:", price, "Stock:", stock};
        int res = JOptionPane.showConfirmDialog(this, msg, "Edit Product", JOptionPane.OK_CANCEL_OPTION);
        if(res == JOptionPane.OK_OPTION){
            try{
                p.setName(name.getText().trim());
                p.setCategory((String)catBox.getSelectedItem());
                double newPrice = Double.parseDouble(price.getText().trim());
                p.setPricePerUnit(newPrice);
                p.setStock(Integer.parseInt(stock.getText().trim()));
                refreshProductTable();
                FileStorage.saveProducts(products);
                if(Math.abs(oldPrice - newPrice) > 0.001) FileStorage.appendPriceHistory(Product.priceHistoryLine(p.getName(), oldPrice, newPrice));
                checkLowStockForProduct(p);
            } catch(Exception ex){ JOptionPane.showMessageDialog(this,"Invalid input."); }
        }
    }

    private void adminDeleteProduct(){
        int row = productTable.getSelectedRow();
        if(row < 0){ JOptionPane.showMessageDialog(this,"Select product."); return; }
        Product p = findProductByName(productTable.getValueAt(row,0).toString());
        if(p==null) return;
        int conf = JOptionPane.showConfirmDialog(this,"Delete "+p.getName()+"?","Confirm",JOptionPane.YES_NO_OPTION);
        if(conf == JOptionPane.YES_OPTION){
            products.remove(p);
            refreshProductTable();
            FileStorage.saveProducts(products);
        }
    }

    // Category manager
    private void showCategoryManager(){
        JDialog dlg = new JDialog(this,"Manage Categories", true);
        dlg.setSize(400,300); dlg.setLocationRelativeTo(this);
        DefaultListModel<String> model = new DefaultListModel<>();
        for(String c: categories) model.addElement(c);
        JList<String> list = new JList<>(model);
        JScrollPane sp = new JScrollPane(list);
        JButton add = new JButton("Add"), edit = new JButton("Edit"), del = new JButton("Delete"), close = new JButton("Close");
        JPanel bp = new JPanel(); bp.add(add); bp.add(edit); bp.add(del); bp.add(close);
        dlg.setLayout(new BorderLayout()); dlg.add(sp, BorderLayout.CENTER); dlg.add(bp, BorderLayout.SOUTH);

        add.addActionListener(e -> {
            String v = JOptionPane.showInputDialog(dlg,"Category name:");
            if(v!=null && !v.trim().isEmpty()){ categories.add(v.trim()); model.addElement(v.trim()); FileStorage.saveCategories(categories); }
        });
        edit.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if(idx<0) { JOptionPane.showMessageDialog(dlg,"Select category."); return; }
            String old = model.get(idx);
            String v = JOptionPane.showInputDialog(dlg,"Edit category:", old);
            if(v!=null && !v.trim().isEmpty()){ categories.set(idx, v.trim()); model.set(idx, v.trim()); FileStorage.saveCategories(categories); }
        });
        del.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if(idx<0) { JOptionPane.showMessageDialog(dlg,"Select category."); return; }
            int conf = JOptionPane.showConfirmDialog(dlg,"Delete "+model.get(idx)+"?","Confirm",JOptionPane.YES_NO_OPTION);
            if(conf==JOptionPane.YES_OPTION){ categories.remove(idx); model.remove(idx); FileStorage.saveCategories(categories); }
        });
        close.addActionListener(e -> dlg.dispose());
        dlg.setVisible(true);
    }

    // Sales report
    private void showSalesReport(){
        Map<String, ReportEntry> report = FileStorage.generateSalesReport(products);
        StringBuilder sb = new StringBuilder();
        if(report.isEmpty()) sb.append("No sales recorded.");
        else {
            sb.append(String.format("%-20s %-10s %-12s\n","Category","QtySold","Revenue"));
            for(Map.Entry<String, ReportEntry> en : report.entrySet()){
                sb.append(String.format("%-20s %-10d ₹%-10.2f\n", en.getKey(), en.getValue().qtySold, en.getValue().revenue));
            }
        }
        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        JOptionPane.showMessageDialog(this,new JScrollPane(area),"Sales Report", JOptionPane.INFORMATION_MESSAGE);
    }

    // User manager
    private void showUserManager(){
        List<User> all = userManager.getAllUsers();
        DefaultListModel<String> lm = new DefaultListModel<>();
        for(User u: all) lm.addElement(u.getUsername() + " (" + (u instanceof Admin ? "ADMIN" : "CUSTOMER") + ")");
        JList<String> list = new JList<>(lm);
        JButton del = new JButton("Delete"), promote = new JButton("Promote"), close = new JButton("Close");
        JPanel p = new JPanel(new BorderLayout()); p.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel b = new JPanel(); b.add(del); b.add(promote); b.add(close); p.add(b, BorderLayout.SOUTH);
        JDialog dlg = new JDialog(this,"User Manager", true); dlg.setSize(400,400); dlg.setLocationRelativeTo(this); dlg.add(p);

        del.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if(idx<0){ JOptionPane.showMessageDialog(dlg,"Select user."); return; }
            String uname = all.get(idx).getUsername();
            if("admin".equals(uname)){ JOptionPane.showMessageDialog(dlg,"Cannot delete admin."); return; }
            try{
                File f = new File("users.txt");
                List<String> lines = new ArrayList<>();
                try(BufferedReader br = new BufferedReader(new java.io.FileReader(f))){
                    String ln; while((ln = br.readLine())!=null) if(!ln.startsWith(uname + ",")) lines.add(ln);
                }
                try(PrintWriter pw = new PrintWriter(new java.io.FileWriter(f))){ for(String ln: lines) pw.println(ln); }
                userManager = new UserManager();
                JOptionPane.showMessageDialog(dlg,"Deleted.");
                dlg.dispose();
            } catch(Exception ex){ JOptionPane.showMessageDialog(dlg,"Error deleting user."); }
        });

        promote.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if(idx<0){ JOptionPane.showMessageDialog(dlg,"Select user."); return; }
            String uname = all.get(idx).getUsername();
            try{
                File f = new File("users.txt");
                List<String> lines = new ArrayList<>();
                try(BufferedReader br = new BufferedReader(new java.io.FileReader(f))){
                    String ln; while((ln = br.readLine())!=null){
                        if(ln.startsWith(uname + ",")){
                            String[] parts = ln.split(",");
                            if(parts.length >= 3) parts[2] = "ADMIN";
                            lines.add(parts[0] + "," + parts[1] + "," + parts[2]);
                        } else lines.add(ln);
                    }
                }
                try(PrintWriter pw = new PrintWriter(new java.io.FileWriter(f))){ for(String ln: lines) pw.println(ln); }
                userManager = new UserManager();
                JOptionPane.showMessageDialog(dlg,"Promoted to Admin.");
                dlg.dispose();
            } catch(Exception ex){ JOptionPane.showMessageDialog(dlg,"Error promoting user."); }
        });

        close.addActionListener(e -> dlg.dispose());
        dlg.setVisible(true);
    }

    // ---------------- Customer Panel ----------------
    private void showCustomerPanel(Consumer consumer){
        getContentPane().removeAll();
        JPanel root = new JPanel(new BorderLayout());
        JLabel hdr = new JLabel("Customer Panel - " + consumer.getUsername(), SwingConstants.CENTER);
        hdr.setFont(new Font("SansSerif", Font.BOLD, 16));
        root.add(hdr, BorderLayout.NORTH);

        String[] cols = {"Name","Category","Price","Stock"};
        DefaultTableModel model = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){ return false; } };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for(Product p: products) model.addRow(new Object[]{p.getName(), p.getCategory(), p.getPricePerUnit(), p.getStock()});

        // filters
        JPanel topFilter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topFilter.add(new JLabel("Search:"));
        JTextField search = new JTextField(12); topFilter.add(search);
        topFilter.add(new JLabel("Category:"));
        JComboBox<String> catBox = new JComboBox<>(); catBox.addItem("All"); for(String c: categories) catBox.addItem(c); topFilter.add(catBox);
        topFilter.add(new JLabel("Max Price:"));
        JTextField maxPrice = new JTextField(6); topFilter.add(maxPrice);
        JButton apply = new JButton("Apply"); topFilter.add(apply);
        root.add(topFilter, BorderLayout.NORTH);

        root.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton addToCart = new JButton("Add to Cart");
        JButton viewCart = new JButton("View/Checkout Cart");
        JButton viewOrders = new JButton("View Orders");
        JButton logout = new JButton("Logout");
        bottom.add(addToCart); bottom.add(viewCart); bottom.add(viewOrders); bottom.add(logout);
        root.add(bottom, BorderLayout.SOUTH);

        add(root);
        revalidate(); repaint();

        apply.addActionListener(e -> {
            String q = search.getText().trim().toLowerCase();
            String sel = (String)catBox.getSelectedItem();
            double mp = Double.MAX_VALUE;
            if(!maxPrice.getText().trim().isEmpty()){
                try{ mp = Double.parseDouble(maxPrice.getText().trim()); } catch(Exception ex){ JOptionPane.showMessageDialog(this,"Invalid price"); return; }
            }
            model.setRowCount(0);
            for(Product p: products){
                boolean catMatch = sel.equals("All") || p.getCategory().equals(sel);
                boolean nameMatch = p.getName().toLowerCase().contains(q);
                boolean priceMatch = p.getPricePerUnit() <= mp;
                if(catMatch && nameMatch && priceMatch) model.addRow(new Object[]{p.getName(), p.getCategory(), p.getPricePerUnit(), p.getStock()});
            }
        });

        addToCart.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row < 0){ JOptionPane.showMessageDialog(this,"Select product."); return; }
            Product p = findProductByName(table.getValueAt(row,0).toString());
            if(p==null){ JOptionPane.showMessageDialog(this,"Product not found."); return; }
            String qtyStr = JOptionPane.showInputDialog(this,"Enter quantity:");
            try{
                int q = Integer.parseInt(qtyStr);
                if(q <= 0){ JOptionPane.showMessageDialog(this,"Invalid qty."); return; }
                if(q > p.getStock()){ JOptionPane.showMessageDialog(this,"Not enough stock."); return; }
                consumer.getBasket().addItem(p,q);
                JOptionPane.showMessageDialog(this,"Added to cart.");
            } catch(Exception ex){ JOptionPane.showMessageDialog(this,"Invalid quantity."); }
        });

        viewCart.addActionListener(e -> {
            Map<String,Integer> items = consumer.getBasket().getItems();
            if(items.isEmpty()){ JOptionPane.showMessageDialog(this,"Cart empty."); return; }
            StringBuilder sb = new StringBuilder();
            double total = 0.0;
            for(Map.Entry<String,Integer> en : items.entrySet()){
                Product p = findProductByName(en.getKey());
                int qty = en.getValue();
                double line = p.getPricePerUnit() * qty;
                sb.append(String.format("%s x %d = ₹%.2f\n", p.getName(), qty, line));
                total += line;
            }
            sb.append("\nTotal: ₹").append(String.format("%.2f", total));
            String coupon = JOptionPane.showInputDialog(this, "Cart:\n" + sb.toString() + "\n\nEnter coupon code (or leave empty):");
            double discountedTotal = total;
            if(coupon != null && !coupon.trim().isEmpty()){
                String code = coupon.trim().toUpperCase();
                if(COUPONS.containsKey(code)){
                    double percent = COUPONS.get(code);
                    discountedTotal = (total * (100.0 - percent)) / 100.0;
                    JOptionPane.showMessageDialog(this, "Coupon applied: " + percent + "% off. New total: ₹" + String.format("%.2f", discountedTotal));
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid coupon. Proceeding without discount.");
                }
            }

            int conf = JOptionPane.showConfirmDialog(this, "Pay ₹" + String.format("%.2f", discountedTotal) + " now?", "Confirm Checkout", JOptionPane.YES_NO_OPTION);
            if(conf == JOptionPane.YES_OPTION){
                for(Map.Entry<String,Integer> en : items.entrySet()){
                    Product p = findProductByName(en.getKey());
                    if(p != null) p.reduceStock(en.getValue());
                }
                FileStorage.saveProducts(products);
                Order order = new Order(consumer.getUsername(), items, discountedTotal);
                FileStorage.saveOrder(order);
                consumer.getBasket().clear();
                JOptionPane.showMessageDialog(this,"Order placed. Total: ₹"+String.format("%.2f",discountedTotal));
                model.setRowCount(0);
                for(Product p: products) model.addRow(new Object[]{p.getName(), p.getCategory(), p.getPricePerUnit(), p.getStock()});
                checkLowStockAlerts();
            }
        });

        viewOrders.addActionListener(e -> {
            List<String> userOrders = FileStorage.loadOrdersForUser(consumer.getUsername());
            if(userOrders.isEmpty()){ JOptionPane.showMessageDialog(this,"No orders yet."); return; }
            StringBuilder sb = new StringBuilder();
            for(String ln: userOrders) sb.append(Order.humanReadable(ln)).append("\n-----------------\n");
            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            JOptionPane.showMessageDialog(this,new JScrollPane(area),"Your Orders", JOptionPane.INFORMATION_MESSAGE);
        });

        logout.addActionListener(e -> { consumer.getBasket().clear(); loggedUser = null; showWelcome(); });
    }

    private Product findProductByName(String name){
        for(Product p: products) if(p.getName().equals(name)) return p;
        return null;
    }

    private void checkLowStockAlerts(){
        StringBuilder sb = new StringBuilder();
        for(Product p: products){
            if(p.getStock() > 0 && p.getStock() < 5){
                sb.append(String.format("%s — only %d left\n", p.getName(), p.getStock()));
            }
        }
        if(sb.length() > 0) JOptionPane.showMessageDialog(this, sb.toString(), "Low Stock Alerts", JOptionPane.WARNING_MESSAGE);
    }

    private void checkLowStockForProduct(Product p){
        if(p.getStock() > 0 && p.getStock() < 5) JOptionPane.showMessageDialog(this, "Low stock: " + p.getName() + " (only " + p.getStock() + " left)", "Low Stock", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
