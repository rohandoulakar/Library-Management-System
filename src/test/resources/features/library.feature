Feature: Library Management Acceptance Tests
  As a library admin
  I want to verify that core borrowing, returning, and hold functionality works correctly
  So that the Library enforces appropriate access for all users

  # A1_scenario
  Scenario: Basic borrow and return cycle
    Given the library system is initialized with default users and books
    And "alice" and "bob" are registered borrowers
    And "The Great Gatsby" is available in the catalogue
    When "alice" borrows "The Great Gatsby"
    Then "The Great Gatsby" becomes unavailable to "bob"
    When "alice" returns "The Great Gatsby"
    Then "The Great Gatsby" becomes available again
    And only one user can have "The Great Gatsby" at a time

  # multiple_holds_queue_processing
  Scenario: Multiple users placing holds on the same book
    Given the library system is initialized with default users and books
    And "alice", "bob", and "charlie" are registered borrowers
    And "To Kill a Mockingbird" is available in the catalogue

    When "alice" borrows "To Kill a Mockingbird"
    Then "To Kill a Mockingbird" becomes unavailable to other users

    When "bob" places a hold on "To Kill a Mockingbird"
    And "charlie" places a hold on "To Kill a Mockingbird"
    Then "bob" should be first in the hold queue and "charlie" should be second in the hold queue for "To Kill a Mockingbird"

    When "alice" returns "To Kill a Mockingbird"
    Then "bob" should be notified that "To Kill a Mockingbird" is available
    And "charlie" should not be able to borrow "To Kill a Mockingbird" before "bob"

    When "bob" borrows "To Kill a Mockingbird"
    Then "To Kill a Mockingbird" becomes unavailable to other users

    When "bob" returns "To Kill a Mockingbird"
    Then "charlie" should be notified that "To Kill a Mockingbird" is available
    And the hold queue for "To Kill a Mockingbird" should advance correctly with "charlie" next in line

  # borrowing_limit_and_hold_interactions
  Scenario: Borrowing limits and holds
    Given the library system is initialized with default users and books
    And "alice" and "bob" are registered borrowers
    When "alice" borrows "The Hobbit"
    And "alice" borrows "Lord of the Flies"
    And "alice" borrows "Hamlet"
    And "bob" borrows "Harry Potter"
    Then "alice" should now have reached the borrowing limit

    When "alice" borrows "The Catcher in the Rye"
    Then "alice" should not be allowed to borrow more than 3 books

    When "alice" places a hold on "Harry Potter"
    Then "alice" should be able to place a hold even at borrowing limit

    When "bob" returns "Harry Potter"
    Then "alice" should be notified that "Harry Potter" is available

    When "alice" returns "The Hobbit"
    Then "alice" should drop below the borrowing limit

  # no_books_borrowed_scenario
  Scenario: No books are currently borrowed
    Given the library system is initialized with default users and books
    And "alice" and "bob" are registered borrowers

    When "alice" views her borrowed books
    Then the system should report that "alice" has no books currently borrowed

    When "bob" returns "The Great Gatsby"
    Then the system should display a message that "bob" has no books to return

    Then all books should still be available in the catalogue
