package org.example.library;

import java.util.ArrayList;
import java.util.List;

public class Catalogue {
    ArrayList<Book> catalogue;

    public Catalogue() {
        catalogue = new ArrayList<>();
    }

    public void addBook(Book book) {
        catalogue.add(book);
    }

    public Book getBook(int index) {
        return catalogue.get(index);
    }

    public int getCatalogueSize() {
        return catalogue.size();
    }

    public List<Book> getBooks() {
        return catalogue;
    }

}
