package org.example.library.steps;

import org.example.library.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class LibrarySteps {

    private Library library;
    private Catalogue catalogue;
    private Book book;
    private boolean secondBorrowAttempt;
    private String lastMessage;
    private List<String> borrowedBooks;

    @Given("the library system is initialized with default users and books")
    public void the_library_system_is_initialized_with_default_users_and_books() {
        library = new Library();
        InitializeLibrary initLib = new InitializeLibrary();
        catalogue = initLib.initializeLibrary();
        library.setCatalogue(catalogue);
        InitializeUsers.initialize(library);
    }

    @Given("{string} and {string} are registered borrowers")
    public void and_are_registered_borrowers(String user1, String user2) {
        Borrower borrower1 = null;
        Borrower borrower2 = null;

        for (Borrower b : library.getBorrowers()) {
            if (b.getUsername().equalsIgnoreCase(user1)) {
                borrower1 = b;
            } else if (b.getUsername().equalsIgnoreCase(user2)) {
                borrower2 = b;
            }
        }

        if (borrower1 != null) {
            library.authenticate(borrower1.getUsername(), borrower1.getPassword());
        }

        if (borrower2 != null) {
            library.authenticate(borrower2.getUsername(), borrower2.getPassword());
        }
    }


    @Given("{string} is available in the catalogue")
    public void is_available_in_the_catalogue(String title) {
        for (Book b : catalogue.getBooks()) {
            if (b.getTitle().equalsIgnoreCase(title)) {
                book = b;
                break;
            }
        }
    }

    @When("{string} borrows {string}")
    public void borrows(String username, String title) {
        Borrower user = null;
        for (Borrower b : library.getBorrowers()) {
            if (b.getUsername().equalsIgnoreCase(username)) {
                user = b;
                break;
            }
        }

        if (user != null) {
            library.login(user.getUsername(), user.getPassword());
        }

        Book book = library.getBook(title);
        library.borrowBook(book);
        library.logout();
    }

    @Then("{string} becomes unavailable to {string}")
    public void becomes_unavailable_to(String title, String otherUser) {
        Book sameBook = null;
        for (Book b : catalogue.getBooks()) {
            if (b.getTitle().equalsIgnoreCase(title)) {
                sameBook = b;
                break;
            }
        }

        Borrower user = null;
        for (Borrower b : library.getBorrowers()) {
            if (b.getUsername().equalsIgnoreCase(otherUser)) {
                user = b;
                break;
            }
        }

        library.login(user.getUsername(), user.getPassword());
        secondBorrowAttempt = library.borrowBook(sameBook);
        library.logout();

        assertFalse(secondBorrowAttempt, otherUser + " should not be able to borrow " + title + " while it is checked out");
    }

    @When("{string} returns {string}")
    public void returns(String username, String title) {
        Borrower user = null;
        for (Borrower b : library.getBorrowers()) {
            if (b.getUsername().equals(username)) {
                user = b;
                break;
            }
        }

        library.login(user.getUsername(), user.getPassword());

        Book toReturn = null;
        for (Book b : user.getBorrowedBooks()) {
            if (b.getTitle().equalsIgnoreCase(title)) {
                toReturn = b;
                break;
            }
        }

        lastMessage = library.returnBook(user, toReturn);
        library.logout();
    }

    @Then("{string} becomes available again")
    public void becomes_available_again(String title) {
        Book sameBook = null;
        for (Book b : catalogue.getBooks()) {
            if (b.getTitle().equalsIgnoreCase(title)) {
                sameBook = b;
                break;
            }
        }
        assertEquals("Available", sameBook.getStatus(), "Book should become Available again after being returned");
    }

    @Then("only one user can have {string} at a time")
    public void only_one_user_can_have_at_a_time(String title) {
        int count = 0;

        for (Borrower b : library.getBorrowers()) {
            for (Book borrowed : b.getBorrowedBooks()) {
                if (borrowed.getTitle().equalsIgnoreCase(title)) {
                    count++;
                }
            }
        }

        assertTrue(count <= 1, "Only one borrower should have " + title + " at a time");
    }

    @Given("{string}, {string}, and {string} are registered borrowers")
    public void register_three_users(String user1, String user2, String user3) {
        Borrower borrower1 = null;
        Borrower borrower2 = null;
        Borrower borrower3 = null;

        for (Borrower b : library.getBorrowers()) {
            if (b.getUsername().equalsIgnoreCase(user1)) {
                borrower1 = b;
            } else if (b.getUsername().equalsIgnoreCase(user2)) {
                borrower2 = b;
            }
            else if (b.getUsername().equalsIgnoreCase(user3)){
                borrower3 = b;
            }
        }

        if (borrower1 != null) {
            library.authenticate(borrower1.getUsername(), borrower1.getPassword());
        }

        if (borrower2 != null) {
            library.authenticate(borrower2.getUsername(), borrower2.getPassword());
        }

        if (borrower3 != null) {
            library.authenticate(borrower3.getUsername(), borrower3.getPassword());
        }
    }

    @Then("{string} becomes unavailable to other users")
    public void becomes_unavailable_to_others(String title) {
        Book sameBook = library.getBook(title);
        assertEquals("Checked Out", sameBook.getStatus(), "Book should be Checked Out and unavailable");
    }

    @When("{string} places a hold on {string}")
    public void place_hold(String username, String title) {
        Borrower user = null;
        for (Borrower b : library.getBorrowers()) {
            if (b.getUsername().equals(username)) {
                user = b;
                break;
            }
        }

        library.login(user.getUsername(), user.getPassword());
        Book book = library.getBook(title);
        library.placeHold(user, book);
        library.logout();
    }

    @Then("{string} should be first in the hold queue and {string} should be second in the hold queue for {string}")
    public void verify_fifo_queue_order(String firstUser, String secondUser, String title) {
        Book sameBook = library.getBook(title);
        Queue<Borrower> queue = sameBook.getHoldQueue();

        assertTrue(queue.size() >= 2, "Hold queue for " + title + " should contain at least two users");

        // Get the first and second users in the queue
        Iterator<Borrower> iterator = queue.iterator();
        Borrower first = iterator.next();
        Borrower second = iterator.next();

        assertEquals(firstUser.toLowerCase(), first.getUsername().toLowerCase(), firstUser + " should be first in the hold queue for " + title);
        assertEquals(secondUser.toLowerCase(), second.getUsername().toLowerCase(), secondUser + " should be second in the hold queue for " + title);
    }

    @Then("{string} should not be able to borrow {string} before {string}")
    public void verify_cannot_borrow_before_notified_user(String otherUser, String title, String notifiedUser) {
        Borrower user = null;
        for (Borrower b : library.getBorrowers()) {
            if (b.getUsername().equals(otherUser)) {
                user = b;
                break;
            }
        }

        Book book = library.getBook(title);

        // Attempt to borrow while it is on hold for another user
        library.login(user.getUsername(), user.getPassword());
        boolean borrowAttempt = library.borrowBook(book);
        library.logout();

        assertFalse(borrowAttempt, otherUser + " should not be able to borrow " + title + " before " + notifiedUser + " who was notified first");
    }



    @Then("{string} should be notified that {string} is available")
    public void verify_notification(String username, String title) {
        List<String> notifications = library.getNotifications();
        boolean notified = notifications.stream().anyMatch(n -> n.contains(username) && n.contains(title) && n.contains("available"));
        assertTrue(notified, username + " should be notified that " + title + " is available");
    }

    @Then("the hold queue for {string} should advance correctly with {string} next in line")
    public void verify_queue_advancement(String title, String expectedNextUser) {
        Book sameBook = library.getBook(title);
        Queue<Borrower> queue = sameBook.getHoldQueue();
        Borrower next = queue.peek();
        assertEquals(expectedNextUser.toLowerCase(), next.getUsername().toLowerCase(), "Queue did advance correctly");
    }


    @Then("{string} should now have reached the borrowing limit")
    public void verify_borrowing_limit_reached(String username) {
        Borrower user = null;
        for (Borrower b : library.getBorrowers()) {
            if (b.getUsername().equalsIgnoreCase(username)) {
                user = b;
                break;
            }
        }

        boolean eligible = library.verifyBorrowerEligibility(user);
        assertFalse(eligible, username + " should have reached the borrowing limit of 3 books");
    }

    @Then("{string} should not be allowed to borrow more than 3 books")
    public void verify_borrow_limit_enforced(String username) {
        Borrower user = null;
        for (Borrower b : library.getBorrowers()) {
            if (b.getUsername().equalsIgnoreCase(username)) {
                user = b;
                break;
            }
        }
        int borrowedCount = user.getBorrowedBooks().size();
        assertTrue(borrowedCount <= 3, username + " should not have more than 3 borrowed books, already has " + borrowedCount);
    }

    @Then("{string} should be able to place a hold even at borrowing limit")
    public void verify_hold_allowed_at_limit(String username) {
        Borrower user = null;
        for (Borrower b : library.getBorrowers()) {
            if (b.getUsername().equalsIgnoreCase(username)) {
                user = b;
                break;
            }
        }

        int borrowedCount = user.getBorrowedBooks().size();
        assertEquals(3, borrowedCount, username + " should have exactly 3 borrowed books, borrowing limit reached");

        // Check that the borrower has at least one held book
        assertFalse(user.getHeldBooks().isEmpty(), username + " should have successfully placed a hold even at the borrowing limit");
    }

    @Then("{string} should drop below the borrowing limit")
    public void verify_drops_below_limit(String username) {
        Borrower user = null;
        for (Borrower b : library.getBorrowers()) {
            if (b.getUsername().equalsIgnoreCase(username)) {
                user = b;
                break;
            }
        }
        int borrowedCount = user.getBorrowedBooks().size();
        assertTrue(borrowedCount < 3, username + " should have dropped below the borrowing limit after returning a book, but currently has " + borrowedCount);
    }

    @Then("all books should still be available in the catalogue")
    public void verify_all_books_available() {
        for (Book book : library.displayAllBooks()) {
            assertEquals("Available", book.getStatus(), "All book should be available");
        }
    }

    @When("{string} views her borrowed books")
    public void views_her_borrowed_books(String username) {
        Borrower user = null;
        for (Borrower b : library.getBorrowers()) {
            if (b.getUsername().equalsIgnoreCase(username)) {
                user = b;
                break;
            }
        }

        borrowedBooks = library.displayBorrowedBooks(user);
    }

    @Then("the system should report that {string} has no books currently borrowed")
    public void verify_no_books_currently_borrowed(String username) {
        assertEquals("No books currently borrowed.", borrowedBooks.get(0), "System should correctly report that " + username + " has no books currently borrowed");
    }

    @Then("the system should display a message that {string} has no books to return")
    public void verify_no_books_to_return_message(String username) {
        String expectedMessage = username + " has no books to return.";
        assertEquals(expectedMessage, lastMessage);
    }

}
