describe('Library Book Management System', () => {

  // Reset backend before each scenario
  beforeEach(() => {
    cy.request('POST', 'http://localhost:3000/api/reset');
    cy.visit('http://localhost:3000');
  });

  it('Borrowing and returning ensures only one user can hold a book at a given time', () => {

    cy.uiLogin('alice', 'pass123');
  
    cy.borrowBook("The Great Gatsby");
    
    // Success message to show the book, The Great Gatsby has been successfully borrowed by Alice
    cy.get('#msg-area').should('contain', 'Successfully borrowed');
  
    cy.contains('td', 'The Great Gatsby', { timeout: 10000 })
      .parents('tr')
      .within(() => {
        // Confirms book shows checked out after Alice borrows it
        cy.get('.chip').should('have.class', 'checkedout')
      });
  
    cy.get('#logoutBtn').click();
  
    // Bob tries to borrow same book
    cy.uiLogin('bob', 'pass456');

    cy.borrowBook("The Great Gatsby");
      
    // Confirms Bob cannot borrow it while Alice still has the book
    // to ensure a borrowed book becomes unavailable to other users
    cy.get('#msg-area').should('contain', 'checked out')
  
    cy.get('#logoutBtn').click();
  
    // User 1 returns the book
    cy.uiLogin('alice', 'pass123');
    cy.goToMyBooks();
    cy.returnBook("The Great Gatsby");
    
    // Success message to show that the book, The Great Gatsby has been returned
    cy.get('#msg-area').should('contain', 'Successfully returned');
      
    cy.backToLibrary();
  
    cy.contains('td', 'The Great Gatsby', { timeout: 10000 })
      .parents('tr')
      .within(() => {
        // A returned book becomes available again
        cy.get('.chip').should('have.class', 'available')
      });
  });

  it('Hold queue processes FIFO and notifications go to the correct users', () => {
    
    cy.uiLogin('alice', 'pass123');
    cy.borrowBook("Harry Potter");

    // Success message to show Alice successfully borrowed the book, Harry Potter
    cy.get('#msg-area').should('contain', 'Successfully borrowed');
    cy.get('#logoutBtn').click();

    // Bob places the first hold
    cy.uiLogin('bob', 'pass456');
    cy.placeHold("Harry Potter");

    // Confirms Bob successfully placed a hold on the unavailable book
    cy.get('#msg-area').should('contain', 'Hold placed');

    cy.contains('td', 'Harry Potter')
      .parents('tr')
      .within(() => {
        // Confirms Bob appears first in the hold queue (FIFO order start)
        // Bob should be first in queue because he placed hold first
        cy.contains('bob');  
      });

    cy.get('#logoutBtn').click();

    // Charlie places hold after Bob
    cy.uiLogin('charlie', 'pass789');
    cy.placeHold("Harry Potter");

    // Confirms Charlie successfully placed a hold in the queue
    cy.get('#msg-area').should('contain', 'Hold placed');

    cy.contains('td', 'Harry Potter')
      .parents('tr')
      .within(() => {
        // Confirms the queue now contains Bob first, then Charlie
        // Queue must show bob first, then charlie to satisfy FIFO
        cy.contains('bob');
        cy.contains('charlie');
      });

    cy.get('#logoutBtn').click();

    // User 1 returns the book
    cy.uiLogin('alice', 'pass123');
    cy.goToMyBooks();
    cy.returnBook("Harry Potter");

    // Success message to show Alice returned the book, Harry Potter
    cy.get('#msg-area').should('contain', 'Returned');
    cy.backToLibrary();

    cy.get('#logoutBtn').click();

    cy.uiLogin('charlie', 'pass789');

    // Ensures Charlie does do receive a notification yet because Bob is first in the queue
    cy.get('#notif-area').should('not.contain', 'available');

    // Charlie attempts to borrow early but must be blocked
    cy.borrowBook("Harry Potter");

    // Error message confirming Charlie is not allowed to borrow Harry Potter before Bob
    cy.get('#msg-area').should('contain', 'Book reserved for another user')
    
    cy.get('#logoutBtn').click();


    cy.uiLogin('bob', 'pass456');

    // Ensures Bob is notified that the book is available for him,
    // which satisfies the requirement that notifications go to the correct user
    cy.get('#notif-area').should('contain', 'available')
      
    // Bob attempts to borrow the book now that he is notified
    cy.borrowBook("Harry Potter");

    // Success message showing Bob successfully borrowed the book
    cy.get('#msg-area').should('contain', 'Successfully borrowed');

    cy.get('#logoutBtn').click();

    //User 3 is now head of the queue and should be notified next
    cy.uiLogin('bob', 'pass456');
    cy.goToMyBooks();
    cy.returnBook("Harry Potter");

    // Success message for returning the book, Harry Potter
    cy.get('#msg-area').should('contain', 'Returned');
    
    cy.backToLibrary();
    cy.get('#logoutBtn').click();


    cy.uiLogin('charlie', 'pass789');

    // Confirms FIFO order, Charlie is notified only after Bob returns the book.
    cy.get('#notif-area').should('contain', 'available')

    // Charlie attempts to borrow now that his notification arrived
    cy.borrowBook("Harry Potter");

    // Success message to show Charlie now gets the book
    cy.get('#msg-area').should('contain', 'Successfully borrowed');

  });
  it('Borrowing limit and hold interactions', () => {

    cy.uiLogin('bob', 'pass456');
    cy.borrowBook("Harry Potter");

    // Check to make sure Bob was able to successfully borrow Harry Potter
    // This sets up the scenario by ensuring the book becomes unavailable for Alice
    cy.get('#msg-area').should('contain', 'Successfully borrowed');
    cy.get('#logoutBtn').click();

    cy.uiLogin('alice', 'pass123');
    

    cy.borrowBook("The Great Gatsby");
    // Alice successfully borrows her first book, The Great Gatsby
    cy.get('#msg-area').should('contain', 'Successfully borrowed');  
  
    cy.borrowBook("1984");
    // Alice successfully borrows her second book, 1984
    cy.get('#msg-area').should('contain', 'Successfully borrowed');  
  
    cy.borrowBook("Pride and Prejudice");
    // Alice successfully borrows her third book, Pride and Prejudice
    cy.get('#msg-area').should('contain', 'Successfully borrowed');  
  
    cy.borrowBook("Harry Potter");
    // Asserts that Alice is prevented from borrowing a 4th book, confirming the 3-book borrowing limit constraint
    cy.get('#msg-area').should('contain', 'Borrowing limit');  
  
    cy.placeHold("Harry Potter");
    // Confirms hold was placed despite the borrowing limit
    cy.get('#msg-area').should('contain', 'Hold placed');
  
    cy.get('#logoutBtn').click();
  
    cy.uiLogin('bob', 'pass456');
    cy.goToMyBooks();
    cy.returnBook("Harry Potter");

    // Bob returns the book Harry Potter, making it available for the hold queue
    cy.get('#msg-area').should('contain', 'Returned');
    cy.backToLibrary();
    cy.get('#logoutBtn').click();
  
    
    cy.uiLogin('alice', 'pass123');
    // Confirms Alice is now notified that Harry Potter is available for her
    cy.get('#notif-area').should('contain', 'available');

    cy.goToMyBooks();
    cy.returnBook("The Great Gatsby");
    // Alice returns The Great Gatsby, to reduce her borrowed count from 3 to 2 so that she can borrow Harry Potter
    cy.get('#msg-area').should('contain', 'Successfully returned');
    
    cy.backToLibrary();
    cy.borrowBook("Harry Potter");
    // Alice should now be able to borrow Harry Potter
    cy.get('#msg-area').should('contain', 'Successfully borrowed');
    
  });
  
});
