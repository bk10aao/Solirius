package com.solirius.advanced.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BookTest {

    @Test
    void testBorrowBook_WhenNotBorrowed() {
        Book book = new Book("The Great Gatsby", "F. Scott Fitzgerald");
        assertFalse(book.isBorrowed());
        assertTrue(book.borrowBook());
        assertTrue(book.isBorrowed());
    }

    @Test
    void testBorrowBook_WhenAlreadyBorrowed() {
        Book book = new Book("The Great Gatsby", "F. Scott Fitzgerald");
        book.borrowBook(); // Borrow the book first
        assertFalse(book.borrowBook()); // Try to borrow again
    }

    @Test
    void testReturnBook_WhenBorrowed() {
        Book book = new Book("The Great Gatsby", "F. Scott Fitzgerald");
        book.borrowBook(); // Borrow the book
        assertTrue(book.returnBook());
        assertFalse(book.isBorrowed());
    }

    @Test
    void testReturnBook_WhenNotBorrowed() {
        Book book = new Book("The Great Gatsby", "F. Scott Fitzgerald");
        assertFalse(book.returnBook());
    }

    @Test
    void testToString() {
        Book book = new Book("The Great Gatsby", "F. Scott Fitzgerald");
        assertEquals("The Great Gatsby by F. Scott Fitzgerald (Available)", book.toString());
        book.borrowBook();
        assertEquals("The Great Gatsby by F. Scott Fitzgerald (Borrowed)", book.toString());
    }
}