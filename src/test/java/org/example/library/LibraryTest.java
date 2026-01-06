package org.example.library;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LibraryTest {

    @Test
    @DisplayName("Check library catalogue size is 20")
    void RESP_01_test_01(){
        InitializeLibrary library = new InitializeLibrary();
        Catalogue catalogue = library.initializeLibrary();

        int size = catalogue.getCatalogueSize();

        assertEquals(20, size);


    }
    @Test
    @DisplayName("Check library catalogue for valid book - Great Gatsby.")
    void RESP_01_test_02(){

        InitializeLibrary library = new InitializeLibrary();
        Catalogue catalogue = library.initializeLibrary();

        Book book = catalogue.getBook(0);
        String title = book.getTitle();
        assertEquals("Great Gatsby",title);
    }

    @Test
    @DisplayName("Check system initializes with 3 borrower accounts")
    void RESP_02_test_01() {
        Library library = new Library();
        InitializeUsers.initialize(library);

        int count = library.getBorrowerCount();
        assertEquals(3, count, "Library should be initialized with exactly 3 borrower accounts");
    }

    @Test
    @DisplayName("Check borrower credentials are valid")
    void RESP_02_test_02() {
        Library library = new Library();
        InitializeUsers.initialize(library);

        Borrower first = library.getBorrowers().get(0);
        assertTrue(first.getUsername().equals("jack") && first.getPassword().equals("jack123"));
    }

    @Test
    @DisplayName("Authenticate valid borrower credentials")
    void RESP_03_test_01() {
        Library library = new Library();
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        assertNotNull(borrower, "Valid credentials should authenticate");
    }

    @Test
    @DisplayName("Reject invalid borrower credentials")
    void RESP_03_test_02() {
        Library library = new Library();
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "mike123");
        assertNull(borrower, "Invalid password should fail");
    }

    @Test
    @DisplayName("Maintain active borrower after login")
    void RESP_04_test_01() {
        Library library = new Library();
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(borrower);

        assertEquals("jack", library.getCurrentBorrower().getUsername());
    }

    @Test
    @DisplayName("Clear current borrower after logout")
    void RESP_04_test_02() {
        Library library = new Library();
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(borrower);
        library.logout();

        assertNull(library.getCurrentBorrower(), "Current borrower should be null after logout");
    }

    @Test
    @DisplayName("Notify borrower when a held book becomes available")
    void RESP_05_test_01_notifyWhenHeldBookAvailable() {
        Library library = new Library();
        Borrower borrower = new Borrower("jack", "jack123");

        Book dune = new Book("Dune", "Frank Herbert");
        dune.setStatus("Available");

        borrower.addHold(dune);
        library.addBorrower(borrower);
        library.login("jack", "jack123");

        List<String> notes = library.getNotifications();
        assertTrue(notes.contains("Book available: Dune"), "Jack should be notified when Dune is available");
    }

    @Test
    @DisplayName("Do not notify borrower when held books are unavailable")
    void RESP_05_test_02_noNotificationWhenNoHeldBookAvailable() {
        Library library = new Library();
        Borrower borrower = new Borrower("jack", "jack123");

        Book greatGatsby = new Book("Great Gatsby", "F. Scott FitzGerald");
        greatGatsby.setStatus("Checked Out");

        // Borrower Jack has a hold, but it is not available
        borrower.addHold(greatGatsby);
        library.addBorrower(borrower);

        assertTrue(library.getNotifications().isEmpty(), "No notifications should be shown");
    }

    @Test
    @DisplayName("Present available operations after login")
    void RESP_06_test_01() {
        Library library = new Library();
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(borrower);

        List<String> operations = library.getAvailableOperations();

        assertTrue(operations.containsAll(List.of("Borrow Book", "Return Book", "Logout")));
    }

    @Test
    @DisplayName("Return no operations when no borrower is logged in")
    void RESP_06_test_02() {
        Library library = new Library();
        InitializeUsers.initialize(library);

        List<String> operations = library.getAvailableOperations();

        assertTrue(operations.isEmpty(), "Operations list should be empty when no user is logged in");

    }

    @Test
    @DisplayName("Display borrower's current book count")
    void RESP_07_test_01() {
        Library library = new Library();
        InitializeLibrary initializeLibrary = new InitializeLibrary();
        Catalogue catalogue = initializeLibrary.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(borrower);

        int count = library.getBorrowedBookCount();
        assertEquals(0, count, "Borrower should start with 0 borrowed books");
    }

    @Test
    @DisplayName("Display all books with availability status")
    void RESP_07_test_02() {
        Library library = new Library();
        InitializeLibrary initializeLibrary = new InitializeLibrary();
        Catalogue catalogue = initializeLibrary.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        List<Book> books = library.displayAllBooks();
        assertEquals("Available", books.get(0).getStatus(), "Books should be available initially");
    }

    @Test
    @DisplayName("Borrow an available book successfully")
    void RESP_08_test_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(borrower);

        Book bookToBorrow = catalogue.getBook(0); // first book
        boolean success = library.borrowBook(bookToBorrow);

        assertTrue(success, "Borrow should succeed for an available book");
    }

    @Test
    @DisplayName("Cannot borrow a book that is already checked out")
    void RESP_08_test_02() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("mark", "mark123");
        library.setCurrentBorrower(borrower);

        Book bookToBorrow = catalogue.getBook(1);
        bookToBorrow.setStatus("Checked Out");

        boolean success = library.borrowBook(bookToBorrow);

        assertFalse(success, "Borrow should fail");
    }

    @Test
    @DisplayName("Verify book is available for borrowing")
    void RESP_09_test_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Book availableBook = catalogue.getBook(0);
        availableBook.setStatus("Available");

        boolean result = library.verifyAvailability(availableBook);
        assertTrue(result, "Book marked as Available should pass verification");
    }

    @Test
    @DisplayName("Verify unavailable books fail availability check")
    void RESP_09_test_02() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Book checkedOutBook = catalogue.getBook(1);
        checkedOutBook.setStatus("Checked Out");
        assertFalse(library.verifyAvailability(checkedOutBook), "Checked Out book should fail availability");

    }

    @Test
    @DisplayName("Borrower with less than 3 books is eligible to borrow")
    void RESP_10_test_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(borrower);

        borrower.getBorrowedBooks().add(catalogue.getBook(0));
        borrower.getBorrowedBooks().add(catalogue.getBook(1));

        boolean eligible = library.verifyBorrowerEligibility(borrower);
        assertTrue(eligible, "Borrower with fewer than 3 books should be eligible");
    }

    @Test
    @DisplayName("Borrower with 3 or more books is not eligible to borrow")
    void RESP_10_test_02() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("mark", "mark123");
        library.setCurrentBorrower(borrower);

        borrower.getBorrowedBooks().add(catalogue.getBook(2));
        borrower.getBorrowedBooks().add(catalogue.getBook(3));
        borrower.getBorrowedBooks().add(catalogue.getBook(4));

        boolean eligible = library.verifyBorrowerEligibility(borrower);
        assertFalse(eligible, "Borrower with 3 or more books should not be eligible");
    }

    @Test
    @DisplayName("Assign 14-day due date when book is borrowed")
    void RESP_11_test_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(borrower);

        Book book = catalogue.getBook(0);
        book.setStatus("Available");

        // Borrow the book
        library.assignDueDate(book);

        LocalDate expectedDue = LocalDate.now().plusDays(14);
        assertEquals(expectedDue.toString(), book.getDueDate());
    }

    @Test
    @DisplayName("Due date remains null if no assignment is made")
    void RESP_11_test_02() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Book book = catalogue.getBook(1);
        assertNull(book.getDueDate());
    }

    @Test
    @DisplayName("Successfully record borrow transaction and update status")
    void RESP_12_test_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(borrower);

        Book book = catalogue.getBook(0);
        book.setStatus("Available");

        library.recordBorrowTransaction(borrower, book);

        assertEquals("Checked Out", book.getStatus());
    }

    @Test
    @DisplayName("Borrow transaction fails if book is unavailable")
    void RESP_12_test_02() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("mark", "mark123");
        library.setCurrentBorrower(borrower);

        Book book = catalogue.getBook(1);
        book.setStatus("Checked Out");

        boolean success = library.recordBorrowTransaction(borrower, book);

        assertFalse(success);
    }

    @Test
    @DisplayName("Borrower can place hold when book is checked out")
    void RESP_13_test_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(borrower);

        Book book = catalogue.getBook(0);
        book.setStatus("Checked Out");

        library.placeHold(borrower, book);
        assertEquals("On Hold", book.getStatus());
    }

    @Test
    @DisplayName("Borrower cannot place duplicate hold on the same book")
    void RESP_13_test_02() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("mark", "mark123");
        library.setCurrentBorrower(borrower);

        Book book = catalogue.getBook(1);
        book.setStatus("Checked Out");

        boolean first = library.placeHold(borrower, book);
        // Attempt second hold
        boolean second = library.placeHold(borrower, book);

        assertFalse(second, "Second hold should fail");
    }

    @Test
    @DisplayName("Borrower cannot place hold on available or borrowed books")
    void RESP_13_test_03() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("charlie", "charlie123");
        library.setCurrentBorrower(borrower);

        //Book is Available
        Book availableBook = catalogue.getBook(2);
        availableBook.setStatus("Available");
        boolean placedAvailable = library.placeHold(borrower, availableBook);

        // Book already borrowed by borrower
        Book borrowedBook = catalogue.getBook(3);
        borrower.getBorrowedBooks().add(borrowedBook);
        boolean placedBorrowed = library.placeHold(borrower, borrowedBook);
        assertFalse(placedAvailable || placedBorrowed);
    }

    @Test
    @DisplayName("Multiple borrowers can place holds")
    void RESP_14_test_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower jack = library.authenticate("jack", "jack123");
        Borrower mark = library.authenticate("mark", "mark123");
        Borrower charlie = library.authenticate("charlie", "charlie123");

        Book book = catalogue.getBook(0);
        book.setStatus("Checked Out");

        // Add holds in order
        library.placeHold(jack, book);
        library.placeHold(mark, book);
        library.placeHold(charlie, book);

        List<String> queue = book.getHoldQueueUsernames();
        assertEquals(List.of("jack", "mark", "charlie"), queue, "Holds should be stored");
    }
    @Test
    @DisplayName("Prevent duplicate borrower from being added")
    void RESP_14_test_02() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower jack = library.authenticate("jack", "jack123");
        Book book = catalogue.getBook(1);
        book.setStatus("Checked Out");

        library.placeHold(jack, book);

        boolean placedAgain = library.placeHold(jack, book);

        assertFalse(placedAgain, "Duplicate hold should not be allowed");
    }

    @Test
    @DisplayName("Display borrower's currently borrowed books with due dates")
    void RESP_15_test_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(borrower);

        Book book1 = catalogue.getBook(0);
        Book book2 = catalogue.getBook(1);

        book1.setStatus("Checked Out");
        book1.setDueDate("2025-10-20");
        book2.setStatus("Checked Out");
        book2.setDueDate("2025-10-21");

        borrower.getBorrowedBooks().add(book1);
        borrower.getBorrowedBooks().add(book2);

        List<String> borrowedBooks = library.displayBorrowedBooks(borrower);

        assertTrue(borrowedBooks.containsAll(List.of("Great Gatsby - Due: 2025-10-20", "The Alchemist - Due: 2025-10-21")));
    }

    @Test
    @DisplayName("Display shows empty list when borrower has no borrowed books")
    void RESP_15_test_02() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("mark", "mark123");
        library.setCurrentBorrower(borrower);

        List<String> borrowedBooks = library.displayBorrowedBooks(borrower);

        assertTrue(borrowedBooks.isEmpty());
    }

    @Test
    @DisplayName("Borrower can return a checked-out book")
    void RESP_16_test_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(borrower);

        Book book = catalogue.getBook(0);
        book.setStatus("Checked Out");
        borrower.getBorrowedBooks().add(book);

        library.processReturn(borrower, book);
        assertFalse(borrower.getBorrowedBooks().contains(book), "Book should be removed from borrower’s borrowed list");
    }

    @Test
    @DisplayName("Return should fail if user never borrowed the book")
    void RESP_16_test_02() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("mark", "mark123");
        library.setCurrentBorrower(borrower);

        Book book = catalogue.getBook(1);
        book.setStatus("Checked Out");

        boolean success = library.processReturn(borrower, book);

        assertFalse(success);
    }

    @Test
    @DisplayName("Returned book becomes Available when no holds exist")
    void RESP_17_test_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(borrower);

        Book book = catalogue.getBook(0);
        book.setStatus("Checked Out");
        borrower.getBorrowedBooks().add(book);

        boolean success = library.updateBookStatusAfterReturn(borrower, book);

        assertTrue(success, "Return should succeed when no holds exist");
    }

    @Test
    @DisplayName("Returned book becomes On Hold when holds exist")
    void RESP_17_test_02() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower jack = library.authenticate("jack", "jack123");
        Borrower mark = library.authenticate("mark", "mark123");
        library.setCurrentBorrower(jack);

        Book book = catalogue.getBook(1);
        book.setStatus("Checked Out");
        jack.getBorrowedBooks().add(book);

        // Mark places hold before Jack returns
        library.placeHold(mark, book);

        // Jack returns the book
        library.updateBookStatusAfterReturn(jack, book);

        assertEquals("On Hold", book.getStatus());
    }

    @Test
    @DisplayName("Transfer hold to next borrower when book is returned")
    void RESP_18_test_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower jack = library.authenticate("jack", "jack123");
        Borrower mark = library.authenticate("mark", "mark123");
        Borrower charlie = library.authenticate("charlie", "charlie123");

        Book book = catalogue.getBook(0);
        book.setStatus("Checked Out");
        jack.getBorrowedBooks().add(book);

        // Add holds
        library.placeHold(mark, book);
        library.placeHold(charlie, book);

        library.transferHoldToNextBorrower(book);

        assertEquals(1, book.getHoldQueue().size());
    }

    @Test
    @DisplayName("No transfer occurs when hold queue is empty")
    void RESP_18_test_02() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Book book = catalogue.getBook(1);
        book.setStatus("Checked Out");

        library.transferHoldToNextBorrower(book);

        assertNull(book.getCurrentHolder());
    }

    @Test
    @DisplayName("Confirm return completion message and availability update")
    void RESP_19_test_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(borrower);

        Book book = catalogue.getBook(0);
        book.setStatus("Checked Out");
        borrower.getBorrowedBooks().add(book);

        // Process return confirmation
        String confirmation = library.confirmReturnCompletion(borrower, book);

        assertEquals("Return complete: Great Gatsby is now Available", confirmation);
    }

    @Test
    @DisplayName("Logout borrower clears session and confirms logout")
    void RESP_20_test_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        Borrower borrower = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(borrower);

        String message = library.logoutBorrower();

        assertEquals("Logout successful. Returning to login prompt.", message);
    }

    @Test
    @DisplayName("RESP-20: Logout called when no borrower logged in should handle gracefully")
    void RESP_20_test_02() {
        Library library = new Library();
        String message = library.logoutBorrower();

        assertEquals("No active session to logout.", message);
    }

    @Test
    @DisplayName("Application entry point to start user session")
    void RESP_21_test_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        library.setCatalogue(initLib.initializeLibrary());
        InitializeUsers.initialize(library);

        boolean loggedIn = library.login("jack", "jack123");

        assertTrue(loggedIn && library.getCurrentBorrower() != null);
    }




}
