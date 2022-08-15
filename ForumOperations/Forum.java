package ForumOperations;

import java.io.*;
import java.util.*;

public class Forum {
    
    private List<Thread> threads;

    public Forum() {
        this.threads = new ArrayList<Thread>();
    }

    public void updateFile(Thread thread) {
        try {
            File file = new File(thread.getTitle());
            file.setWritable(true);
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write("creator: "+thread.getCreator()+"\n");
            for (Message m : thread.getMessages()) {
                fileWriter.write(m.getSeq_num()+" "+m.getAuthor()+": "+m.getContent()+"\n");
            }
            fileWriter.close();
        } catch (Exception e) {

        }
    }

    // Create a thread
    public boolean CRT(String creator, String title) {
        for (Thread t : threads) {
            if (t.getTitle().equals(title)) {
                return false;
            }
        }
        Thread new_thread = new Thread(creator, title);
        threads.add(new_thread);
        updateFile(new_thread);
        return true;
    }

    // Post a message
    public boolean MSG(String author, String title, String content) {
        for (Thread t : threads) {
            if (t.getTitle().equals(title)) {
                t.post_message(author, content);
                updateFile(t);
                return true;
            }
        }
        return false;
    }

    // List threads
    public String LST() {
        if (threads.size()==0) { return null; }
        StringBuffer sb = new StringBuffer();
        for (Thread t : threads) {
            sb.append(t.getTitle());
            sb.append(" ");
        }
        String list = sb.toString();
        return list;
    }

    // Read threads
    public String[] RDT(String title) {
        String[] messages;
        for (Thread t : threads) {
            if (t.getTitle().equals(title)) {
                messages = new String[t.getMessages().size()];
                for (int i=0; i<t.getMessages().size(); i++) {
                    Message m = t.getMessages().get(i);
                    if (m.getSeq_num() != -1) {
                        messages[i] = m.getSeq_num()+" "+m.getAuthor()+": "+m.getContent();
                    } else {
                        messages[i] = m.getContent();
                    }   
                }
                return messages;
            }
        }
        return null;
    }

    // Edit message
    public String EDT(String title, int seq, String author, String message) {
        for (Thread t : threads) {
            if (t.getTitle().equals(title)) {
                for (Message m : t.getMessages()) {
                    if (m.getSeq_num()==seq) {
                        if (m.getAuthor().equals(author)) {
                            m.setContent(message);
                            updateFile(t);
                            return "EDT Success";
                        }
                        return "EDT Fail User";
                    }
                }
                return "EDT Fail Seq "+seq;
            }
        }
        return "EDT Fail Thread "+title;
    }

    // Delete Message
    public String DLT(String title, int seq, String author) {
        String resultant;
        for (Thread t : threads) {
            if (t.getTitle().equals(title)) {
                for (Message m : t.getMessages()) {
                    if (m.getSeq_num()==seq) {
                        if (m.getAuthor().equals(author)) {
                            t.getMessages().remove(m);
                            reorder(t.getMessages());
                            updateFile(t);
                            t.setNum_message(t.getNum_message()-1);
                            return "DLT Success";
                        }
                        return "DLT Fail User";
                    }
                }
                return "DLT Fail Seq "+seq;
            }
        }
        return "DLT Fail Thread "+title;
    }

    // reorganize the sequence number of messages
    public void reorder(List<Message> messages) {
        int i = 1;
        for (Message m : messages) {
            if (m.getSeq_num() != -1) {
                m.setSeq_num(i);
            }
            i++;
        }
    }

    public String RMV(String title, String creator) {
        for (Thread t : threads) {
            if (t.getTitle().equals(title)) {
                if (t.getCreator().equals(creator)) {
                    threads.remove(t);
                    updateFile(t);
                    return "RMV Success";
                }
                return "RMV Fail User";
            }
        }
        return "RMV Fail Thread "+title;
    }

    public void deleteAll() {
        File file;
        for (Thread t : threads) {
            List<Message> messages = t.getMessages();
            for (Message m : messages) {
                if (m.getSeq_num() == -1) {
                    file = new File(t.getTitle()+"-"+m.getFileName());
                    file.delete();
                }
            }
            file = new File(t.getTitle());
            file.delete();
        }
    }

    public String UPD(String title, String filename, String creator) throws IOException {
        String Fname = title+"-"+filename;
        for (Thread t : threads) {
            if (t.getTitle().equals(title)) {
                File file = new File(Fname);
                if (file.exists()) {
                    return "UPD Fail File";
                } else {
                    Message m = new Message(-1, creator, 
                        creator+" uploaded file "+filename);
                    m.setFileName(filename);
                    t.getMessages().add(m);
                    return "UPD Success "+filename+" "+title;
                }
            }
        }
        return "UPD Fail Thread";
    }

    public String DWN(String title, String filename, String username) {
        for (Thread t : threads) {
            if (t.getTitle().equals(title)) {
                List<Message> messages = t.getMessages();
                for (Message m : messages) {
                    if (m.getSeq_num()==-1 && m.getFileName().equals(filename)) {
                        return "DWN Success";
                    }
                }
                return "DWN Fail File "+filename+" "+title;
            }
        }
        return "DWN Fail Thread";
    }
}
