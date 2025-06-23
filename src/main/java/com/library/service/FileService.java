package com.library.service;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Genre;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class FileService {
    private static final String DATA_DIR = "data";
    
    public FileService() {
        ensureDataDirectoryExists();
    }
    
    private void ensureDataDirectoryExists() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }
    
    public void saveBooksToFile(List<Book> books, String filename) {
        ensureFileExists(filename, "title,author,isbn,year,genre");
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Write header
            writer.println("title,author,isbn,year,genre");
            
            // Write book data
            for (Book book : books) {
                writer.println(String.format("\"%s\",\"%s\",%s,%d,%s",
                        book.getTitle().replace("\"", "\"\""),
                        book.getAuthor().replace("\"", "\"\""),
                        book.getIsbn(),
                        book.getPublicationYear(),
                        book.getGenre()));
            }
        } catch (IOException e) {
            System.err.println("Error saving books to file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public List<Book> loadBooksFromFile(String filename) {
        List<Book> books = new ArrayList<>();
        
        if (!fileExists(filename)) {
            return books; // Return empty list if file doesn't exist
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // Skip header
            String line = reader.readLine();
            if (line == null || !line.startsWith("title,author")) {
                System.err.println("Invalid or empty books file: " + filename);
                return books;
            }
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                try {
                    // Simple CSV parsing (handles quoted fields with commas)
                    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (parts.length != 5) continue;
                    
                    String title = parts[0].replaceAll("^\"|\"$", "");
                    String author = parts[1].replaceAll("^\"|\"$", "");
                    String isbn = parts[2].replaceAll("^\"|\"$", "");
                    int year = Integer.parseInt(parts[3]);
                    Genre genre = Genre.valueOf(parts[4]);
                    
                    books.add(new Book(title, author, isbn, year, genre));
                } catch (Exception e) {
                    System.err.println("Error parsing line: " + line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading books file: " + e.getMessage());
            e.printStackTrace();
        }
        
        return books;
    }
    
    public void saveMembersToFile(List<Member> members, String filename) {
        ensureFileExists(filename, "memberId,name,borrowedBooks");
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Write header
            writer.println("memberId,name,borrowedBooks");
            
            // Write member data
            for (Member member : members) {
                writer.println(member.toCsvString());
            }
        } catch (IOException e) {
            System.err.println("Error saving members to file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public List<Member> loadMembersFromFile(String filename) {
        List<Member> members = new ArrayList<>();
        
        if (!fileExists(filename)) {
            return members; // Return empty list if file doesn't exist
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // Skip header
            String line = reader.readLine();
            if (line == null || !line.startsWith("memberId,name")) {
                System.err.println("Invalid or empty members file: " + filename);
                return members;
            }
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                try {
                    Member member = Member.fromCsv(line);
                    if (member != null) {
                        members.add(member);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing line: " + line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading members file: " + e.getMessage());
            e.printStackTrace();
        }
        
        return members;
    }
    
    private boolean fileExists(String filename) {
        File file = new File(filename);
        return file.exists() && file.length() > 0;
    }
    
    private void ensureFileExists(String filename, String header) {
        File file = new File(filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
                if (header != null && !header.isEmpty()) {
                    try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                        writer.println(header);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error creating file " + filename + ": " + e.getMessage());
            }
        }
    }
}
