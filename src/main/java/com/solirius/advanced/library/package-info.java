/**
 * Provides the classes and interfaces for managing
 * a library system.
 * <p>
 * This package includes functionalities for:
 * </p>
 * <ul>
 *   <li>Managing books (adding, removing, and updating
 *   book information).</li>
 *   <li>Handling user operations such as registration,
 *   borrowing, and
 *   returning books.</li>
 * </ul>
 * <p>
 * The core components of this package are:
 * </p>
 * <ul>
 *   <li>{@code Book}: Represents a book entity with details
 *   like title,
 *   author, ISBN, and availability status.</li>
 *   <li>{@code Library}: Handles the main operations of the
 *   library, acting
 *   as the controller of the system.</li>
 * </ul>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 *         Library library = new Library();
 *         Book book = new Book("The Great Gatsby",
 *         "F. Scott Fitzgerald");
 *         library.addBook(book);
 *         book.borrowBook();
 * </pre>
 *
 * @since 1.0
 * @author Antonio Cucchiara (Solirius Consulting Ltd.)
 */
package com.solirius.advanced.library;
