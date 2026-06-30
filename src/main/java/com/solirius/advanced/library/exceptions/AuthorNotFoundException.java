package com.solirius.advanced.library.exceptions;

/**
 * Exception thrown when attempting to borrow a book that is already borrowed.
 */
public class AuthorNotFoundException extends Exception {
    /**
     * Constructs a new AlreadyBorrowedException with the specified message.
     *
     * @param message the detail message
     */
    public AuthorNotFoundException(final String message) {
        super(message);
    }
}
