package user;

import java.io.*;
import java.util.*;

public class UserManager {
    private static final String USER_FILE = "users.txt";
    private List<User> users = new ArrayList<>();

    public UserManager(){
        loadUsers();
    }

    private void loadUsers(){
        users.clear();
        File f = new File(USER_FILE);
        if(!f.exists()) return;
        try(BufferedReader br = new BufferedReader(new FileReader(f))){
            String ln;
            while((ln = br.readLine()) != null){
                String[] parts = ln.split(",");
                if(parts.length < 3) continue;
                String uname = parts[0];
                String pwd = parts[1];
                String role = parts[2];
                if(role.equalsIgnoreCase("ADMIN")) users.add(new Admin(uname,pwd));
                else users.add(new Consumer(uname,pwd));
            }
        } catch(IOException e){ e.printStackTrace(); }
    }

    public boolean register(String username, String password, String role){
        if(getUser(username) != null) return false;
        try(PrintWriter pw = new PrintWriter(new FileWriter(USER_FILE, true))){
            pw.println(username + "," + password + "," + role);
        } catch(IOException e){ e.printStackTrace(); return false; }
        loadUsers();
        return true;
    }

    public User login(String username, String password){
        for(User u: users) if(u.getUsername().equals(username) && u.checkPassword(password)) return u;
        return null;
    }

    public User getUser(String username){
        for(User u: users) if(u.getUsername().equals(username)) return u;
        return null;
    }

    public List<User> getAllUsers(){ return Collections.unmodifiableList(users); }
}
