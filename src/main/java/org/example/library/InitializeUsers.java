package org.example.library;

public class InitializeUsers {

    public static void initialize(Library library) {
        library.addBorrower(new Borrower("alice", "pass123"));
        library.addBorrower(new Borrower("bob", "pass456"));
        library.addBorrower(new Borrower("charlie", "pass789"));
    }
}
