package ForumOperations;

import java.io.*;
import java.util.*;

public class Message {
    protected int seq_num;
    protected String author;
    protected String content;
    protected String filename;
    
    public Message(int seq_num, String author, String content) {
        this.seq_num = seq_num;
        this.author = author;
        this.content = content;
    }

    public int getSeq_num() {
        return seq_num;
    }

    public void setSeq_num(int seq_num) {
        this.seq_num = seq_num;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFileName(String filename) {
        this.filename = filename;
    }

    public String getFileName() {
        return filename;
    }
}
