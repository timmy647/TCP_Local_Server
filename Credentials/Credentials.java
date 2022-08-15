package Credentials;

import java.io.*;
import java.util.*;

public class Credentials {
    public List<Credential> credentials;

    // @SuppressWarnings("unchecked")
    public Credentials() {
        credentials = new ArrayList<Credential>();
        File file = new File("credentials.txt");
        if (file.exists()) {
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                while (line != null) {
                    String[] word = line.split(" ");
                    if (line.length() >= 2) {
                        String username = word[0];
                        String passwd = word[1];
                        Credential c = new Credential(username, passwd);
                        credentials.add(c);
                        line = reader.readLine();
                    }
                }
                reader.close();
            } catch (Exception e) {

            }
        }
    }

    public void add(String username, String password) throws Exception {
        Credential c = new Credential(username, password);
        credentials.add(c);
        this.save();
    }

    public boolean isUsername(String username) {
        for (Credential c : credentials) {
            if (c.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public String login(String username, String password) {
        for (Credential c : credentials) {
            if (c.getUsername().equals(username)) {
                if (c.getPassowrd().equals(password)) {
                    if (c.isOnline() == false) {
                        c.setOnline(true);
                        return "Command";
                    }
                    return "Already_Loggin";
                }
                return "Invalid_Password";
            }
        }
        return "Invalid_Password";
    }

    public boolean logout(String username) {
        for (Credential c : credentials) {
            if (c.getUsername().equals(username)) {
                c.setOnline(false);
                return true;
            }
        }
        return false;
    }

    public boolean isOnline(String username) {
        for (Credential c : credentials) {
            if (c.getUsername().equals(username)) {
                return c.isOnline();
            }
        }
        return false;
    }

    public void save() throws Exception {
        File file = new File("credentials.txt");
        file.setWritable(true);
        FileWriter writter = new FileWriter(file, false);
        for (Credential c : credentials) {
            writter.write(c.getUsername()+" "+c.getPassowrd()+"\n");
        }
        writter.close();
    }

    public void deleteAll() {
        File file = new File("credentials.txt");
        file.delete();
    }
}
