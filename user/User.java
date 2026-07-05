package user;

import java.io.Serializable;

public abstract class User implements Serializable {
    protected String username;
    protected String password;

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername(){ return username; }
    public boolean checkPassword(String pwd){ return password.equals(pwd); }
    public abstract boolean isAdmin();
}
