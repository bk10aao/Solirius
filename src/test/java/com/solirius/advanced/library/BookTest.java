package com.solirius.advanced.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solirius.advanced.library.exceptions.InvalidParameterException;
import org.junit.jupiter.api.Test;

class BookTest {

    @Test
    void testBorrowBook_WhenNotBorrowed() throws InvalidParameterException {
        Book book = new Book("The Great Gatsby", "F. Scott Fitzgerald");
        assertFalse(book.isBorrowed());
        assertTrue(book.borrowBook());
        assertTrue(book.isBorrowed());
    }

    @Test
    void testBorrowBook_WhenAlreadyBorrowed() throws InvalidParameterException {
        Book book = new Book("The Great Gatsby", "F. Scott Fitzgerald");
        book.borrowBook(); // Borrow the book first
        assertFalse(book.borrowBook()); // Try to borrow again
    }

    @Test
    void testReturnBook_WhenBorrowed() throws InvalidParameterException {
        Book book = new Book("The Great Gatsby", "F. Scott Fitzgerald");
        book.borrowBook(); // Borrow the book
        assertTrue(book.returnBook());
        assertFalse(book.isBorrowed());
    }

    @Test
    void testReturnBook_WhenNotBorrowed() throws InvalidParameterException {
        Book book = new Book("The Great Gatsby", "F. Scott Fitzgerald");
        assertFalse(book.returnBook());
    }

    @Test
    void testToString() throws InvalidParameterException {
        Book book = new Book("The Great Gatsby", "F. Scott Fitzgerald");
        assertEquals("The Great Gatsby by F. Scott Fitzgerald (Available)", book.toString());
        book.borrowBook();
        assertEquals("The Great Gatsby by F. Scott Fitzgerald (Borrowed)", book.toString());
    }

    @Test
    void whenConstructingBook_withNullAuthorAndNullTitle_throwInvalidParameterException() {
        assertThrows(InvalidParameterException.class, () -> new Book(null, null));
    }

    @Test
    void whenConstructingBook_withNullAuthor_throwsInvalidParameterException() {
        assertThrows(InvalidParameterException.class, () -> new Book("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", null));
    }

    @Test
    void whenConstructingBook_withNullTitle_throwsInvalidParameterException() {
        assertThrows(InvalidParameterException.class, () -> new Book(null, "Jon Skeet"));
    }

    @Test
    void whenConstructingBook_withEmptyTitle_throwsInvalidParameterException() {
        assertThrows(InvalidParameterException.class, () -> new Book("", "Jon Skeet"));
    }

    @Test
    void whenConstructingBook_withBlankTitle_throwsInvalidParameterException() {
        assertThrows(InvalidParameterException.class, () -> new Book("     ", "Jon Skeet"));
    }

    @Test
    void whenConstructingBook_withBlankAuthor_throwsInvalidParameterException() {
        assertThrows(InvalidParameterException.class, () -> new Book("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", ""));
    }
}