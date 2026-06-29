package com.solirius.advanced.library.exceptions;

/**
 * Exception thrown when a book is not found in the library.
 */
public class BookNotFoundException extends Exception {
    /**
     * Constructs a new BookNotFoundException with the specified message.
     *
     * @param message the detail message
     */
    public BookNotFoundException(final String message) {
        super(message);
    }
}
