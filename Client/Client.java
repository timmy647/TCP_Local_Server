import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Client implements Runnable {
    private String IP;
    private int port;
    private String username;
    private boolean exit;

    private Socket clientSocket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private PrintStream result;

    public Client(String IP, String port) {
        this.IP = IP;
        this.port = Integer.parseInt(port);
        this.exit = false;
        this.username = null;
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Please enter correct arguments");
            System.exit(0);
        }
        Client c = new Client(args[0], args[1]);
        c.clientSocket = new Socket(c.IP, c.port);

        // set up check tcp to check server shutdown
        new Thread(new Client(args[0], args[1])).start();
        
        // get input from keyboard
        Scanner sc = new Scanner(System.in); 
        System.out.print("Enter username: ");
        String sentence = sc.nextLine();
        c.username = sentence;
        
        // write to server
        DataOutputStream outToServer = new DataOutputStream(c.clientSocket.getOutputStream());
        outToServer.writeBytes("Username " + sentence + '\n');
    
        while (true) {
            try {
                // create read stream and receive from server
                c.inputStream = c.clientSocket.getInputStream();
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(c.inputStream));
                // String sentenceFromServer = inFromServer.readLine();
                c.handle_response(c.clientSocket, inFromServer);
                
                if (c.exit == true) {
                    c.clientSocket.close();
                    break;
                }
            } catch (Exception e) {
                // System.out.println(e);
                // break;
                c.exit = true;
                System.out.println();
                System.out.println("Goodbye. Server shutting down");
                System.exit(0);
            }
        }
    }

    @Override
    public void run() {
        try {
            Socket clientSocket = new Socket(IP, port);
            while (true) {
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                outToServer.writeBytes("Check \n");
                TimeUnit.SECONDS.sleep(1);
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String sentence = inFromServer.readLine();
                if (!sentence.equals("OK")) {
                    clientSocket.close();
                    System.out.println();
                    System.out.println("Goodbye. Server shutting down");
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            exit = true;
            System.out.println();
            System.out.println("Goodbye. Server shutting down");
            System.exit(0);
        }
    }

    public void handle_response(Socket socket, BufferedReader inFromServer) throws Exception {
        String sentence = inFromServer.readLine();
        
        // System.out.println(sentence);
        // if (sentence == null) { System.exit(0); }

        String[] words = sentence.split(" ");
        String method = words[0];
        outputStream = socket.getOutputStream();
        result = new PrintStream(outputStream, true);

        if (method.equals("New_Password")) {
            Scanner sc = new Scanner(System.in); 
            System.out.print("Enter new password for "+this.username+": ");
            String new_password  = sc.nextLine();
            result.println("New_Password "+new_password);
        } else if (method.equals("Password")) {
            Scanner sc = new Scanner(System.in); 
            System.out.print("Enter password: ");
            String password  = sc.nextLine();
            result.println("Password "+password);
        } else if (method.equals("Command")) {
            handle_command(result);
        } else if (method.equals("Already_Loggin")) {
            System.out.println(username+" has already logged in");
            Scanner sc = new Scanner(System.in); 
            System.out.print("Enter username: ");
            String username  = sc.nextLine();
            result.println("Username "+username);
        } else if (method.equals("Invalid_Password")) {
            System.out.println("Invalid password");
            Scanner sc = new Scanner(System.in); 
            System.out.print("Enter username: ");
            String username  = sc.nextLine();
            result.println("Username "+username);
        } else if (method.equals("CRT")) {      // Create Thread
            handle_CRT(result, words);
        } else if (method.equals("LST")) {      // List Threads
            handle_LST(result, words);
        } else if (method.equals("MSG")) {      // Post Message
            handle_MSG(result, words);
        } else if (method.equals("DLT")) {      // Delete Message
            handle_DLT(result, words);
        } else if (method.equals("RDT")) {      // Read Message
            handle_RDT(result, words, inFromServer);
        } else if (method.equals("EDT")) {      // Edit Message
            handle_EDT(result, words);
        } else if (method.equals("UPD")) {      // Upload File
            handle_UPD(result, words, inFromServer);
        } else if (method.equals("DWN")) {      // Download File
            handle_DWN(result, words, inFromServer);
        } else if (method.equals("RMV")) {      // Remove Thread
            handle_RMV(result, words);
        } else if (method.equals("XIT")) {      // Exit
            handle_XIT(result, words);
        } else if (method.equals("SHT")) {      // Shutdown Server
            handle_SHT(result, words);
        } else if (method.equals("OK")) {

        }
    }

    public void syntaxError(PrintStream result, String method) {
        System.out.println("Incorrect syntax for "+method);
        handle_command(result);
    }

    public void handle_command(PrintStream result) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter one of the following commands: CRT, MSG, DLT, EDT, LST, RDT, UPD, DWN, RMV, XIT, SHT: ");
        String command  = sc.nextLine();
        String[] words = command.split(" ");
        String method = words[0];
        if (method.equals("CRT")) {             // Create Thread
            if (words.length == 2) { result.println(command); }
            else { syntaxError(result, method); }
        } else if (method.equals("LST")) {      // List Threads
            if (words.length == 1) { result.println(command); }
            else { syntaxError(result, method); }
        } else if (method.equals("MSG")) {      // Post Message
            if (words.length > 2) { result.println(command); }
            else { syntaxError(result, method); }
        } else if (method.equals("DLT")) {      // Delete Message
            if (words.length == 3) { result.println(command); }
            else { syntaxError(result, method); }
        } else if (method.equals("RDT")) {      // Read Message
            if (words.length == 2) { result.println(command); }
            else { syntaxError(result, method); }
        } else if (method.equals("EDT")) {      // Edit Message
            if (words.length > 3) { result.println(command); }
            else { syntaxError(result, method); }
        } else if (method.equals("UPD")) {      // Upload File
            if (words.length == 3) { startUpload(words, result); }
            else { syntaxError(result, method); }
        } else if (method.equals("DWN")) {      // Download File
            if (words.length == 3) { startDownload(words, result); }
            else { syntaxError(result, method); }
        } else if (method.equals("RMV")) {      // Remove Thread
            if (words.length == 2) { result.println(command); }
            else { syntaxError(result, method); }
        } else if (method.equals("XIT")) {      // Exit
            if (words.length == 1) { result.println(command); } 
            else { syntaxError(result, method); }
        } else if (method.equals("SHT")) {      // Shutdown Server
            if (words.length == 2) { result.println(command); } 
            else { syntaxError(result, method); }
        } else {
            System.out.println("Invalid command");
            handle_command(result);
        }
    }

    public void handle_CRT(PrintStream result, String[] words) {
        if (words[1].equals("Success")) {
            System.out.println("Thread "+words[2]+" created");
        } else {
            System.out.println("Thread "+words[2]+" exists");
        }
        handle_command(result);
    }

    public void handle_MSG(PrintStream result, String[] words) {
        if (words[1].equals("Success")) {
            System.out.println("Message posted to "+words[2]+" thread");
        } else {
            System.out.println("Thread "+words[2]+" does not exist");
        }
        handle_command(result);
    }
    
    public void handle_DLT(PrintStream result, String[] words) throws IOException {
        if (words[1].equals("Success")) {
            System.out.println("Message has been deleted");
        } else if (words[2].equals("User")) {
            System.out.println("The message belongs to another user and cannot be deleted");
        } else if (words[2].equals("Seq")) {
            System.out.println("Sequence number "+words[3]+" does not exist");
        } else if (words[2].equals("Thread")) {
            System.out.println("Thread "+words[3]+" does not exist");
        }
        handle_command(result);
    }

    public void handle_LST(PrintStream result, String[] words) {
        if (words[1].equals("Success")) {
            System.out.println("The list of active threads:");
            for (int i=2; i<words.length; i++) {
                System.out.println(words[i]);
            }
        } else {
            System.out.println("No threads to list");
        }
        handle_command(result);
    }

    public void handle_RDT(PrintStream result, String[] words, BufferedReader inFromServer) throws IOException {
        String sentence;
        if (words[1].equals("Success")) {
            try {
                while ((sentence = inFromServer.readLine()) != null && !sentence.equals("")) {
                    System.out.println(sentence);
                }
            } catch (Exception e) {

            }
        } else if (words[1].equals("Empty")) {
            System.out.println("Thread "+words[2]+" is empty");
        } else {
            System.out.println("Thread "+words[2]+" does not exist");
        }
        handle_command(result);
    }

    public void handle_EDT(PrintStream result, String[] words) throws IOException {
        if (words[1].equals("Success")) {
            System.out.println("Message has been edited");
        } else if (words[2].equals("User")) {
            System.out.println("The message belongs to another user and cannot be edited");
        } else if (words[2].equals("Seq")) {
            System.out.println("Sequence number "+words[3]+" does not exist");
        } else if (words[2].equals("Thread")) {
            System.out.println("Thread "+words[3]+" does not exist");
        }
        handle_command(result);
    }

    public void handle_RMV(PrintStream result, String[] words) throws IOException {
        if (words[1].equals("Success")) {
            System.out.println("Thread has been removed");
        } else if (words[2].equals("User")) {
            System.out.println("The thread was created by another user and cannot be removed");
        } else if (words[2].equals("Thread")) {
            System.out.println("Thread "+words[3]+" does not exist");
        }
        handle_command(result);
    }

    public void handle_XIT(PrintStream result, String[] words) {
        result.close();
        exit = true;
        System.out.println("Goodbye");
        System.exit(0);
    }

    public void handle_SHT(PrintStream result, String[] words) {
        if (words[1].equals("Success")) {
            result.close();
            exit = true;
            System.out.println("Goodbye. Server shutting down");
            System.exit(0);
        } else {
            System.out.println("Incorrect passowrd");
            handle_command(result);
        }
    }

    // request to upload
    public void startUpload(String[] words, PrintStream result) {
        String title = words[1];
        String filename = words[2];
        File file = new File(filename);
        if (file.exists()) {
            result.println("UPD "+title+" "+filename+" "+file.length());
        } else {
            System.out.println(filename+" is not found");
            handle_command(result);
        }
    }

    // after recieve the upload permission, start to upload bytes to the server
    public void upload(String[] words, PrintStream result) {
        String title = words[1];
        String filename = words[2];
        File file = new File(filename);
        if (file.exists()) {
            try {
                // result.println("UPD "+title+" "+filename+" "+file.length());
                DataOutputStream dos = new DataOutputStream(outputStream);
                FileInputStream fis = new FileInputStream(filename);
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
                System.out.println(e);
                handle_command(result);
            }
        } else {
            System.out.println(filename+" is not found");
            handle_command(result);
        }
    }

    public void handle_UPD(PrintStream result, String[] words, BufferedReader inFromServer) {
        if (words[1].equals("Success")) {
            upload(words, result);
            System.out.println(words[2]+" uploaded to "+words[3]+" thread");
        } else if (words[2].equals("File")) {
            System.out.println("File already exits in the Thread");
        } else if (words[2].equals("Thread")) {
            System.out.println("Incorrect thread specified");
        }
        handle_command(result);
    }

    public void handle_DWN(PrintStream result, String[] words, BufferedReader inFromServer) {
        if (words[1].equals("Success")) {
            result.println("Request File");
            download(result, words);
            System.out.println(words[2]+" successfully downloaded");
        } else if (words[2].equals("User")) {
            System.out.println("The file belongs to another user and cannot be downloaded");
        } else if (words[2].equals("File")) {
            System.out.println(words[3]+" does not exist in the Thread "+words[4]);
        } else if (words[2].equals("Thread")) {
            System.out.println("Incorrect thread specified");
        }
        handle_command(result);
    }

    public void download(PrintStream result, String[] words) {
        try {
            String title = words[1];
            String filename = words[2];
            int filesize = Integer.parseInt(words[3]);
            DataInputStream dis = new DataInputStream(inputStream);
            FileOutputStream fos = new FileOutputStream(filename);
            byte[] buffer = new byte[4096];
            int read = 0;
            int totalRead = 0;
            int remaining = filesize;
            while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining)))>0) {
                totalRead += read;
                remaining -= read;
                fos.write(buffer, 0 ,read);
            }
            fos.close();
        } catch (Exception e) {

        }
    }

    // request to upload
    public void startDownload(String[] words, PrintStream result) {
        String title = words[1];
        String filename = words[2];
        File file = new File(filename);
        if (file.exists()) {
            result.println("DWN "+title+" "+filename+" "+file.length());
        } else {
            System.out.println(filename+" is not found");
            handle_command(result);
        }
    }
}
