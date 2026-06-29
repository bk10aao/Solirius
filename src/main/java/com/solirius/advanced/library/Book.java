package com.solirius.advanced.library;

/**
 * Represents a book in the library.
 */
public class Book {

    /**
     * The book's title.
     */
    private final String title;

    /**
     * The book's author.
     */
    private final String author;

    /**
     * Defines whether the book has been borrowed.
     */
    private boolean isBorrowed;

    /**
     * Creates a book with a title and author.
     * The book is initially not borrowed.
     *
     * @param theTitle  the title of the book
     * @param theAuthor the author of the book
     */
    public Book(final String theTitle, final String theAuthor) {
        this.title = theTitle;
        this.author = theAuthor;
        this.isBorrowed = false;
    }

    /**
     * Borrows the book if it's not already borrowed.
     *
     * @return true if the book is successfully borrowed, otherwise false
     */
    public boolean borrowBook() {
        if (!isBorrowed) {
            isBorrowed = true;
            return true;
        }
        return false;
    }

    /**
     * Returns the book if it's currently borrowed.
     *
     * @return true if the book is successfully returned, otherwise false
     */
    public boolean returnBook() {
        if (isBorrowed) {
            isBorrowed = false;
            return true;
        }
        return false;
    }

    /**
     * Helps to print a book's features.
     * @return a string representation of the book.
     */
    @Override
    public String toString() {
        return title + " by " + author
                + " (" + (isBorrowed ? "Borrowed" : "Available") + ")";
    }

    /**
     * Gets the title of the book.
     *
     * @return the title of the book
     */
    public String getTitle() {
        return title;
    }

    /**
     * Checks if the book is borrowed.
     *
     * @return true if the book is borrowed, otherwise false
     */
    public boolean isBorrowed() {
        return isBorrowed;
    }

    /**
     * Gets the author of the book.
     *
     * @return the author of the book
     */
    public String getAuthor() {
        return author;
    }
}
