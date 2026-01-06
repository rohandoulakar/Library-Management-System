const Catalogue = require("./Catalogue");
const Book = require("./Book");

function initializeLibrary() {
    const catalogue = new Catalogue();

    const titles = [
        "The Great Gatsby", "To Kill a Mockingbird", "1984",
        "Pride and Prejudice", "The Hobbit", "Harry Potter",
        "The Catcher in the Rye", "Animal Farm", "Lord of the Flies",
        "Jane Eyre", "Wuthering Heights", "Moby Dick",
        "The Odyssey", "Hamlet", "War and Peace",
        "The Divine Comedy", "Crime and Punishment",
        "Don Quixote", "The Iliad", "Ulysses"
    ];

    titles.forEach((title, i) => {
        catalogue.addBook(new Book(i + 1, title, "Unknown Author"));
    });

    return catalogue;
}

module.exports = initializeLibrary;
