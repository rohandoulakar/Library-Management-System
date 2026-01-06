package org.example.library;

public class InitializeLibrary {

    Catalogue catalogue = new Catalogue();

    public Catalogue initializeLibrary() {
        catalogue.addBook(new Book("The Great Gatsby", "F. Scott Fitzgerald"));
        catalogue.addBook(new Book("To Kill a Mockingbird", "Harper Lee"));
        catalogue.addBook(new Book("1984", "George Orwell"));
        catalogue.addBook(new Book("Pride and Prejudice", "Jane Austen"));
        catalogue.addBook(new Book("The Hobbit", "J.R.R. Tolkien"));
        catalogue.addBook(new Book("Harry Potter", "J.K. Rowling"));
        catalogue.addBook(new Book("The Catcher in the Rye", "J.D. Salinger"));
        catalogue.addBook(new Book("Animal Farm", "George Orwell"));
        catalogue.addBook(new Book("Lord of the Flies", "William Golding"));
        catalogue.addBook(new Book("Jane Eyre", "Charlotte Brontë"));
        catalogue.addBook(new Book("Wuthering Heights", "Emily Brontë"));
        catalogue.addBook(new Book("Moby Dick", "Herman Melville"));
        catalogue.addBook(new Book("The Odyssey", "Homer"));
        catalogue.addBook(new Book("Hamlet", "William Shakespeare"));
        catalogue.addBook(new Book("War and Peace", "Leo Tolstoy"));
        catalogue.addBook(new Book("The Divine Comedy", "Dante Alighieri"));
        catalogue.addBook(new Book("Crime and Punishment", "Fyodor Dostoevsky"));
        catalogue.addBook(new Book("Don Quixote", "Miguel de Cervantes"));
        catalogue.addBook(new Book("The Iliad", "Homer"));
        catalogue.addBook(new Book("Ulysses", "James Joyce"));
        return catalogue;
    }
}
