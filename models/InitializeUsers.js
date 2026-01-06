const User = require("./User");

function initializeUsers() {
    const users = {};

    users["alice"] = new User("alice", "pass123");
    users["bob"] = new User("bob", "pass456");
    users["charlie"] = new User("charlie", "pass789");

    return users;
}

module.exports = initializeUsers;
