import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import Credentials.Credentials;
import ForumOperations.Forum;

public class Server implements Runnable {
    private static ServerSocket server;
    private Socket socket;
    private int port;
    private String admin_passwd;
    private Credentials credentials;
    private Forum forum;

    private InputStream inputStream;
    private OutputStream outputStream;
    private PrintStream result;

    private boolean connection;
    private static boolean shutdown;
    private boolean running;

    private String username;

    public Server(Socket socket, int port, String admin_passwd, Credentials credentials, Forum forum) {
        this.socket = socket;
        this.admin_passwd = admin_passwd;
        this.connection = false;
        shutdown = false;
        try {
            this.credentials = credentials;
            this.forum = forum;
        } catch (Exception e) {

        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Please enter correct arguments");
        }
        int port = Integer.parseInt(args[0]);
        String admin_passwd = args[1];
        // Server s = new Server(port, admin_passwd);
        server = new ServerSocket(port);
        System.out.println("Waiting for clients");

        Credentials credentials = new Credentials();
        Forum forum = new Forum();

        List<Server> sockets = new ArrayList<Server>();
        TimerTask checkShutDown = new TimerTask() {
            @Override
            public void run() {
                if (shutdown == true) {
                    try {
                        for (Server s : sockets) {
                            s.server_shutdown();
                        }
                        server.close();
                        System.out.println("Server shutting down");
                        System.exit(1);
                    } catch (Exception e) {
                        
                    }
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(checkShutDown, 100, 100);
        try {
            while (shutdown != true) {
                Socket socket = server.accept();
                // connection = true;
                Server server = new Server(socket, port, admin_passwd, credentials, forum);
                sockets.add(server);
                new Thread(server).start();
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void run() {
        while (shutdown != true || running == true) {

            try {
                // if (this.connection == false) {
                    // // accept connection from connection queue
                    // socket = server.accept();
                    // new Thread(new Server(port, admin_passwd)).start();
                    // System.out.println("Client connected");
                    // this.connection = true;

                // creatge read stream to get input
                inputStream = socket.getInputStream();
                BufferedReader query = new BufferedReader(new InputStreamReader(inputStream));
                
                String line = query.readLine();

                if (line == null) { running = false; }

                // extract information from the query, such as GET /index.html HTTP/1.1
                this.server_response(socket, line, query);

            } catch (Exception e) {
                running = false;
            }
            
        }
    }

    private void server_response(Socket socket, String line, BufferedReader query) throws Exception {
        // extract information from the query
        String[] lines = line.split(" ");
        String method = lines[0];
        
        outputStream = socket.getOutputStream();
        result = new PrintStream(outputStream, true);

        if (method.equals("Username")) {
            if (connection == false) {
                System.out.println("Client connected");
                connection = true;
            }
            this.username = lines[1];
            handle_username(result, this.username);
        } else if (method.equals("New_Password")) {
            String new_password = lines[1];
            handle_new_password(result, new_password);
        } else if (method.equals("Password")) {
            String password = lines[1];
            handle_password(result, password);
        } else if (method.equals("CRT")) {      // Create Thread
            String title = lines[1];
            System.out.println(username+" issued CRT command");
            handle_CRT(result, title);
        } else if (method.equals("LST")) {      // List Threads
            System.out.println(username+" issued LST command");
            handle_LST(result, lines);
        } else if (method.equals("MSG")) {      // Post Message
            System.out.println(username+" issued MSG command");
            handle_MSG(result, lines);
        } else if (method.equals("DLT")) {      // Delete Message
            System.out.println(username+" issued DLT command");
            handle_DLT(result, lines);
        } else if (method.equals("RDT")) {      // Read Message
            System.out.println(username+" issued RDT command");
            handle_RDT(result, lines);
        } else if (method.equals("EDT")) {      // Edit Message
            System.out.println(username+" issued EDT command");
            handle_EDT(result, lines);
        } else if (method.equals("UPD")) {      // Upload File
            System.out.println(username+" issued UPD command");
            handle_UPD(result, lines, query);
        } else if (method.equals("DWN")) {      // Download File
            System.out.println(username+" issued DWN command");
            handle_DWN(result, lines);
        } else if (method.equals("RMV")) {      // Remove Thread
            System.out.println(username+" issued RMV command");
            handle_RMV(result, lines);
        } else if (method.equals("XIT")) {      // Exit
            System.out.println(username+" issued XIT command");
            handle_XIT(result, socket);
        } else if (method.equals("SHT")) {      // Shutdown Server
            System.out.println(username+" issued SHT command");
            handle_SHT(result, lines, socket);
        } else if (method.equals("Check")) {
            handle_CHECK(result, lines);
        }

        
    }

    private void handle_username(PrintStream result, String username) throws Exception {
        if (credentials.isUsername(username)==false) {
            result.println("New_Password");
            System.out.println("New user");
        } else if (credentials.isOnline(username)==true) {
            result.println("Already_Loggin");
            System.out.println(username+" has already logged in");
        } else {
            result.println("Password");
        }
    }

    private void handle_new_password(PrintStream result, String new_password) throws Exception {
        credentials.add(username, new_password);
        if (credentials.login(username, new_password).equals("Command")) {
            result.println("Command");
            System.out.println(username+" successfully logged in");
        } else {
            System.out.println("Fail to login");
        }
    }

    private void handle_password(PrintStream result, String password) {
        String resultant = credentials.login(username, password);
        if (resultant.equals("Command")) {
            result.println("Command");
            System.out.println(username+" successfully logged in");
        } else if (resultant.equals("Invalid_Password")) {
            result.println("Invalid_Password");
            System.out.println("Invalid_Password");
        } else {
            result.println("Already_Loggin");
            System.out.println(username+" has already logged in");
        }
    }

    private void handle_CRT(PrintStream result, String title) {
        if (forum.CRT(username, title)) {
            System.out.println("Thread "+title+" created");
            result.println("CRT Success "+title);
        } else {
            System.out.println("Thread "+title+" existed");
            result.println("CRT Fail "+title);
        }
    }

    private void handle_LST(PrintStream result, String[] lines) {
        String list = forum.LST();
        if (list == null) {
            result.println("LST Fail");
        } else {
            result.println("LST Success "+list);
        }
    }

    private void handle_MSG(PrintStream result, String[] lines) {
        String title = lines[1];
        StringBuffer sb = new StringBuffer();
        for (int i=2; i<lines.length; i++) {
            sb.append(lines[i]);
            if (i != lines.length-1) { sb.append(" "); }
        }
        String message = sb.toString();
        if (forum.MSG(username, title, message)) {
            System.out.println("Message posted to "+title+" thread");
            result.println("MSG Success "+title);
        } else {
            System.out.println("Incorrect thread specified");
            result.println("MSG Fail "+title);
        }
    }

    private void handle_DLT(PrintStream result, String[] lines) {
        StringBuffer sb = new StringBuffer();
        for (int i=3; i<lines.length; i++) {
            sb.append(lines[i]);
            if (i != lines.length-1) { sb.append(" "); }
        }
        String message = sb.toString();
        String resultant = forum.DLT(lines[1], Integer.parseInt(lines[2]), username);
        if (resultant.split(" ")[1].equals("Success")) {
            System.out.println("Message has been deleted");
        } else {
            System.out.println("Message cannot be deleted");
        }
        result.println(resultant);
    }

    private void handle_RDT(PrintStream result, String[] lines) {
        String title = lines[1];
        String[] messages = forum.RDT(title);
        if (messages==null) {
            System.out.println("Incorrect thread specified");
            result.println("RDT Fail "+title);
        } else if (messages.length==0) {
            System.out.println("Thread "+title+" read");
            result.println("RDT Empty "+title);
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("RDT Success "+title+"\n");
            for (String m : messages) {
                sb.append(m);
                sb.append("\n");
            }
            sb.append("\n");
            String send = sb.toString();
            System.out.println("Thread "+title+" read");
            result.print(send);
        }
    }

    private void handle_EDT(PrintStream result, String[] lines) {
        StringBuffer sb = new StringBuffer();
        for (int i=3; i<lines.length; i++) {
            sb.append(lines[i]);
            if (i != lines.length-1) { sb.append(" "); }
        }
        String message = sb.toString();
        String resultant = forum.EDT(lines[1], Integer.parseInt(lines[2]), username, message);
        if (resultant.split(" ")[1].equals("Success")) {
            System.out.println("Message has been edited");
        } else {
            System.out.println("Message cannot be edited");
        }
        result.println(resultant);
    }

    private void handle_RMV(PrintStream result, String[] lines) {
        String resultant = forum.RMV(lines[1], username);
        if (resultant.split(" ")[1].equals("Success")) {
            System.out.println("Thread "+lines[1]+" has been removed");
        } else {
            System.out.println("Thread "+lines[1]+" cannot be removed");
        }
        result.println(resultant);
    }

    private void handle_XIT(PrintStream result, Socket socket) throws IOException {
        result.println("XIT Success");
        result.close();
        socket.close();
        connection = false;
        credentials.logout(username);
        System.out.println(username+" exited");
    }

    private void handle_SHT(PrintStream result, String[] lines, Socket socket) throws IOException {
        if (lines[1].equals(admin_passwd)) {
            shutdown = true;
            System.out.println("Server shutting down");
            server_shutdown();
        } else {
            System.out.println("Incorrect password");
            result.println("SHT Fail");
        }
    }

    private void server_shutdown() throws IOException {
        result.println("SHT Success");
        result.close();
        socket.close();
        forum.deleteAll();
        credentials.deleteAll();
        System.exit(0);
    }

    private void handle_UPD(PrintStream result, String[] lines, BufferedReader query) throws IOException {
        String title = lines[1];
        String filename = lines[2];
        String resultant = forum.UPD(title, filename, username);
        if (resultant.split(" ")[1].equals("Success")) {
            result.println(resultant);
            upload(result, lines, query);
            System.out.println(filename+" uploaded to "+title+" thread");
            result.println("OK");
        } else if (resultant.equals("UPD Fail File")) {
            result.println(resultant);
            System.out.println("File already exits in the Thread");
        } else if (resultant.equals("UPD Fail Thread")) {
            result.println(resultant);
            System.out.println("Incorrect thread specified");
        }
    }

    public void upload(PrintStream result, String[] lines, BufferedReader query) throws IOException {
        String title = lines[1];
        String filename = lines[2];
        int length = Integer.parseInt(lines[3]);
        DataInputStream dis = new DataInputStream(inputStream);
        FileOutputStream fos = new FileOutputStream(title+"-"+filename);
        byte[] buffer = new byte[4096];
        
        int filesize = length;
        int read = 0;
        int totalRead = 0;
        int remaining = filesize;
        while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining)))>0) {
            totalRead += read;
            remaining -= read;
            fos.write(buffer, 0, read);
        }
        fos.close();
    }

    private void handle_DWN(PrintStream result, String[] lines) {
        String title = lines[1];
        String filename = lines[2];
        String resultant = forum.DWN(title, filename, username);
        if (resultant.equals("DWN Success")) {
            download(result, lines);
            System.out.println(filename+" downloaded from Thread "+title);
        } else if (resultant.split(" ")[2].equals("User")) {
            System.out.println("File cannot be downloaded");
            result.println(resultant);
        } else if (resultant.split(" ")[2].equals("File")) {
            System.out.println("Incorrect file specified");
            result.println(resultant);
        } else if (resultant.split(" ")[2].equals("Thread")) {
            System.out.println("Incorrect thread specified");
            result.println(resultant);
        }
    }

    private void download(PrintStream result, String[] lines) {
        String title = lines[1];
        String filename = lines[2];
        String Fname = title+"-"+filename;
        File file = new File(Fname);
        if (file.exists()) {
            try {
                result.println("DWN Success "+filename+" "+file.length());
                DataInputStream dis = new DataInputStream(inputStream);
                DataOutputStream dos = new DataOutputStream(outputStream);
                FileInputStream fis = new FileInputStream(Fname);
                byte[] buffer = new byte[4096];
                int filesize = (int) file.length();
                int read = 0;
                int totalRead = 0;
                int remaining = filesize;
                while ((fis.read(buffer, totalRead, totalRead+Math.min(buffer.length, remaining))) > 0) {
                    totalRead += read;
                    remaining -= read;
                    dos.write(buffer);
                }
                fis.close();
                outputStream.flush();
            } catch (Exception e) {
                
            }
        } else {
            System.out.println(Fname+" is not found");
        }
    }

    private void handle_CHECK(PrintStream result, String[] lines) {
        result.println("OK");
    }
}