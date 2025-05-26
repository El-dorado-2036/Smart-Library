// === PACKAGE STRUCTURE ===
// Assuming this is inside package com.smartlibrary.
package com.smartlibrary;

import java.io.*;
import java.util.*;

// === ABSTRACT BASE ENTITY ===
abstract class BaseEntity {
    protected String id;
    protected String name;

    public BaseEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public abstract void display();
}

// === INTERFACE FOR SERVICES ===
interface LibraryService {
    void borrowBook(String userId, String bookId) throws Exception;
    void returnBook(String userId, String bookId) throws Exception;
    void displayAvailableBooks();
}

// === BOOK CLASS ===
class Book extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean isAvailable = true;

    public Book(String id, String name) {
        super(id, name);
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public void display() {
        System.out.println("Book ID: " + id + ", Title: " + name + ", Available: " + isAvailable);
    }
}

// === USER CLASS ===
class User extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<String> borrowedBooks = new ArrayList<>();

    public User(String id, String name) {
        super(id, name);
    }

    public List<String> getBorrowedBooks() {
        return borrowedBooks;
    }

    public void borrow(String bookId) {
        borrowedBooks.add(bookId);
    }

    public void returnBook(String bookId) {
        borrowedBooks.remove(bookId);
    }

    @Override
    public void display() {
        System.out.println("User ID: " + id + ", Name: " + name + ", Borrowed: " + borrowedBooks);
    }
}

// === MAIN SYSTEM IMPLEMENTATION ===
class SmartLibrary implements LibraryService {
    private Map<String, Book> bookMap = new HashMap<>();
    private Map<String, User> userMap = new HashMap<>();

    public SmartLibrary() {
        loadBooks();
        loadUsers();
    }

    @Override
    public void borrowBook(String userId, String bookId) throws Exception {
        Book book = bookMap.get(bookId);
        User user = userMap.get(userId);
        if (book == null || user == null) throw new Exception("Invalid user or book ID.");
        if (!book.isAvailable()) throw new Exception("Book not available.");
        book.setAvailable(false);
        user.borrow(bookId);
        System.out.println("Book borrowed successfully!");
        saveAll();
    }

    @Override
    public void returnBook(String userId, String bookId) throws Exception {
        Book book = bookMap.get(bookId);
        User user = userMap.get(userId);
        if (book == null || user == null) throw new Exception("Invalid user or book ID.");
        if (!user.getBorrowedBooks().contains(bookId)) throw new Exception("User has not borrowed this book.");
        book.setAvailable(true);
        user.returnBook(bookId);
        System.out.println("Book returned successfully!");
        saveAll();
    }

    @Override
    public void displayAvailableBooks() {
        bookMap.values().stream().filter(Book::isAvailable).forEach(Book::display);
    }

    public void addUser(User user) {
        userMap.put(user.id, user);
    }

    public void addBook(Book book) {
        bookMap.put(book.id, book);
    }

    private void loadBooks() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("books.dat"))) {
            bookMap = (Map<String, Book>) ois.readObject();
        } catch (Exception ignored) {}
    }

    private void loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("users.dat"))) {
            userMap = (Map<String, User>) ois.readObject();
        } catch (Exception ignored) {}
    }

    private void saveAll() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("books.dat"))) {
            oos.writeObject(bookMap);
        } catch (IOException ignored) {}
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"))) {
            oos.writeObject(userMap);
        } catch (IOException ignored) {}
    }
}

// === LOGGER THREAD ===
class LoggerThread extends Thread {
    public void run() {
        while (true) {
            try {
                System.out.println("[LOG] System running at: " + new Date());
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println("Logger interrupted.");
            }
        }
    }
}

// === MAIN CLASS ===
public class Main {
    public static void main(String[] args) throws Exception {
        SmartLibrary library = new SmartLibrary();
        LoggerThread logger = new LoggerThread();
        logger.setDaemon(true);
        logger.start();

        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to SmartLibrary!");

        while (true) {
            System.out.println("\n1. Borrow Book\n2. Return Book\n3. Display Available Books\n4. Exit\nEnter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();
            switch (choice) {
                case 1:
                    System.out.print("Enter User ID: ");
                    String uid1 = sc.nextLine();
                    System.out.print("Enter Book ID: ");
                    String bid1 = sc.nextLine();
                    library.borrowBook(uid1, bid1);
                    break;
                case 2:
                    System.out.print("Enter User ID: ");
                    String uid2 = sc.nextLine();
                    System.out.print("Enter Book ID: ");
                    String bid2 = sc.nextLine();
                    library.returnBook(uid2, bid2);
                    break;
                case 3:
                    library.displayAvailableBooks();
                    break;
                case 4:
                    System.exit(0);
            }
        }
    }
}