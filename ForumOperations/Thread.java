package ForumOperations;

import java.io.*;
import java.util.*;

public class Thread {
    private String creator;
    private String title;
    private int num_message;
    private List<Message> messages;

    public Thread(String creator, String title) {
        this.creator = creator;
        this.title = title;
        this.num_message = 0;
        messages = new ArrayList<Message>();
    }

    public void post_message(String author, String content) {
        Message message = new Message(++num_message, author, content);
        messages.add(message);
    }

    public String getCreator() {
        return creator;
    }

    public String getTitle() {
        return title;
    }

    public int getNum_message() {
        return num_message;
    }

    public void setNum_message(int num_message) {
        this.num_message = num_message;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void SetNumMessages(int num_message) {
        this.num_message = num_message;
    }
}
