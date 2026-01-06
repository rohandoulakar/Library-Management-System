const express = require('express');
const path = require('path');

const initializeUsers = require('./models/InitializeUsers');
const initializeLibrary = require('./models/InitializeLibrary');
const LibrarySystem = require('./models/LibrarySystem');

const app = express();
const PORT = 3000;

app.use(express.json());
app.use(express.static('public'));

let users = initializeUsers();
let catalogue = initializeLibrary();
let librarySystem = new LibrarySystem(users, catalogue);

let currentUser = null;

// Response helper
function api(success, message, extra = {}) {
    return { success, message, ...extra };
}

app.get('/', (req, res) => {
    res.redirect('/login.html');
});


app.post('/api/login', (req, res) => {
    const { username, password } = req.body;

    const user = librarySystem.authenticate(username, password);
    if (!user) {
        return res.status(401).json(api(false, "Invalid username or password"));
    }

    currentUser = username;

    return res.json(api(true, "Login successful.", {
        username,
        notifications: librarySystem.getNotifications(username)
    }));
});


app.post('/api/logout', (req, res) => {
    currentUser = null;
    res.json(api(true, "Logout successful."));
});

// Session status
app.get('/api/currentUser', (req, res) => {
    if (!currentUser)
        return res.status(401).json(api(false, "No user logged in"));

    res.json(api(true, "User session active", { username: currentUser }));
});


app.get('/api/books', (req, res) => {
    const books = catalogue.getAllBooks().map(b => b.toJSON());
    res.json(books);
});


app.post('/api/borrow', (req, res) => {
    if (!currentUser)
        return res.status(401).json(api(false, "User not logged in"));

    const { bookId } = req.body;

    const result = librarySystem.borrowBook(currentUser, bookId);

    res.json(api(result.success, result.message, {
        book: result.book ? result.book.toJSON() : null,
        user: { borrowedBooks: librarySystem.getBorrowedBooks(currentUser) }
    }));
});


app.post('/api/return', (req, res) => {
    if (!currentUser)
        return res.status(401).json(api(false, "User not logged in"));

    const { bookId } = req.body;

    const result = librarySystem.returnBook(currentUser, bookId);

    res.json(api(result.success, result.message, {
        book: result.book ? result.book.toJSON() : null,
        user: { borrowedBooks: librarySystem.getBorrowedBooks(currentUser) }
    }));
});


app.post('/api/hold', (req, res) => {
    if (!currentUser)
        return res.status(401).json(api(false, "User not logged in"));

    const { bookId } = req.body;

    const result = librarySystem.placeHold(currentUser, bookId);

    res.json(api(result.success, result.message, {
        book: result.book ? result.book.toJSON() : null
    }));
});


app.get('/api/notifications', (req, res) => {
    if (!currentUser)
        return res.status(401).json(api(false, "User not logged in"));

    const notes = librarySystem.getNotifications(currentUser);

    res.json(api(true, "Notifications retrieved.", {
        notifications: notes
    }));
});

// Clear notifications
app.post('/api/notifications/clear', (req, res) => {
    if (!currentUser)
        return res.status(401).json(api(false, "User not logged in"));

    librarySystem.clearNotifications(currentUser);

    res.json(api(true, "Notifications cleared."));
});


app.get('/api/mybooks', (req, res) => {
    if (!currentUser)
        return res.status(401).json(api(false, "User not logged in"));

    const books = librarySystem.getBorrowedBooks(currentUser);
    res.json(api(true, "Borrowed books retrieved.", { books }));
});


app.post('/api/reset', (req, res) => {
    users = initializeUsers();
    catalogue = initializeLibrary();
    librarySystem = new LibrarySystem(users, catalogue);

    currentUser = null;

    res.json(api(true, "System reset."));
});

// Start server
app.listen(PORT, () => {
    console.log(`Library A3 server running at http://localhost:${PORT}`);
});
