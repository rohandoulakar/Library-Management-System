class Catalogue {
    constructor() {
        this.books = []; // array of Book objects
    }

    addBook(book) {
        this.books.push(book);
    }

    getBook(id) {
        return this.books.find(b => b.id === id);
    }

    getAllBooks() {
        return this.books;
    }
}

module.exports = Catalogue;
