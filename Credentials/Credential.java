package Credentials;

import java.io.*;
import java.util.*;

public class Credential implements Serializable {
    private String username;
    private String password;
    private boolean online;

    public Credential(String username, String password) {
        this.username = username;
        this.password = password;
        this.online = false;
    }

    public String getUsername() {
        return username;
    }

    public String getPassowrd() {
        return password;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    
}
