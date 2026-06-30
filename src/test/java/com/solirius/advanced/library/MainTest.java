package com.solirius.advanced.library;

import static com.solirius.advanced.library.Main.*;
import static com.solirius.advanced.library.Library.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.solirius.advanced.library.exceptions.AlreadyBorrowedException;
import com.solirius.advanced.library.exceptions.AuthorNotFoundException;
import com.solirius.advanced.library.exceptions.BookNotFoundException;
import com.solirius.advanced.library.exceptions.InvalidParameterException;
import com.solirius.advanced.library.exceptions.NotBorrowedException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.SQLException;

class MainTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private Library mockLibrary;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private Book book1;
    private Book book2;
    private Book book3;

    @BeforeEach
    void setUp() throws SQLException, BookNotFoundException, AlreadyBorrowedException, NotBorrowedException, InvalidParameterException {
        MockitoAnnotations.openMocks(this);
        book1 = new Book("Mock Title 1", "Mock Author 2");
        book2 = new Book("Mock Title 2", "Mock Author 3");
        book3 = new Book("Mock Title 3", "Mock Author 1");
        when(mockConnection.createStatement()).thenThrow(new SQLException("Mocked connection"));
        when(mockLibrary.addBook(any(Book.class))).thenReturn(true);
        when(mockLibrary.searchBook(anyString())).thenReturn(book1);
        when(mockLibrary.borrowBook(anyString())).thenReturn(true);
        when(mockLibrary.returnBook(anyString())).thenReturn(true);

        Main.setLibrary(mockLibrary);

        when(mockLibrary.viewAvailableBooks()).thenReturn(List.of(book1, book3));
        when(mockLibrary.viewAllBooks()).thenReturn(List.of(book1, book2, book3));
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    void testMain_InvalidChoice() {
        String input = "9\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Main.main(new String[]{});

        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(INVALID));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_AddBook() throws InvalidParameterException {
        String input = "1\nMock Title\nMock Author\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Main.main(new String[]{});

        verify(mockLibrary).addBook(any(Book.class));
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(ENTER_TITLE));
        assertTrue(output.contains(ENTER_AUTHOR));
        assertTrue(output.contains(BOOK_ADDED));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_AddBookFail() throws InvalidParameterException {
        String input = "1\nMock Title 1\nMock Author 2\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        when(mockLibrary.addBook(any())).thenReturn(false);

        Main.main(new String[]{});

        verify(mockLibrary).addBook(any(Book.class));
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(ENTER_TITLE));
        assertTrue(output.contains(ENTER_AUTHOR));
        assertTrue(output.contains(BOOK_NOT_ADDED));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_ViewAvailableBooks_SortedInvalid() {
        String input = "2\n3\n2\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Main.main(new String[]{});

        verify(mockLibrary).viewAvailableBooks();
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(SORT_MENU));
        assertTrue(output.contains(INVALID));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_ViewAvailableBooks_SortedByAuthor() {
        String input = "2\n1\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Main.main(new String[]{});

        verify(mockLibrary).viewAvailableBooks();
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(SORT_MENU));
        assertTrue(output.contains(AVAILABLE_BOOKS));
        assertTrue(output.contains(book3.toString() + "\n" + book1.toString()));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_ViewAvailableBooks_SortedByTitle() {
        String input = "2\n2\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Main.main(new String[]{});

        verify(mockLibrary).viewAvailableBooks();
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(SORT_MENU));
        assertTrue(output.contains(AVAILABLE_BOOKS));
        assertTrue(output.contains(book1.toString() + "\n" + book3.toString()));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_ViewAllBooks_SortedInvalid() {
        String input = "3\n3\n2\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Main.main(new String[]{});

        verify(mockLibrary).viewAllBooks();
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(SORT_MENU));
        assertTrue(output.contains(INVALID));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_ViewAllBooks_SortedByAuthor() {
        String input = "3\n1\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Main.main(new String[]{});

        verify(mockLibrary).viewAllBooks();
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(SORT_MENU));
        assertTrue(output.contains(ALL_BOOKS));
        assertTrue(output.contains(book3.toString() + "\n" + book1.toString() + "\n" + book2.toString()));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_ViewAllBooks_SortedByTitle() {
        String input = "3\n2\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Main.main(new String[]{});

        verify(mockLibrary).viewAllBooks();
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(SORT_MENU));
        assertTrue(output.contains(ALL_BOOKS));
        assertTrue(output.contains(book1.toString() + "\n" + book2.toString() + "\n" + book3.toString()));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_SearchBook() throws BookNotFoundException, InvalidParameterException {
        String input = "4\nMock Title 1\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Main.main(new String[]{});

        verify(mockLibrary).searchBook("Mock Title 1");
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(TITLE_AUTHOR_SEARCH));
        assertTrue(output.contains(book1.toString()));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_SearchBookFailed() throws BookNotFoundException, InvalidParameterException {
        String input = "4\nMock Title\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        when(mockLibrary.searchBook(anyString())).thenThrow(new BookNotFoundException(BOOK_NOT_FOUND));

        Main.main(new String[]{});

        verify(mockLibrary).searchBook("Mock Title");
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(TITLE_AUTHOR_SEARCH));
        assertTrue(output.contains(BOOK_NOT_FOUND));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_BorrowBook() throws BookNotFoundException, AlreadyBorrowedException, InvalidParameterException  {
        String input = "5\nMock Title\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Main.main(new String[]{});

        verify(mockLibrary).borrowBook("Mock Title");
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(TITLE_BORROW));
        assertTrue(output.contains(BORROWED));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_BorrowBookFailed() throws BookNotFoundException, AlreadyBorrowedException, InvalidParameterException  {
        String input = "5\nMock Title\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        when(mockLibrary.borrowBook(anyString())).thenThrow(new AlreadyBorrowedException(BOOK_ALREADY_BORROWED));

        Main.main(new String[]{});

        verify(mockLibrary).borrowBook("Mock Title");
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(TITLE_BORROW));
        assertTrue(output.contains(BOOK_ALREADY_BORROWED));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_ReturnBook() throws BookNotFoundException, NotBorrowedException, InvalidParameterException  {
        String input = "6\nMock Title\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Main.main(new String[]{});

        verify(mockLibrary).returnBook("Mock Title");
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(TITLE_RETURN));
        assertTrue(output.contains(RETURNED));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_ReturnBookFailed() throws BookNotFoundException, NotBorrowedException, InvalidParameterException  {
        String input = "6\nMock Title\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        when(mockLibrary.returnBook(anyString())).thenThrow(new NotBorrowedException(BOOK_NOT_BORROWED));

        Main.main(new String[]{});

        verify(mockLibrary).returnBook("Mock Title");
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(TITLE_RETURN));
        assertTrue(output.contains(BOOK_NOT_BORROWED));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_Exit() {
        String input = "8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        Main.main(new String[]{});

        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(OPENED));
        assertTrue(output.contains(WELCOME));
        assertTrue(output.contains(MENU));
        assertTrue(output.contains(EXIT));
    }

    @Test
    void testMain_getBooksByAuthor() throws InvalidParameterException, AuthorNotFoundException {
        String input = "7\nMock Author 1\n8\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        when(mockLibrary.getBooksByAuthor("Mock Author 1")).thenReturn(List.of(book3));
        Main.main(new String[]{});
        verify(mockLibrary).getBooksByAuthor("Mock Author 1");
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(BOOKS_BY_AUTHOR));
        assertTrue(output.contains(book3.toString()));
        assertTrue(output.contains(EXIT));
    }
}