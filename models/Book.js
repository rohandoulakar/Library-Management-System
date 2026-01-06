class Book {
    constructor(id, title, author = "") {
        this.id = id;
        this.title = title;
        this.author = author;

        this.status = "available";      
        this.dueDate = null;
        this.currentHolder = null;

        this.holdQueue = []; // FIFO of usernames
    }

    toJSON() {
        return {
            id: this.id,
            title: this.title,
            author: this.author,
            status: this.status,
            dueDate: this.dueDate,
            currentHolder: this.currentHolder,
            holdQueue: [...this.holdQueue]
        };
    }
}

module.exports = Book;
