package org.example.library;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.Scanner;

public class Library {

    private List<Borrower> borrowers = new ArrayList<>();
    private List<Book> books = new ArrayList<>();
    private Borrower currentBorrower;
    private List<String> notifications = new ArrayList<>();
    private Catalogue catalogue;
    private String loginMessage;


    public void addBorrower(Borrower borrower) {
        borrowers.add(borrower);
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public boolean login(String username, String password) {
        for (Borrower b : borrowers) {
            if (b.getUsername().equals(username) && b.getPassword().equals(password)) {
                currentBorrower = b;
                checkAvailableHeldBooks(b);
                loginMessage = "Login successful.";
                return true;
            }
        }
        loginMessage = "Authentication failed. Please try again.";
        return false;
    }

    private void checkAvailableHeldBooks(Borrower borrower) {
        notifications.clear();
        for (Book held : borrower.getHeldBooks()) {
            if (held.getStatus().equalsIgnoreCase("Available")) {
                notifications.add("Notification for " + borrower.getUsername() + ": Book available - " + held.getTitle());
            }
        }
    }

    public int getBorrowerCount() {
        return borrowers.size();
    }

    public List<String> getNotifications() {
        return notifications;
    }

    public List<Borrower> getBorrowers() {
        return borrowers;
    }

    public Borrower authenticate(String username, String password) {
        for (Borrower b : borrowers) {
            if (b.getUsername().equals(username) && b.getPassword().equals(password)) {
                return b;
            }
        }
        return null;
    }

    public void setCurrentBorrower(Borrower borrower) {
        this.currentBorrower = borrower;
    }

    public Borrower getCurrentBorrower() {
        return currentBorrower;
    }

    public String getLoginMessage() {
        return loginMessage;
    }

    public void logout() {
        currentBorrower = null;
    }

    public List<String> getAvailableOperations() {
        List<String> operations = new ArrayList<>();
        if (currentBorrower != null) {
            operations.add("Borrow Book");
            operations.add("Return Book");
            operations.add("Logout");
        }
        return operations;
    }

    public int getBorrowedBookCount() {
        if (currentBorrower == null) return 0;
        return currentBorrower.getBorrowedBooks().size();
    }

    public List<Book> displayAllBooks() {
        return catalogue.getBooks();
    }

    public void setCatalogue(Catalogue catalogue) {
        this.catalogue = catalogue;
    }

    public boolean borrowBook(Book book) {
        if (currentBorrower == null || book == null) return false;

        if (!verifyBorrowerEligibility(currentBorrower)) {
            return false;
        }

        Book catalogBook = getBook(book.getTitle());
        if (catalogBook == null) return false;

        // Available to Checked Out
        if (catalogBook.getStatus().equalsIgnoreCase("Available")) {
            catalogBook.setStatus("Checked Out");
            catalogBook.setCurrentHolder(currentBorrower);
            currentBorrower.getBorrowedBooks().add(catalogBook);
            assignDueDate(catalogBook);
            return true;
        }

        // On Hold to Checked Out
        if (catalogBook.getStatus().equalsIgnoreCase("On Hold")) {
            Borrower nextInQueue = catalogBook.getHoldQueue().peek();
            if (nextInQueue != null &&
                    nextInQueue.getUsername().equalsIgnoreCase(currentBorrower.getUsername())) {

                catalogBook.getHoldQueue().poll();
                catalogBook.setStatus("Checked Out");
                catalogBook.setCurrentHolder(currentBorrower);
                currentBorrower.getBorrowedBooks().add(catalogBook);
                currentBorrower.getHeldBooks().remove(catalogBook);
                assignDueDate(catalogBook);
                return true;
            }
        }

        return false;
    }

    public String returnBook(Borrower borrower, Book book) {
        if (borrower == null) {
            return "Invalid borrower.";
        }

        if (borrower.getBorrowedBooks().isEmpty()) {
            return borrower.getUsername() + " has no books to return.";
        }

        if (!borrower.getBorrowedBooks().contains(book)) {
            return borrower.getUsername() + " has not borrowed " + book.getTitle() + ".";
        }

        borrower.getBorrowedBooks().remove(book);
        book.setDueDate(null);

        if (!book.getHoldQueue().isEmpty()) {
            transferHoldToNextBorrower(book);
        } else {
            book.setStatus("Available");
            book.setCurrentHolder(null);
        }

        return "Successfully returned " + book.getTitle() + ".";

    }

    public boolean verifyAvailability(Book book) {
        if (book == null) return false;
        String status = book.getStatus();
        return status.equalsIgnoreCase("Available");
    }

    public boolean verifyBorrowerEligibility(Borrower borrower) {
        if (borrower == null) return false;
        int count = borrower.getBorrowedBooks().size();
        return count < 3;
    }

    public void assignDueDate(Book book) {
        if (book == null) return;
        LocalDate due = LocalDate.now().plusDays(14);
        book.setDueDate(due.toString());
    }

    public boolean recordBorrowTransaction(Borrower borrower, Book book) {
        if (borrower == null || book == null) return false;

        // Only allow if the book is available
        if (!book.getStatus().equalsIgnoreCase("Available")) {
            return false;
        }

        book.setStatus("Checked Out");
        borrower.getBorrowedBooks().add(book);

        return true;
    }

    public boolean placeHold(Borrower borrower, Book book) {
        if (borrower == null || book == null) return false;

        // Can't hold available books
        if (book.getStatus().equalsIgnoreCase("Available")) {
            return false;
        }

        // Can't place duplicate holds
        if (borrower.getHeldBooks().contains(book) || book.getHoldQueueUsernames().contains(borrower.getUsername())) {
            return false;
        }

        if (borrower.getBorrowedBooks().contains(book)) {
            return false;
        }

        borrower.getHeldBooks().add(book);
        book.getHoldQueue().add(borrower);
        book.setStatus("On Hold");
        return true;
    }

    public List<String> displayBorrowedBooks(Borrower borrower) {
        List<String> result = new ArrayList<>();

        if (borrower == null) return result;

        if (borrower.getBorrowedBooks().isEmpty()) {
            result.add("No books currently borrowed.");
            return result;
        }

        for (Book book : borrower.getBorrowedBooks()) {
            String due = (book.getDueDate() != null) ? book.getDueDate() : "No due date";
            result.add(book.getTitle() + " - Due: " + due);
        }

        return result;
    }

    public boolean processReturn(Borrower borrower, Book book) {
        if (borrower == null || book == null) return false;

        if (!borrower.getBorrowedBooks().contains(book)) {
            return false;
        }

        borrower.getBorrowedBooks().remove(book);
        book.setStatus("Available");
        book.setDueDate(null);

        return true;
    }

    public boolean updateBookStatusAfterReturn(Borrower borrower, Book book) {
        if (borrower == null || book == null) return false;

        // Borrower must actually have the book
        if (!borrower.getBorrowedBooks().contains(book)) return false;

        borrower.getBorrowedBooks().remove(book);

        if (book.getHoldQueue().isEmpty()) {
            book.setStatus("Available");
        } else {
            // Holds exists so mark on hold
            Borrower nextInQueue = book.getHoldQueue().peek();
            book.setStatus("On Hold");

            notifications.clear();
            notifications.add("Book available: " + book.getTitle());
        }

        book.setDueDate(null);
        return true;
    }

    public void transferHoldToNextBorrower(Book book) {
        if (book == null) return;

        if (book.getHoldQueue().isEmpty()) {
            book.setStatus("Available");
            book.setCurrentHolder(null);
        } else {
            Borrower nextInQueue = book.getHoldQueue().peek();
            book.setCurrentHolder(null);
            book.setStatus("On Hold");

            if (nextInQueue != null) {
                notifications.clear();
                notifications.add(nextInQueue.getUsername() + " — The book '" + book.getTitle() + "' is now available");
            }
        }
    }



    public String confirmReturnCompletion(Borrower borrower, Book book) {
        if (borrower == null || book == null) {
            return "Error: Invalid return operation";
        }

        // Ensure the book is now available
        book.setStatus("Available");

        String message = "Return complete: " + book.getTitle() + " is now Available";

        return message;
    }

    public String logoutBorrower() {
        if (currentBorrower == null) {
            return "No active session to logout.";
        }

        currentBorrower = null;
        return "Logout successful. Returning to login prompt.";
    }

    public Book getBook(String title) {
        if (catalogue == null) return null;
        for (Book b : catalogue.getBooks()) {
            if (b.getTitle().equalsIgnoreCase(title)) {
                return b;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Scanner scanner = new Scanner(System.in);
        System.out.println();
        System.out.println("Welcome to the Library Management System");

        while (true) {
            System.out.print("\nEnter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            if (!library.login(username, password)) {
                System.out.println("Authentication failed. Please try again.\n");
                continue;
            }

            System.out.println("Login successful!\n");

            boolean sessionActive = true;
            while (sessionActive) {
                System.out.println("Choose an option:");
                System.out.println("1. Borrow Book");
                System.out.println("2. Return Book");
                System.out.println("3. Logout");
                System.out.print("Enter choice: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        System.out.println("\nAvailable Books:");
                        for (int i = 0; i < catalogue.getCatalogueSize(); i++) {
                            Book book = catalogue.getBook(i);
                            System.out.println((i + 1) + ". " + book.getTitle() + " - " + book.getStatus());
                        }

                        System.out.print("\nEnter the number of the book to borrow: ");
                        try {
                            int index = Integer.parseInt(scanner.nextLine()) - 1;
                            Book selected = catalogue.getBook(index);
                            if (library.borrowBook(selected)) {
                                System.out.println("You have borrowed: " + selected.getTitle());
                            } else {
                                System.out.println("Book is already checked out.");
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid input. Please enter a valid number.");
                        }
                        break;

                    case "2":
                        Borrower current = library.getCurrentBorrower();
                        if (current.getBorrowedBooks().isEmpty()) {
                            System.out.println("You have no books to return.");
                            break;
                        }

                        System.out.println("\nYour Borrowed Books:");
                        for (int i = 0; i < current.getBorrowedBooks().size(); i++) {
                            Book b = current.getBorrowedBooks().get(i);
                            String dueDate = b.getDueDate() != null ? " (Due: " + b.getDueDate() + ")" : "";
                            System.out.println((i + 1) + ". " + b.getTitle() + dueDate);
                        }

                        System.out.print("\nEnter the number of the book you want to return: ");
                        try {
                            int index = Integer.parseInt(scanner.nextLine()) - 1;
                            Book toReturn = current.getBorrowedBooks().get(index);
                            library.returnBook(current, toReturn);
                            System.out.println("Returned successfully: " + toReturn.getTitle());
                        } catch (Exception e) {
                            System.out.println("Invalid input. Please enter a valid number.");
                        }
                        break;

                    case "3":
                        library.logoutBorrower();
                        System.out.println("Logged out successfully.\n");
                        sessionActive = false;
                        break;

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        }
    }
}