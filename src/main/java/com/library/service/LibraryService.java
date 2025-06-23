package com.library.service;

import com.library.exception.*;
import com.library.model.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class LibraryService {
    private Map<String, Book> books = new HashMap<>();
    private Map<String, Member> members = new HashMap<>();

    // Book Management Methods
    public void addBook(Book book) throws DuplicateBookException {
        if (books.containsKey(book.getIsbn())) throw new DuplicateBookException("ISBN already exists.");
        books.put(book.getIsbn(), book);
    }

    public void removeBook(String isbn) throws BookNotFoundException {
        if (!books.containsKey(isbn)) throw new BookNotFoundException("Book not found.");
        books.remove(isbn);
    }

    public List<Book> searchByAuthor(String author) {
        return books.values().stream()
                .filter(book -> book.getAuthor().equalsIgnoreCase(author))
                .collect(Collectors.toList());
    }

    public List<Book> searchByGenre(Genre genre) {
        return books.values().stream()
                .filter(book -> book.getGenre() == genre)
                .collect(Collectors.toList());
    }

    public Book getOldestBook() {
        return books.values().stream()
                .min(Comparator.comparingInt(Book::getPublicationYear))
                .orElse(null);
    }

    public Book getNewestBook() {
        return books.values().stream()
                .max(Comparator.comparingInt(Book::getPublicationYear))
                .orElse(null);
    }

    public Map<Genre, Long> countBooksByGenre() {
        return books.values().stream()
                .collect(Collectors.groupingBy(Book::getGenre, Collectors.counting()));
    }

    public List<Book> getAllBooksSortedByYear() {
        return books.values().stream()
                .sorted(Comparator.comparingInt(Book::getPublicationYear))
                .collect(Collectors.toList());
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books.values());
    }

    
    // Member Management Methods
    public void addMember(Member member) throws DuplicateMemberException {
        if (members.containsKey(member.getMemberId())) {
            throw new DuplicateMemberException("Member ID already exists.");
        }
        members.put(member.getMemberId(), member);
    }

    public Member getMember(String memberId) throws MemberNotFoundException {
        Member member = members.get(memberId);
        if (member == null) {
            throw new MemberNotFoundException("Member not found with ID: " + memberId);
        }
        return member;
    }

    public void borrowBook(String memberId, String isbn) throws MemberNotFoundException, BookNotFoundException, 
            BookLimitExceededException, BookNotAvailableException {
        Member member = getMember(memberId);
        Book book = books.get(isbn);
        
        if (book == null) {
            throw new BookNotFoundException("Book not found with ISBN: " + isbn);
        }
        
        if (!member.canBorrow()) {
            throw new BookLimitExceededException("Member has reached the maximum number of borrowed books.");
        }
        
        // Check if book is already borrowed by someone
        boolean isBookBorrowed = members.values().stream()
                .anyMatch(m -> m.getBorrowedBooks().containsKey(isbn));
                
        if (isBookBorrowed) {
            throw new BookNotAvailableException("Book is already borrowed by another member.");
        }
        
        if (!member.borrowBook(isbn)) {
            throw new IllegalStateException("Failed to borrow book. Please try again.");
        }
    }

    public void returnBook(String memberId, String isbn) throws MemberNotFoundException, BookNotBorrowedException {
        Member member = getMember(memberId);
        
        if (!member.getBorrowedBooks().containsKey(isbn)) {
            throw new BookNotBorrowedException("This book is not borrowed by the member.");
        }
        
        if (!member.returnBook(isbn)) {
            throw new IllegalStateException("Failed to return book. Please try again.");
        }
    }
    
    public List<Member> getAllMembers() {
        return new ArrayList<>(members.values());
    }
    
    public List<Book> getAvailableBooks() {
        Set<String> borrowedIsbns = members.values().stream()
                .flatMap(m -> m.getBorrowedBooks().keySet().stream())
                .collect(Collectors.toSet());
                
        return books.values().stream()
                .filter(book -> !borrowedIsbns.contains(book.getIsbn()))
                .collect(Collectors.toList());
    }}
