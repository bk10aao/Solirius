package com.solirius.advanced.library;

import com.solirius.advanced.library.exceptions.AlreadyBorrowedException;
import com.solirius.advanced.library.exceptions.BookNotFoundException;
import com.solirius.advanced.library.exceptions.NotBorrowedException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a library that holds a collection of books.
 */
public class Library {

    /**
     * Library Books.
     */
    private final List<Book> books;

    /**
     * SqlLite Connection.
     */
    private final Connection connection;

    public static final String BOOK_NOT_FOUND = "Book not found: Book is not in the library.";
    public static final String BOOK_ALREADY_BORROWED = "Book not borrowed: Book has already been borrowed.";
    public static final String BOOK_NOT_BORROWED = "Book not returned: Book has not been borrowed.";

    /**
     * Creates a library from SqlLite if not existing else updates book list with entries.
     */
    public Library(final Connection connection) {
        this.books = new ArrayList<>();
        this.connection = connection;
        try {
            // Creates a table for books if not existing
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS books (title TEXT, author TEXT, isBorrowed BOOLEAN)");
            // Query the books table
            Statement libraryStatement = connection.createStatement();
            ResultSet libraryResultSet = libraryStatement.executeQuery("SELECT title, author, isBorrowed FROM books");
            // Add books from the table to the library
            while (libraryResultSet.next()) {
                String title = libraryResultSet.getString("title");
                String author = libraryResultSet.getString("author");
                boolean isBorrowed = libraryResultSet.getBoolean("isBorrowed");
                Book book = new Book(title, author);
                if (isBorrowed) {
                    book.borrowBook();
                }
                this.books.add(book);
            }
            libraryResultSet.close();
            libraryStatement.close();
        } catch (SQLException e) {
            System.out.println("Error in connecting to the library database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a book to the library.
     *
     * @param book the book to add
     * @return true if successful, otherwise false
     */
    public boolean addBook(final Book book) {
        try {
            String query = "INSERT INTO books (title, author, isBorrowed) VALUES (?, ?, ?)";
            var preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, book.getTitle());
            preparedStatement.setString(2, book.getAuthor());
            preparedStatement.setBoolean(3, book.isBorrowed());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            books.add(book);
        } catch (SQLException e) {
            System.out.println("Error adding book to the library: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Gets a list of available books.
     *
     * @return a list of books that are not borrowed
     */
    public List<Book> viewAvailableBooks() {
        return books.stream().filter(book -> !book.isBorrowed()).collect(Collectors.toList());
    }

    /**
     * Gets a list of all books.
     *
     * @return a list of all books in library, borrowed or not.
     */
    public List<Book> viewAllBooks() {
        return books;
    }

    /**
     * Searches for a book by its title.
     *
     * @param titleAuthor the title or author of the book to search
     * @return the book if found, otherwise throws BookNotFoundException
     */
    public Book searchBook(final String titleAuthor) throws BookNotFoundException {
        return books.stream()
            .filter(book -> book.getTitle().equalsIgnoreCase(titleAuthor) || book.getAuthor().equalsIgnoreCase(titleAuthor))
            .findFirst()
            .orElseThrow(() -> new BookNotFoundException(BOOK_NOT_FOUND));
    }

    /**
     * Borrows a book by its title.
     *
     * @param title the title of the book to borrow
     * @return true if the book is successfully borrowed
     */
    public boolean borrowBook(final String title) throws BookNotFoundException, AlreadyBorrowedException {
        if(title == null) {
            throw new IllegalArgumentException("Title must not be null");
        }
        if(title.isBlank()) {
            throw new IllegalArgumentException("Title must not be blank");
        }
        Book book = searchBook(title);
        if (book.isBorrowed()) {
            throw new AlreadyBorrowedException(BOOK_ALREADY_BORROWED);
        }
        String borrowQuery = "UPDATE books SET isBorrowed = ? WHERE title = ?";
        try {
            var preparedStatement = connection.prepareStatement(borrowQuery);
            preparedStatement.setBoolean(1, true);
            preparedStatement.setString(2, book.getTitle());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("Error borrowing book from library: " + e.getMessage());
            return false;
        }
        return book.borrowBook();
    }

    /**
     * Returns a book by its title.
     *
     * @param title the title of the book to return
     * @return true if the book is successfully returned, otherwise false
     */
    public boolean returnBook(final String title) throws BookNotFoundException, NotBorrowedException {
        if(title == null) {
            throw new IllegalArgumentException("Title must not be null");
        }
        if(title.isBlank()) {
            throw new IllegalArgumentException("Title must not be blank");
        }
        Book book = searchBook(title);
        if (!book.isBorrowed()) {
            throw new NotBorrowedException(BOOK_NOT_BORROWED);
        }
        String returnQuery = "UPDATE books SET isBorrowed = ? WHERE title = ?";
        try {
            var preparedStatement = connection.prepareStatement(returnQuery);
            preparedStatement.setBoolean(1, false);
            preparedStatement.setString(2, book.getTitle());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("Error returning book from library: " + e.getMessage());
            return false;
        }
        return book.returnBook();
    }
}
