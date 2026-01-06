Cypress.Commands.add('uiLogin', (username, password) => {
    cy.visit('/login.html');
  
    // Type username and password in UI fields
    cy.get('#username').type(username);
    cy.get('#password').type(password);
  
    cy.get('#loginBtn').click();
  });
  
  // Navigate to My Books page
  Cypress.Commands.add('goToMyBooks', () => {
    cy.get('#myBooksBtn').click();
  });

  // From my books page to initial library page
  Cypress.Commands.add('backToLibrary', () => {
    cy.contains('button', 'Back to Library', { timeout: 8000 })
      .click();
  });
  
  Cypress.Commands.add('borrowBook', (title) => {
    cy.contains('td', title, { timeout: 10000 })
      .parents('tr')
      .within(() => {
        cy.contains('Borrow').click();
      });
  });
  
  Cypress.Commands.add('returnBook', (title) => {
    cy.contains('td', title, { timeout: 10000 })
      .parents('tr')
      .within(() => {
        cy.contains('Return').click();
      });
  });
    
  Cypress.Commands.add('placeHold', (title) => {
    cy.contains('td', title, { timeout: 10000 })
      .parents('tr')
      .within(() => {
        cy.get('button')
          .contains(/^Hold$/) 
          .click({ force: true });
      });
  });
  
  