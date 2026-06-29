package com.solirius.advanced.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.solirius.advanced.library.exceptions.AlreadyBorrowedException;
import com.solirius.advanced.library.exceptions.BookNotFoundException;
import com.solirius.advanced.library.exceptions.NotBorrowedException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

class LibraryTest {
    private Library library;

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private PreparedStatement mockPreparedStatement;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    void testLibraryConstructor_WhenDatabaseIsNotEmpty() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("title")).thenReturn("1984");
        when(mockResultSet.getString("author")).thenReturn("George Orwell");
        when(mockResultSet.getBoolean("isBorrowed")).thenReturn(false);
        library = new Library(mockConnection);
        verify(mockConnection, times(2)).createStatement();
        verify(mockStatement).executeQuery(anyString());
        verify(mockResultSet, times(2)).next();
        verify(mockResultSet).getString("title");
        verify(mockResultSet).getString("author");
        verify(mockResultSet).getBoolean("isBorrowed");
        verify(mockResultSet).close();
        verify(mockStatement).close();
        List<Book> books = library.viewAllBooks();
        assertEquals(1, books.size());
        assertEquals("1984", books.get(0).getTitle());
    }

    @Test
    void testLibraryConstructor_WhenDatabaseIsEmpty() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);
        when(mockResultSet.getString("title")).thenReturn("1984");
        when(mockResultSet.getString("author")).thenReturn("George Orwell");
        when(mockResultSet.getBoolean("isBorrowed")).thenReturn(false);
        library = new Library(mockConnection);
        verify(mockConnection, times(2)).createStatement();
        verify(mockStatement).executeQuery(anyString());
        verify(mockResultSet).next();
        verify(mockResultSet, never()).getString(anyString());
        verify(mockResultSet, never()).getBoolean(anyString());
        verify(mockResultSet).close();
        verify(mockStatement).close();
        List<Book> books = library.viewAllBooks();
        assertEquals(0, books.size());
    }

    @Test
    void testLibraryThrowsRuntimeException_WhenDatabaseConnectionThrows() throws SQLException {
        when(mockConnection.createStatement()).thenThrow(new SQLException("Test message."));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> new Library(mockConnection));
        assertEquals("java.sql.SQLException: Test message.", exception.getMessage());
        assertEquals("Error in connecting to the library database: Test message.", outputStreamCaptor.toString().trim());
    }

    @Test
    void testAddBook() throws SQLException {
        Book book = new Book("Brave New World", "Aldous Huxley");
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        library = new Library(mockConnection);
        boolean result = library.addBook(book);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, "Brave New World");
        verify(mockPreparedStatement).setString(2, "Aldous Huxley");
        verify(mockPreparedStatement).setBoolean(3, false);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testAddBookReturnsFalse_WhenSqlExceptionThrown() throws SQLException {
        Book book = new Book("Brave New World", "Aldous Huxley");
        library = new Library(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test message."));
        boolean result = library.addBook(book);

        assertFalse(result);
        verify(mockPreparedStatement, never()).setString(1, "Brave New World");
        verify(mockPreparedStatement, never()).setString(2, "Aldous Huxley");
        verify(mockPreparedStatement, never()).setBoolean(3, false);
        verify(mockPreparedStatement, never()).executeUpdate();
        assertEquals("Error adding book to the library: Test message.", outputStreamCaptor.toString().trim());
    }

    @Test
    void testViewAvailableBooks() {
        Book book1 = new Book("1984", "George Orwell");
        Book book2 = new Book("The Catcher in the Rye", "J.D. Salinger");
        library = new Library(mockConnection);
        library.addBook(book1);
        library.addBook(book2);
        List<Book> availableBooks = library.viewAvailableBooks();

        assertEquals(2, availableBooks.size());
    }

    @Test
    void testViewAllBooks() throws AlreadyBorrowedException, BookNotFoundException {
        Book book1 = new Book("1984", "George Orwell");
        Book book2 = new Book("The Catcher in the Rye", "J.D. Salinger");
        library = new Library(mockConnection);
        library.addBook(book1);
        library.addBook(book2);
        library.borrowBook("1984");
        List<Book> availableBooks = library.viewAvailableBooks();
        List<Book> allBooks = library.viewAllBooks();

        assertEquals(1, availableBooks.size());
        assertEquals(2, allBooks.size());
    }

    @Test
    void testSearchBook_WhenBookExists() throws BookNotFoundException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);
        Book foundBook = library.searchBook("1984");

        assertNotNull(foundBook);
        assertEquals("1984", foundBook.getTitle());
    }

    @Test
    void testSearchBook_WhenBookDoesNotExist() {
        library = new Library(mockConnection);
        assertThrows(BookNotFoundException.class, () -> library.searchBook("Nonexistent Book"));
    }

    @Test
    void testBorrowBook_WhenBookIsAvailable() throws AlreadyBorrowedException, BookNotFoundException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);
        boolean result = library.borrowBook("1984");

        assertTrue(result);
        assertTrue(book.isBorrowed());
    }

    @Test
    void testBorrowBook_WhenBookIsNotAvailable() {
        Book book = new Book("1984", "George Orwell");
        book.borrowBook();
        library = new Library(mockConnection);
        library.addBook(book);

        assertThrows(AlreadyBorrowedException.class, () -> library.borrowBook("1984"));
    }

    @Test
    void testBorrowBook_WhenBookDoesNotExist() {
        library = new Library(mockConnection);
        assertThrows(BookNotFoundException.class, () -> library.borrowBook("Nonexistent Book"));
    }

    @Test
    void testReturnBook_WhenBookIsBorrowed() throws AlreadyBorrowedException, BookNotFoundException, NotBorrowedException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);
        library.borrowBook("1984");

        boolean result = library.returnBook("1984");

        assertTrue(result);
        assertFalse(book.isBorrowed());
    }

    @Test
    void testReturnBook_WhenBookIsNotBorrowed() {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);

        assertThrows(NotBorrowedException.class, () -> library.returnBook("1984"));
    }

    @Test
    void testReturnBook_WhenBookDoesNotExist() {
        library = new Library(mockConnection);
        assertThrows(BookNotFoundException.class, () -> library.returnBook("Nonexistent Book"));
    }

    @Test
    void givenLibraryInitialisedWithOneBookBorrowed_loadsBookAsBorrowed() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("title")).thenReturn("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions");
        when(mockResultSet.getString("author")).thenReturn("Jon Skeet");
        when(mockResultSet.getBoolean("isBorrowed")).thenReturn(true);
        library = new Library(mockConnection);
        List<Book> books = library.viewAllBooks();
        assertEquals(1, books.size());
        assertTrue(books.get(0).isBorrowed());
    }

    @Test
    void testSearchBook_byAuthorName_Jon_Skeet_returnsCorrectBookTitle() throws BookNotFoundException {
        Book book = new Book("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", "Jon Skeet");
        library = new Library(mockConnection);
        library.addBook(book);
        Book foundBook = library.searchBook("Jon Skeet");
        assertNotNull(foundBook);
        assertEquals("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", foundBook.getTitle());
        assertEquals("Jon Skeet", foundBook.getAuthor());
    }

    @Test
    void testSearchBook_byAuthorName_Not_Present_throwsBookNotFoundException() {
        Book book = new Book("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", "Jon Skeet");
        library = new Library(mockConnection);
        library.addBook(book);
        assertThrows(BookNotFoundException.class, () -> library.searchBook("Not_Present"));
    }

    @Test
    void whenBorrowingBook_returnsFalse_whenSqlExceptionIsThrown() throws SQLException, AlreadyBorrowedException, BookNotFoundException {
        Book book = new Book("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", "Jon Skeet");
        library = new Library(mockConnection);
        library.addBook(book);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Error"));
        boolean result = library.borrowBook(book.getTitle());
        assertFalse(result);
    }

    @Test
    void whenReturningBook_returnsFalse_whenSqlExceptionIsThrown() throws SQLException, BookNotFoundException, NotBorrowedException, AlreadyBorrowedException {
        Book book = new Book("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", "Jon Skeet");
        library = new Library(mockConnection);
        library.addBook(book);
        library.borrowBook("Jon Skeet");
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Error"));
        boolean result = library.returnBook("Jon Skeet");
        assertFalse(result);
    }
}