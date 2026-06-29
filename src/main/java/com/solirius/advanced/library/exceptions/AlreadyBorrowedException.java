package com.solirius.advanced.library.exceptions;

/**
 * Exception thrown when attempting to borrow a book that is already borrowed.
 */
public class AlreadyBorrowedException extends Exception {
    /**
     * Constructs a new AlreadyBorrowedException with the specified message.
     *
     * @param message the detail message
     */
    public AlreadyBorrowedException(final String message) {
        super(message);
    }
}
