package com.library;

import com.library.exception.*;
import com.library.model.*;
import com.library.service.*;

import java.time.LocalDate;
import java.util.*;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final LibraryService library = new LibraryService();
    private static final FileService fileService = new FileService();
    private static final String BOOKS_FILE = "data/books.csv";
    private static final String MEMBERS_FILE = "data/members.csv";

    public static void main(String[] args) {
        // Load existing data
        loadData();
        
        while (true) {
            System.out.println("\n--- Library Management System ---");
            System.out.println("1. Book Management");
            System.out.println("2. Member Management");
            System.out.println("3. Borrow/Return Books");
            System.out.println("4. View Reports");
            System.out.println("5. Save and Exit");
            System.out.print("Enter choice: ");
            
            try {
                int choice = Integer.parseInt(sc.nextLine());
                
                switch (choice) {
                    case 1 -> bookManagement();
                    case 2 -> memberManagement();
                    case 3 -> borrowReturnManagement();
                    case 4 -> viewReports();
                    case 5 -> {
                        saveData();
                        System.out.println("Data saved. Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void bookManagement() throws Exception {
        while (true) {
            System.out.println("\n--- Book Management ---");
            System.out.println("1. Add Book");
            System.out.println("2. Remove Book");
            System.out.println("3. Search by Author");
            System.out.println("4. Search by Genre");
            System.out.println("5. View All Books");
            System.out.println("6. Back to Main Menu");
            System.out.print("Enter choice: ");
            
            int choice = Integer.parseInt(sc.nextLine());
            
            switch (choice) {
                case 1 -> addBook();
                case 2 -> removeBook();
                case 3 -> searchByAuthor();
                case 4 -> searchByGenre();
                case 5 -> printBooks(library.getAllBooks());
                case 6 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void memberManagement() {
        while (true) {
            System.out.println("\n--- Member Management ---");
            System.out.println("1. Add Member");
            System.out.println("2. View All Members");
            System.out.println("3. Back to Main Menu");
            System.out.print("Enter choice: ");
            
            int choice = Integer.parseInt(sc.nextLine());
            
            try {
                switch (choice) {
                    case 1 -> addMember();
                    case 2 -> viewAllMembers();
                    case 3 -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void borrowReturnManagement() {
        while (true) {
            System.out.println("\n--- Borrow/Return Management ---");
            System.out.println("1. Borrow a Book");
            System.out.println("2. Return a Book");
            System.out.println("3. View Available Books");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter choice: ");
            
            int choice = Integer.parseInt(sc.nextLine());
            
            try {
                switch (choice) {
                    case 1 -> borrowBook();
                    case 2 -> returnBook();
                    case 3 -> printBooks(library.getAvailableBooks());
                    case 4 -> { return; }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void viewReports() {
        System.out.println("\n--- Reports ---");
        System.out.println("1. Books by Genre");
        System.out.println("2. Oldest/Newest Books");
        System.out.println("3. Overdue Books");
        System.out.println("4. Back to Main Menu");
        System.out.print("Enter choice: ");
        
        int choice = Integer.parseInt(sc.nextLine());
        
        try {
            switch (choice) {
                case 1 -> {
                    System.out.println("\nBooks by Genre:");
                    library.countBooksByGenre().forEach((g, c) -> 
                        System.out.println(g + ": " + c + " books"));
                }
                case 2 -> {
                    Book oldest = library.getOldestBook();
                    Book newest = library.getNewestBook();
                    System.out.println("\nOldest Book: " + 
                        (oldest != null ? oldest : "No books in library"));
                    System.out.println("Newest Book: " + 
                        (newest != null ? newest : "No books in library"));
                }
                case 3 -> {
                    System.out.println("\nOverdue Books:");
                    viewOverdueBooks();
                }
                case 4 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Helper methods for member operations
    private static void addMember() throws Exception {
        System.out.print("Enter Member ID: ");
        String memberId = sc.nextLine();
        System.out.print("Enter Member Name: ");
        String name = sc.nextLine();
        
        Member member = new Member();
        member.setMemberId(memberId);
        member.setName(name);
        
        library.addMember(member);
        System.out.println("Member added successfully!");
    }

    private static void viewAllMembers() {
        List<Member> members = library.getAllMembers();
        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }
        
        System.out.println("\n--- All Members ---");
        for (Member member : members) {
            System.out.println("ID: " + member.getMemberId() + 
                           ", Name: " + member.getName() +
                           ", Books Borrowed: " + member.getBorrowedBooks().size());
        }
    }

    private static void borrowBook() throws Exception {
        System.out.print("Enter Member ID: ");
        String memberId = sc.nextLine();
        
        System.out.println("\nAvailable Books:");
        List<Book> availableBooks = library.getAvailableBooks();
        if (availableBooks.isEmpty()) {
            System.out.println("No books available for borrowing.");
            return;
        }
        
        printBooks(availableBooks);
        
        System.out.print("Enter ISBN of the book to borrow: ");
        String isbn = sc.nextLine();
        
        try {
            library.borrowBook(memberId, isbn);
            System.out.println("Book borrowed successfully!");
            Member member = library.getMember(memberId);
            LocalDate dueDate = member.getBorrowedBooks().get(isbn);
            System.out.println("Due date: " + dueDate);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void returnBook() throws Exception {
        System.out.print("Enter Member ID: ");
        String memberId = sc.nextLine();
        
        Member member = library.getMember(memberId);
        if (member.getBorrowedBooks().isEmpty()) {
            System.out.println("This member has no borrowed books.");
            return;
        }
        
        System.out.println("\nBorrowed Books:");
        member.getBorrowedBooks().forEach((isbn, dueDate) -> 
            System.out.println("ISBN: " + isbn + ", Due: " + dueDate + 
                (LocalDate.now().isAfter(dueDate) ? " (OVERDUE)" : ""))
        );
        
        System.out.print("Enter ISBN of the book to return: ");
        String isbn = sc.nextLine();
        
        try {
            library.returnBook(memberId, isbn);
            System.out.println("Book returned successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewOverdueBooks() {
        boolean found = false;
        for (Member member : library.getAllMembers()) {
            List<String> overdue = member.getOverdueBooks();
            if (!overdue.isEmpty()) {
                found = true;
                System.out.println("\nMember: " + member.getName() + " (ID: " + member.getMemberId() + ")");
                overdue.forEach(System.out::println);
            }
        }
        if (!found) {
            System.out.println("No overdue books found.");
        }
    }

    // Existing helper methods...
    private static void addBook() throws Exception {
        System.out.print("Title: "); String title = sc.nextLine();
        System.out.print("Author: "); String author = sc.nextLine();
        System.out.print("ISBN: "); String isbn = sc.nextLine();
        System.out.print("Year: "); int year = Integer.parseInt(sc.nextLine());
        System.out.print("Genre (FICTION, SCIENCE, HISTORY, MYSTERY, BIOGRAPHY): ");
        Genre genre = Genre.valueOf(sc.nextLine().toUpperCase());
        library.addBook(new Book(title, author, isbn, year, genre));
        System.out.println("Book added successfully!");
    }

    private static void removeBook() throws Exception {
        System.out.print("ISBN to remove: ");
        String isbn = sc.nextLine();
        library.removeBook(isbn);
        System.out.println("Book removed successfully!");
    }

    private static void searchByAuthor() {
        System.out.print("Author: ");
        String author = sc.nextLine();
        List<Book> books = library.searchByAuthor(author);
        if (books.isEmpty()) {
            System.out.println("No books found by this author.");
        } else {
            printBooks(books);
        }
    }

    private static void searchByGenre() {
        System.out.print("Genre (FICTION, SCIENCE, HISTORY, MYSTERY, BIOGRAPHY): ");
        try {
            Genre genre = Genre.valueOf(sc.nextLine().toUpperCase());
            List<Book> books = library.searchByGenre(genre);
            if (books.isEmpty()) {
                System.out.println("No books found in this genre.");
            } else {
                printBooks(books);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid genre.");
        }
    }

    private static void printBooks(Collection<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        System.out.println("\n--- Books ---");
        books.forEach(book -> System.out.println(book.getTitle() + " by " + 
            book.getAuthor() + " (ISBN: " + book.getIsbn() + ", " + book.getGenre() + ")"));
    }

    // Data persistence methods
    private static void loadData() {
        int booksLoaded = 0;
        int membersLoaded = 0;
        
        try {
            // Load books
            System.out.println("Loading books from " + BOOKS_FILE + "...");
            List<Book> books = fileService.loadBooksFromFile(BOOKS_FILE);
            for (Book book : books) {
                try {
                    library.addBook(book);
                    booksLoaded++;
                } catch (DuplicateBookException e) {
                    System.err.println("Skipping duplicate book: " + book.getIsbn());
                } catch (Exception e) {
                    System.err.println("Error loading book " + book.getIsbn() + ": " + e.getMessage());
                }
            }
            
            // Load members
            System.out.println("Loading members from " + MEMBERS_FILE + "...");
            List<Member> members = fileService.loadMembersFromFile(MEMBERS_FILE);
            for (Member member : members) {
                try {
                    library.addMember(member);
                    membersLoaded++;
                } catch (DuplicateMemberException e) {
                    System.err.println("Skipping duplicate member: " + member.getMemberId());
                } catch (Exception e) {
                    System.err.println("Error loading member " + member.getMemberId() + ": " + e.getMessage());
                }
            }
            
            System.out.println("Data loaded successfully: " + 
                             booksLoaded + " books, " + 
                             membersLoaded + " members");
        } catch (Exception e) {
            System.out.println("Error loading data: " + e.getMessage());
            System.out.println("Starting with empty library.");
        }
    }

    private static void saveData() {
        try {
            System.out.println("Saving books to " + BOOKS_FILE + "...");
            List<Book> books = library.getAllBooks();
            fileService.saveBooksToFile(books, BOOKS_FILE);
            
            System.out.println("Saving members to " + MEMBERS_FILE + "...");
            List<Member> members = library.getAllMembers();
            fileService.saveMembersToFile(members, MEMBERS_FILE);
            
            System.out.println("Data saved successfully - " + 
                             books.size() + " books, " + 
                             members.size() + " members");
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}