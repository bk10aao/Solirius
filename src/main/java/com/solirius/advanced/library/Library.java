package com.solirius.advanced.library;

import com.solirius.advanced.library.exceptions.AlreadyBorrowedException;
import com.solirius.advanced.library.exceptions.AuthorNotFoundException;
import com.solirius.advanced.library.exceptions.BookNotFoundException;
import com.solirius.advanced.library.exceptions.InvalidParameterException;
import com.solirius.advanced.library.exceptions.NotBorrowedException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
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
        Statement libraryStatement = null;
        ResultSet libraryResultSet = null;
        try {
            // Creates a table for books if not existing
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS books (title TEXT, author TEXT, isBorrowed BOOLEAN)");
            // Query the books table
            libraryStatement = connection.createStatement();
            libraryResultSet = libraryStatement.executeQuery("SELECT title, author, isBorrowed FROM books");
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
        } catch (SQLException e) {
            System.out.println("Error in connecting to the library database: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (InvalidParameterException e) {
            throw new RuntimeException("Invalid argument when reading from database: " + e.getMessage());
        } finally {
            try {
                if (libraryResultSet != null) {
                    libraryResultSet.close();
                }
                if (libraryStatement != null) {
                    libraryStatement.close();
                }
            } catch (SQLException e) {
                System.err.println("Failed to close database resources: " + e.getMessage());
            }
        }
    }

    /**
     * Adds a book to the library.
     *
     * @param book the book to add
     * @return true if successful, otherwise false
     * @throws InvalidParameterException if the book is null
     */
    public boolean addBook(final Book book) throws InvalidParameterException {
        if(book == null) {
            throw new InvalidParameterException("Book must not be null.");
        }
        String query = "INSERT INTO books (title, author, isBorrowed) VALUES (?, ?, ?)";
        try(var preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, book.getTitle().trim());
            preparedStatement.setString(2, book.getAuthor().trim());
            preparedStatement.setBoolean(3, book.isBorrowed());
            preparedStatement.executeUpdate();
            books.add(book);
            return true;
        } catch (SQLException e) {
            System.out.println("Error adding book to the library: " + e.getMessage());
            return false;
        }
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
        return new ArrayList<>(books);
    }

    /**
     * Searches for a book by its title.
     *
     * @param title the title of the book to search
     * @return the book if found, otherwise throws BookNotFoundException
     * @throws InvalidParameterException if title is null, empty or blank
     */
    public Book searchBook(final String title) throws BookNotFoundException, InvalidParameterException {
        validateTitle(title);
        return books.stream()
            .filter(book -> book.getTitle().equalsIgnoreCase(title.trim()))
            .findFirst()
            .orElseThrow(() -> new BookNotFoundException(BOOK_NOT_FOUND));
    }

    /**
     * Gets all books by author.
     *
     * @param author the author of the books to get from library
     * @return a list of books by the author
     * @throws InvalidParameterException if author is null, empty ot blank
     * @throws AuthorNotFoundException if author is not found
     */
    public List<Book> getBooksByAuthor(final String author) throws InvalidParameterException, AuthorNotFoundException {
        validateAuthor(author);
        List<Book> authorBooks =  books.stream()
                                        .filter(book -> book.getAuthor().equalsIgnoreCase(author.trim()))
                                        .sorted(Comparator.comparing(Book::getTitle, String::compareToIgnoreCase))
                                        .collect(Collectors.toList());
        if(authorBooks.isEmpty()) {
            throw new AuthorNotFoundException("No books found for author: " + author);
        }
        return new ArrayList<>(authorBooks);
    }

    /**
     * Borrows a book by its title.
     *
     * @param title the title of the book to borrow
     * @return true if the book is successfully borrowed
     * @throws AlreadyBorrowedException if book is already borrowed
     * @throws InvalidParameterException if title is null or is blank
     * @throws BookNotFoundException if book is not found
     */
    public boolean borrowBook(final String title) throws BookNotFoundException, AlreadyBorrowedException, InvalidParameterException {
        validateTitle(title);
        Book book = searchBook(title.trim());
        if (book.isBorrowed()) {
            throw new AlreadyBorrowedException(BOOK_ALREADY_BORROWED);
        }
        String borrowQuery = "UPDATE books SET isBorrowed = ? WHERE title = ?";
        try(var preparedStatement = connection.prepareStatement(borrowQuery)) {
            preparedStatement.setBoolean(1, true);
            preparedStatement.setString(2, book.getTitle());
            preparedStatement.executeUpdate();
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
     * @throws InvalidParameterException if title is null or is blank
     * @throws BookNotFoundException if no book exists with title
     * @throws NotBorrowedException if the book is no borrowed
     */
    public boolean returnBook(final String title) throws BookNotFoundException, NotBorrowedException, InvalidParameterException {
        validateTitle(title);
        Book book = searchBook(title.trim());
        if (!book.isBorrowed()) {
            throw new NotBorrowedException(BOOK_NOT_BORROWED);
        }
        String returnQuery = "UPDATE books SET isBorrowed = ? WHERE title = ?";
        try(var preparedStatement = connection.prepareStatement(returnQuery)) {
            preparedStatement.setBoolean(1, false);
            preparedStatement.setString(2, book.getTitle());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error returning book from library: " + e.getMessage());
            return false;
        }
        return book.returnBook();
    }

    private static void validateTitle(final String title) throws InvalidParameterException {
        if(title == null)
            throw new InvalidParameterException("Title must not be null.");
        if(title.trim().isBlank())
            throw new InvalidParameterException("Tile must not be blank.");
    }

    private static void validateAuthor(final String author) throws InvalidParameterException {
        if(author == null)
            throw new InvalidParameterException("Author must not be null.");
        if(author.trim().isBlank())
            throw new InvalidParameterException("Author must not be blank.");
    }
}
