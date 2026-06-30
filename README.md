This repository contains a Java challenge designed to assess the skills of a Java Guild candidate. The challenge includes problem-solving, coding, and demonstrating knowledge of Java fundamentals and best practices.

# Build a Library Management System
## Objective:
Create a simplified Library Management System (LMS) using Java that allows users to perform basic operations like adding books, borrowing books, and viewing available books.

# Requirements
## Book Class (visibility at candidate discretion)
Represents a book in the library.

### Attributes (visibility at candidate discretion)
 * String title
 * String author
 * boolean isBorrowed

### Methods (visibility at candidate discretion)
 * Book(String title, String author) - Constructor to initialise a book.
 * boolean borrowBook() - Marks the book as borrowed if it's not already borrowed.
 * boolean returnBook() - Marks the book as returned.
 * String toString() - Returns a string representation of the book (e.g., "Title by Author (Available/Borrowed)").

## Library Class (visibility at candidate discretion)
Manages a collection of books.

### Attributes (visibility at candidate discretion)
 * ArrayList<Book> books

### Methods (visibility at candidate discretion)
 * void addBook(Book book) - Adds a new book to the collection.
 * List<Book> viewAvailableBooks() - Returns a list of books that are not borrowed.
 * Book searchBook(String title) - Searches for a book by title.
 * boolean borrowBook(String title) - Allows a user to borrow a book by title.
 * boolean returnBook(String title) - Allows a user to return a book by title.

## Main Class (visibility at candidate discretion)
Provides a menu-driven interface for the user to interact with the Library Management System.

Options include:
 * Add a new book.
 * View available books.
 * Search for a book.
 * Borrow a book.
 * Return a book.
 * Exit the application.

## Incremental role-based extra features (eg. an intermediate is expected to build Graduate+Intermediate)
 * Graduate
   * Unit Tests: Write test cases for Book and Library classes. 
 * Intermediate
   * Exception Handling: Handle scenarios where a book is already borrowed, does not exist, or cannot be returned.
   * Persistence: Save the library state to a file and reload it upon application restart.
 * Advanced roles
   * Persistence: Save the library state to a portable file-based database.
   * Search: Find a book by title or author
   * Sorting: Provide an option to view books sorted by title or author.

## Instructions for Submission
Create a zip file (.7z or .zip) containing the code and any other files for your project.

Include:
 * The complete Java source code.
 * A README.md file with instructions on how to run the program.
 * A brief description of the approach taken and any additional features implemented.
 * Email the zip file to your talent acquisition contact as an attachment when completed.

## Evaluation Criteria
 * Code readability and organisation.
 * Proper use of object-oriented programming principles.
 * Handling edge cases and exceptions.
 * Implementation of bonus features (if any).
 * Clarity and thoroughness of the README file.

# Notes
1. Added JaCoCo
2. Added missing edge case in `LibraryTest` where Library is initialised with 1 book already borrowed.
3. Added missing edge case in `LibraryTest` where Library is initialised with 1 book, but search for non existent author throws exception.
4. Added borrow book functionality.
5. Added return book functionality.
6. Fixed menu mismatch causing problems with `View All Books` and `View Available Books`.
7. Added null checks for search, borrow and return book.