package com.solirius.advanced.library.exceptions;

/**
 * Exception thrown when attempting to return a book that was not borrowed.
 */
public class NotBorrowedException extends Exception {
    /**
     * Constructs a new NotBorrowedException with the specified message.
     *
     * @param message the detail message
     */
    public NotBorrowedException(final String message) {
        super(message);
    }
}
