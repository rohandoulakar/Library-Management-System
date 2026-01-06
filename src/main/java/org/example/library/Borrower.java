package org.example.library;
import java.util.List;
import java.util.ArrayList;


public class Borrower {
    private String username;
    private String password;
    private List<Book> heldBooks = new ArrayList<>();
    private List<Book> borrowedBooks = new ArrayList<>();


    public Borrower(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void addHold(Book book) { heldBooks.add(book); }
    public List<Book> getHeldBooks() { return heldBooks; }

    public List<Book> getBorrowedBooks() {
        return borrowedBooks;
    }
}
