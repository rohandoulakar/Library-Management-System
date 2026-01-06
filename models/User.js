class User {
    constructor(username, password) {
        this.username = username;
        this.password = password;

        this.borrowLimit = 3;

        this.borrowedBooks = [];   
        this.heldBooks = [];       
        this.notifications = [];  
    }

    canBorrow() {
        return this.borrowedBooks.length < this.borrowLimit;
    }

    addBorrowedBook(bookId) {
        this.borrowedBooks.push(bookId);
    }

    removeBorrowedBook(bookId) {
        this.borrowedBooks = this.borrowedBooks.filter(id => id !== bookId);
    }

    addHold(bookId) {
        if (!this.heldBooks.includes(bookId)) {
            this.heldBooks.push(bookId);
        }
    }

    removeHold(bookId) {
        this.heldBooks = this.heldBooks.filter(id => id !== bookId);
    }

    addNotification(msg) {
        this.notifications.push(msg);
    }

    clearNotifications() {
        this.notifications = [];
    }
}

module.exports = User;
