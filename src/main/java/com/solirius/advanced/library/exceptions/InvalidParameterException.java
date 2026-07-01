package com.solirius.advanced.library.exceptions;

/**
 * Exception thrown when entering an invalid parameter.
 */
public class InvalidParameterException extends Exception {

    /**
     * Constructs a new InvalidParameterException with the specified message.
     *
     * @param message the detail message
     */
    public InvalidParameterException(final String message) {
        super(message);
    }
}
