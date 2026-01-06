package org.example.library;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.stream.Collectors;


public class Book {
    String title;
    String author;
    String status;
    String dueDate;

    private Queue<Borrower> holdQueue = new LinkedList<>();
    private Borrower currentHolder;

    Book(String title, String author){
        this.title = title;
        this.author = author;
        this.status = "Available";
        this.dueDate = null;
    }
    public String getTitle(){
        return title;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Queue<Borrower> getHoldQueue() {
        return holdQueue;
    }

    public List<String> getHoldQueueUsernames() {
        return holdQueue.stream()
                .map(Borrower::getUsername)
                .collect(Collectors.toList());
    }

    public Borrower getCurrentHolder() {
        return currentHolder;
    }

    public void setCurrentHolder(Borrower currentHolder) {
        this.currentHolder = currentHolder;
    }
}
