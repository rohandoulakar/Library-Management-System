// models/LibrarySystem.js

class LibrarySystem {
    constructor(users, catalogue) {
        this.users = users;           
        this.catalogue = catalogue;   

        this.notifications = {};
        Object.keys(users).forEach(u => this.notifications[u] = []);
    }

    authenticate(username, password) {
        const user = this.users[username];
        if (!user || user.password !== password) return null;

        // DO NOT clear notifications on login!
        this._checkHeldBookAvailability(username);

        return user;
    }

    _checkHeldBookAvailability(username) {
        const user = this.users[username];
        const notes = this.notifications[username];

        user.heldBooks.forEach(bookId => {
            const book = this.catalogue.getBook(bookId);

            if (book && book.status === "available") {
                const msg = `Book available: ${book.title}`;

                // Avoid duplicates
                if (!notes.includes(msg)) {
                    notes.push(msg);
                }
            }
        });
    }

    borrowBook(username, bookId) {
        const user = this.users[username];
        const book = this.catalogue.getBook(bookId);

        if (!user || !book)
            return { success: false, message: "Invalid user or book." };

        // Borrowing limit
        if (user.borrowedBooks.length >= user.borrowLimit)
            return { success: false, message: "Borrowing limit reached." };

        if (book.status === "available") {
            this._borrowTransition(book, user);
            return {
                success: true,
                message: `Successfully borrowed ${book.title}.`,
                book
            };
        }

        // Book on hold and user is first in queue
        if (book.status === "on_hold") {
            const next = book.holdQueue[0];

            if (next === username) {
                // Remove from queue
                book.holdQueue.shift();

                // Remove from heldBooks
                user.removeHold(bookId);

                this._borrowTransition(book, user);

                return {
                    success: true,
                    message: `Successfully borrowed ${book.title}.`,
                    book
                };
            }

            return { success: false, message: "Book reserved for another user." };
        }

        return { success: false, message: "Book is already checked out." };
    }

    _borrowTransition(book, user) {
        book.status = "checked_out";
        book.currentHolder = user.username;
        book.dueDate = this._assignDueDate();
        user.borrowedBooks.push(book.id);
    }

    _assignDueDate() {
        const d = new Date();
        d.setDate(d.getDate() + 14);
        return d.toISOString().split("T")[0];
    }

    returnBook(username, bookId) {
        const user = this.users[username];
        const book = this.catalogue.getBook(bookId);

        if (!user || !book)
            return { success: false, message: "Invalid user or book." };

        // Must currently hold the book
        if (book.currentHolder !== username)
            return { success: false, message: "You cannot return a book you didn't borrow." };

        user.removeBorrowedBook(bookId);

        book.currentHolder = null;
        book.dueDate = null;

        if (book.holdQueue.length > 0) {
            const nextUser = book.holdQueue[0];
            book.status = "on_hold";

            // Only notification we keep
            this.notifications[nextUser].push(
                `${book.title} is now available for you`
            );

            return {
                success: true,
                message: `Returned ${book.title}. Hold queue user notified.`,
                book
            };
        }

        book.status = "available";

        return {
            success: true,
            message: `Successfully returned ${book.title}.`,
            book
        };
    }

    placeHold(username, bookId) {
        const user = this.users[username];
        const book = this.catalogue.getBook(bookId);
    
        if (!user || !book)
            return { success: false, message: "Invalid user or book." };
    
        // If book is already borrowed
        if (user.borrowedBooks.includes(bookId))
            return { success: false, message: "You already borrowed this book." };
    
        if (user.heldBooks.includes(bookId) || book.holdQueue.includes(username))
            return { success: false, message: "You already placed a hold on this book." };
    
        if (book.status === "available") {
    
            if (user.borrowedBooks.length < user.borrowLimit) {
                return {
                    success: false,
                    message: "Book is available — you can borrow it now."
                };
            }
    
            user.addHold(bookId);
            book.holdQueue.push(username);
            book.status = "on_hold";
    
            return {
                success: true,
                message: `Hold placed on ${book.title}.`,
                book
            };
        }
        user.addHold(bookId);
        book.holdQueue.push(username);
        book.status = "on_hold";
    
        return {
            success: true,
            message: `Hold placed on ${book.title}.`,
            book
        };
    } 

    getNotifications(username) {
        return this.notifications[username] || [];
    }

    clearNotifications(username) {
        this.notifications[username] = [];
    }

    getBorrowedBooks(username) {
        const user = this.users[username];
        if (!user) return [];

        return user.borrowedBooks.map(id => {
            const b = this.catalogue.getBook(id);
            return {
                id: b.id,
                title: b.title,
                dueDate: b.dueDate,
                status: b.status
            };
        });
    }
}

module.exports = LibrarySystem;
