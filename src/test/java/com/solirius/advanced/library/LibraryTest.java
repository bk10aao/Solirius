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
import com.solirius.advanced.library.exceptions.AuthorNotFoundException;
import com.solirius.advanced.library.exceptions.BookNotFoundException;
import com.solirius.advanced.library.exceptions.InvalidParameterException;
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
    void testLibraryThrowsInvalidParameterException_whenReadingInvalidBookDataFromDatabase_onConstructor() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("title")).thenReturn("");
        when(mockResultSet.getString("author")).thenReturn("Jon Skeet");
        when(mockResultSet.getBoolean("isBorrowed")).thenReturn(false);
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new Library(mockConnection));
        assertEquals("Invalid argument when reading from database: Title must not be blank.", runtimeException.getMessage());
        verify(mockResultSet).close();
    }

    @Test
    void testAddBook() throws SQLException, InvalidParameterException {
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
    void testAddBookReturnsFalse_WhenSqlExceptionThrown() throws SQLException, InvalidParameterException {
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
    void testViewAvailableBooks() throws InvalidParameterException {
        Book book1 = new Book("1984", "George Orwell");
        Book book2 = new Book("The Catcher in the Rye", "J.D. Salinger");
        library = new Library(mockConnection);
        library.addBook(book1);
        library.addBook(book2);
        List<Book> availableBooks = library.viewAvailableBooks();

        assertEquals(2, availableBooks.size());
    }

    @Test
    void testViewAllBooks() throws AlreadyBorrowedException, BookNotFoundException, InvalidParameterException {
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
    void testSearchBook_WhenBookExists() throws BookNotFoundException, InvalidParameterException {
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
    void testBorrowBook_WhenBookIsAvailable() throws AlreadyBorrowedException, BookNotFoundException, InvalidParameterException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);
        boolean result = library.borrowBook("1984");

        assertTrue(result);
        assertTrue(book.isBorrowed());
    }

    @Test
    void testBorrowBook_WhenBookIsNotAvailable() throws InvalidParameterException {
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
    void testReturnBook_WhenBookIsBorrowed() throws AlreadyBorrowedException, BookNotFoundException, NotBorrowedException, InvalidParameterException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);
        library.borrowBook("1984");

        boolean result = library.returnBook("1984");

        assertTrue(result);
        assertFalse(book.isBorrowed());
    }

    @Test
    void whenReturningBook_thatIsNotBorrows_throwsNotBorrowedException() throws InvalidParameterException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);

        assertThrows(NotBorrowedException.class, () -> library.returnBook("1984"));
    }

    @Test
    void whenReturningBook_returnsFalse_whenSqlExceptionIsThrown() throws InvalidParameterException, SQLException, NotBorrowedException, BookNotFoundException, AlreadyBorrowedException {
        Book book = new Book("1984", "George Orwell");
        library = new Library(mockConnection);
        library.addBook(book);
        library.borrowBook("1984");
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database failure"));
        boolean result = library.returnBook("1984");
        assertFalse(result);
        assertTrue(book.isBorrowed());
        assertEquals("Error returning book from library: Database failure", outputStreamCaptor.toString().trim());
    }

    @Test
    void whenReturningBook_thaytDoesNotExist_throwsBookNotFoundException() {
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
    void whenBorrowingBook_returnsFalse_whenSqlExceptionIsThrown() throws SQLException, AlreadyBorrowedException, BookNotFoundException, InvalidParameterException {
        Book book = new Book("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", "Jon Skeet");
        library = new Library(mockConnection);
        library.addBook(book);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Error"));
        boolean result = library.borrowBook(book.getTitle());
        assertFalse(result);
    }

    @Test
    void whenBorrowingBook_withNullValue_throwsInvalidParameterException() {
        library = new Library(mockConnection);
        assertThrows(InvalidParameterException.class, () -> library.borrowBook(null));
    }

    @Test
    void whenBorrowingBook_withEmptyTitle_throwsInvalidParameterException() {
        library = new Library(mockConnection);
        assertThrows(InvalidParameterException.class, () -> library.borrowBook(""));
    }

    @Test
    void whenBorrowingBook_withBlankString_throwsInvalidParameterException() {
        library = new Library(mockConnection);
        assertThrows(InvalidParameterException.class, () -> library.borrowBook("    "));
    }

    @Test
    void whenReturningBook_withNullValue_throwsInvalidParameterException() {
        library = new Library(mockConnection);
        assertThrows(InvalidParameterException.class, () -> library.returnBook(null));
    }

    @Test
    void whenReturningBook_withEmptyTitle_throwsInvalidParameterException() {
        library = new Library(mockConnection);
        assertThrows(InvalidParameterException.class, () -> library.returnBook(""));
    }

    @Test
    void whenReturningBook_withBlankString_throwsInvalidParameterException() {
        library = new Library(mockConnection);
        assertThrows(InvalidParameterException.class, () -> library.returnBook("    "));
    }

    @Test
    void whenSearchingForBook_withNullValue_throwsInvalidParameterException() {
        library = new Library(mockConnection);
        assertThrows(InvalidParameterException.class, () -> library.searchBook(null));
    }

    @Test
    void whenSearchingForBook_withEmptyString_throwsInvalidParameterException() {
        library = new Library(mockConnection);
        assertThrows(InvalidParameterException.class, () -> library.searchBook(""));
    }

    @Test
    void whenSearchingForBook_withBlankString_throwsInvalidParameterException() {
        library = new Library(mockConnection);
        assertThrows(InvalidParameterException.class, () -> library.searchBook("    "));
    }

    @Test
    void whenAdding_nullBook_throwsInvalidParameterException() {
        library = new Library(mockConnection);
        assertThrows(InvalidParameterException.class, () -> library.addBook(null));
    }

    @Test
    void whenSearchingForBooksByAuthor_withNull_throwsInvalidArgumentException() {
        library = new Library(mockConnection);
        assertThrows(InvalidParameterException.class, () -> library.getBooksByAuthor(null));
    }

    @Test
    void whenSearchingForBooksByAuthor_withEmptyString_throwsInvalidArgumentException() {
        library = new Library(mockConnection);
        assertThrows(InvalidParameterException.class, () -> library.getBooksByAuthor(""));
    }

    @Test
    void whenSearchingForBooksByAuthor_withBlankString_throwsInvalidArgumentException() {
        library = new Library(mockConnection);
        assertThrows(InvalidParameterException.class, () -> library.getBooksByAuthor("     "));
    }

    @Test
    void whenSearchingForBooksByAuthor_whereAuthorHasNoBooks_throwAuthorNotFoundException() throws InvalidParameterException {
        library = new Library(mockConnection);
        Book book = new Book("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", "Jon Skeet");
        library.addBook(book);
        assertThrows(AuthorNotFoundException.class, () -> library.getBooksByAuthor("Bob"));
    }

    @Test
    void whenSearchingForBooksByAuthor_whereAuthorOneBooks_returnsCorrectBook() throws InvalidParameterException, AuthorNotFoundException {
        library = new Library(mockConnection);
        Book book = new Book("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", "Jon Skeet");
        library.addBook(book);
        List<Book> authorBooks = library.getBooksByAuthor("Jon Skeet");
        assertEquals(1, authorBooks.size());
        Book returnedBook = authorBooks.get(0);
        assertEquals("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", returnedBook.getTitle());
        assertEquals("Jon Skeet", returnedBook.getAuthor());
    }

    @Test
    void whenSearchingForBooksByAuthor_whereAuthorHasTwoBooks_returnsCorrectBooks_sortedAlphabetically() throws InvalidParameterException, AuthorNotFoundException {
        library = new Library(mockConnection);
        Book book = new Book("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", "Jon Skeet");
        Book bookTwo = new Book("C# In deoth", "Jon Skeet");
        library.addBook(book);
        library.addBook(bookTwo);
        List<Book> authorBooks = library.getBooksByAuthor("Jon Skeet");
        assertEquals(2, authorBooks.size());
        Book returnBook = authorBooks.get(0);
        assertEquals("C# In deoth", returnBook.getTitle());
        assertEquals("Jon Skeet", returnBook.getAuthor());
        Book returnBookTwo = authorBooks.get(1);
        assertEquals("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", returnBookTwo.getTitle());
        assertEquals("Jon Skeet", returnBookTwo.getAuthor());

    }

    @Test
    void whenSearchingForBooksByAuthor_whereAuthorTwoBooks_AndThereIsASecondAuthor_returnsCorrectBooks_sortedAlphabetically() throws InvalidParameterException, AuthorNotFoundException {
        library = new Library(mockConnection);
        Book book = new Book("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", "Jon Skeet");
        Book bookTwo = new Book("C# In deoth", "Jon Skeet");
        Book bookThree = new Book("Clean Code", "Robert C. Martin");
        library.addBook(book);
        library.addBook(bookTwo);
        library.addBook(bookThree);
        List<Book> authorBooksOne = library.getBooksByAuthor("Jon Skeet");
        assertEquals(2, authorBooksOne.size());
        Book returnBookTwo = authorBooksOne.get(0);
        assertEquals("C# In deoth", returnBookTwo.getTitle());
        assertEquals("Jon Skeet", returnBookTwo.getAuthor());
        Book returnBook = authorBooksOne.get(1);
        assertEquals("Software Mistakes and Tradeoffs: How to Make Good Programming Decisions", returnBook.getTitle());
        assertEquals("Jon Skeet", returnBook.getAuthor());
        List<Book> authorBooksTwo = library.getBooksByAuthor("Robert C. Martin");
        Book returnedBookThree = authorBooksTwo.get(0);
        assertEquals("Clean Code", returnedBookThree.getTitle());
        assertEquals("Robert C. Martin", returnedBookThree.getAuthor());
    }
}
