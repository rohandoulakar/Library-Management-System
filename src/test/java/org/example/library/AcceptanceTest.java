package org.example.library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class AcceptanceTest {
    @Test
    @DisplayName("Multi-User Borrow and Return with Availability Validated")
    void A_TEST_01() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        // User1 logs in and borrows The Great Gatsby
        Borrower user1 = library.authenticate("jack", "jack123");
        library.setCurrentBorrower(user1);

        Book book = catalogue.getBook(0);
        assertEquals("Available", book.getStatus(), "Book should initially be Available");

        book.setStatus("Checked Out");
        user1.getBorrowedBooks().add(book);
        library.logoutBorrower();

        Borrower user2 = library.authenticate("mark", "mark123");
        library.setCurrentBorrower(user2);

        assertEquals("Checked Out", book.getStatus());

        library.logoutBorrower();

        // User1 logs back in and returns book
        library.setCurrentBorrower(user1);
        library.confirmReturnCompletion(user1, book);

        assertEquals("Available", book.getStatus(), "Book should be Available after User1 return");

        library.logoutBorrower();

        // User2 logs in again and sees book available
        library.setCurrentBorrower(user2);
        assertEquals("Available", book.getStatus());
    }

    @Test
    @DisplayName("Initialization and Authentication with Error Handling")
    void A_TEST_02() {
        Library library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);

        assertEquals(20, catalogue.getCatalogueSize());
        assertEquals(3, library.getBorrowerCount());

        boolean validLogin = library.login("jack", "jack123");
        assertTrue(validLogin);

        // Display menu options
        List<String> operations = library.getAvailableOperations();
        assertTrue(operations.contains("Borrow Book") && operations.contains("Return Book") && operations.contains("Logout"));

        library.logoutBorrower();
        assertNull(library.getCurrentBorrower(), "Borrower should be logged out successfully");

        // Invalid Login
        boolean invalidLogin = library.login("unknownUser", "pass123");
        assertFalse(invalidLogin);
        assertEquals("Authentication failed. Please try again.", library.getLoginMessage());

    }


}
