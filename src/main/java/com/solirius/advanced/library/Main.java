package com.solirius.advanced.library;

import com.solirius.advanced.library.exceptions.AlreadyBorrowedException;
import com.solirius.advanced.library.exceptions.BookNotFoundException;
import com.solirius.advanced.library.exceptions.InvalidParameterException;
import com.solirius.advanced.library.exceptions.NotBorrowedException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.Scanner;

public final class Main {

    /**
     * The main menu.
     */
    public static final String MENU = "\nMenu:"
        + "\n1. Add a new book"
        + "\n2. View available books"
        + "\n3. View all books"
        + "\n4. Search for a book"
        + "\n5. Borrow a book"
        + "\n6. Return a book"
        + "\n7. Exit"
        + "\nEnter your choice: ";

    /**
     * The main menu.
     */
    public static final String SORT_MENU = "\nSort by:"
        + "\n1. Author"
        + "\n2. Title"
        + "\nEnter your choice: ";

    /**
     * Option to sort by author.
     */
    public static final int AUTHOR = 1;

    /**
     * Option to sort by title.
     */
    public static final int TITLE = 2;

    /**
     * Option to add a Book.
     */
    public static final int ADD_A_BOOK_OPTION = 1;

    /**
     * Option to list available Books.
     */
    public static final int LIST_AVAILABLE_BOOKS = 2;

    /**
     * Option to list all Books.
     */
    public static final int LIST_ALL_BOOKS = 3;

    /**
     * Option to search a Book.
     */
    public static final int SEARCH_BOOK_OPTION = 4;

    /**
     * Option to borrow a Book.
     */
    public static final int BORROW_BOOK_OPTION = 5;

    /**
     * Option to return a Book.
     */
    public static final int RETURN_BOOK_OPTION = 6;

    /**
     * Option to terminate the program.
     */
    public static final int EXIT_OPTION = 7;

    /**
     * String constants.
     */
    public static final String OPENED = "Opened library database successfully";
    public static final String WELCOME = "Welcome to the Library Management System!";
    public static final String ENTER_TITLE = "Enter book title: ";
    public static final String ENTER_AUTHOR = "Enter book author: ";
    public static final String BOOK_ADDED = "Book added successfully!";
    public static final String BOOK_NOT_ADDED = "Book not added.";
    public static final String ALL_BOOKS = "All books:";
    public static final String AVAILABLE_BOOKS = "Available books:";
    public static final String INVALID = "Invalid choice. Please try again.";
    public static final String EXIT = "Thank you for using the Library Management System!";
    public static final String RETURNED = "Book returned successfully!";
    public static final String BORROWED = "Book borrowed successfully!";
    public static final String TITLE_AUTHOR_SEARCH = "Enter the title or author of the book to search: ";
    public static final String TITLE_BORROW = "Enter the title of the book to borrow: ";
    public static final String TITLE_RETURN = "Enter the title of the book to return: ";

    private static Library library;

    public static void setLibrary(Library library) {
        Main.library = library;
    }

    /**
     * Initialises the LMS program.
     * @param args from the command line.
     */
    public static void main(final String[] args) {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:library.db");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println(OPENED);
        if (library == null) {
            library = new Library(connection);
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println(WELCOME);

        while (running) {
            System.out.println(MENU);
            int choice = validateChoices(scanner, 1, 7);
            scanner.nextLine(); // Consume newline
            String title;
            switch (choice) {
                case ADD_A_BOOK_OPTION:
                    System.out.print(ENTER_TITLE);
                    title = scanner.nextLine();
                    System.out.print(ENTER_AUTHOR);
                    String author = scanner.nextLine();
                    boolean success = false;
                    try {
                        success = library.addBook(new Book(title, author));
                    } catch (InvalidParameterException e) {
                        System.out.println(e.getMessage());
                    }
                    System.out.println(success ? BOOK_ADDED : BOOK_NOT_ADDED);
                    break;
                case LIST_ALL_BOOKS:
                    System.out.println(SORT_MENU);
                    int sortAllChoice = validateChoices(scanner, 1, 2);
                    System.out.println(ALL_BOOKS);
                    switch (sortAllChoice) {
                        case AUTHOR:
                            library.viewAllBooks().stream()
                                .sorted(Comparator.comparing(Book::getAuthor))
                                .forEach(System.out::println);
                            break;
                        case TITLE:
                            library.viewAllBooks().stream()
                                .sorted(Comparator.comparing(Book::getTitle))
                                .forEach(System.out::println);
                            break;
                        default:
                            System.out.println(INVALID);
                    }
                    break;
                case LIST_AVAILABLE_BOOKS:
                    System.out.println(SORT_MENU);
                    int sortAvailableChoice = validateChoices(scanner, 1, 2);
                    System.out.println(AVAILABLE_BOOKS);
                    switch (sortAvailableChoice) {
                        case AUTHOR:
                            library.viewAvailableBooks().stream()
                                .sorted(Comparator.comparing(Book::getAuthor))
                                .forEach(System.out::println);
                            break;
                        case TITLE:
                            library.viewAvailableBooks().stream()
                                .sorted(Comparator.comparing(Book::getTitle))
                                .forEach(System.out::println);
                            break;
                        default:
                            System.out.println(INVALID);
                    }
                    break;
                case SEARCH_BOOK_OPTION:
                    System.out.print(TITLE_AUTHOR_SEARCH);
                    title = scanner.nextLine();
                    try {
                        Book book = library.searchBook(title);
                        System.out.println(book);
                    } catch (BookNotFoundException bookNotFoundException) {
                        System.out.println(bookNotFoundException.getMessage());
                    } catch (InvalidParameterException invalidParameterException) {
                        System.out.printf(invalidParameterException.getMessage());
                    }
                    break;
                case BORROW_BOOK_OPTION:
                    System.out.print(TITLE_BORROW);
                    title = scanner.nextLine();
                    try {
                        library.borrowBook(title);
                        System.out.println(BORROWED);
                    } catch (BookNotFoundException | AlreadyBorrowedException | InvalidParameterException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case RETURN_BOOK_OPTION:
                    System.out.print(TITLE_RETURN);
                    title = scanner.nextLine();
                    try {
                        library.returnBook(title);
                        System.out.println(RETURNED);
                    } catch (BookNotFoundException | NotBorrowedException | InvalidParameterException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case EXIT_OPTION:
                    running = false;
                    System.out.println(EXIT);
                    break;
                default:
                    System.out.println(INVALID);
            }
        }

        scanner.close();
    }

    private static int validateChoices(Scanner scanner, int min, int max) {
        int choice = 0;
        boolean validChoice = false;
        while (!validChoice) {
            try {
                choice = scanner.nextInt();
                if (choice < min || choice > max) {
                    throw new InputMismatchException();
                }
                validChoice = true;
            } catch (InputMismatchException inputMismatchException) {
                System.out.println(INVALID);
            }
        }
        return choice;
    }
}
