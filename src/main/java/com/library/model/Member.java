package com.library.model;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.time.temporal.ChronoUnit;

public class Member {
    private String memberId;
    private String name;
    private Map<String, LocalDate> borrowedBooks = new HashMap<>(); // ISBN -> Due Date
    private static final int MAX_BOOKS = 5;
    private static final int BORROW_DAYS = 14;

    // Constructor
    public Member() {
    }

    public Member(String memberId, String name) {
        this.memberId = memberId;
        this.name = name;
    }

    // Getters and Setters
    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, LocalDate> getBorrowedBooks() {
        return Collections.unmodifiableMap(borrowedBooks);
    }

    public boolean canBorrow() {
        return borrowedBooks.size() < MAX_BOOKS;
    }

    public boolean hasOverdueBooks() {
        return borrowedBooks.values().stream()
                .anyMatch(dueDate -> LocalDate.now().isAfter(dueDate));
    }

    public List<String> getOverdueBooks() {
        List<String> overdueBooks = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        borrowedBooks.forEach((isbn, dueDate) -> {
            if (today.isAfter(dueDate)) {
                long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);
                overdueBooks.add(String.format("ISBN: %s, Days overdue: %d", isbn, daysOverdue));
            }
        });
        
        return overdueBooks;
    }

    public boolean borrowBook(String isbn) {
        if (isbn == null || isbn.trim().isEmpty() || !canBorrow()) {
            return false;
        }
        borrowedBooks.put(isbn, LocalDate.now().plusDays(BORROW_DAYS));
        return true;
    }

    public boolean returnBook(String isbn) {
        if (isbn == null) {
            return false;
        }
        return borrowedBooks.remove(isbn) != null;
    }

    public String toCsvString() {
        if (memberId == null || name == null) {
            throw new IllegalStateException("Member ID and name must be set");
        }
        
        StringBuilder sb = new StringBuilder();
        // Escape member ID (replace quotes and escape commas)
        String safeId = memberId.replace("\"", "\"\"").replace(",", "\\,");
        // Escape name (handle quotes and commas)
        String safeName = name.replace("\"", "\"\"").replace("\n", " ").replace("\r", "");
        
        sb.append('"').append(safeId).append("\",");
        sb.append('"').append(safeName).append('"').append(",");
        
        // Add borrowed books as ISBN:DueDate pairs
        List<String> books = new ArrayList<>();
        for (Map.Entry<String, LocalDate> entry : borrowedBooks.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                books.add(entry.getKey().replace(":", "\\:") + ":" + entry.getValue());
            }
        }
        sb.append('"').append(String.join(";", books)).append('"');
        
        return sb.toString();
    }

    public static Member fromCsv(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV line cannot be null or empty");
        }
        
        try {
            // Parse CSV line with quoted values
            List<String> parts = new ArrayList<>();
            boolean inQuotes = false;
            StringBuilder current = new StringBuilder();
            
            for (int i = 0; i < csvLine.length(); i++) {
                char c = csvLine.charAt(i);
                if (c == '"') {
                    // Handle escaped quotes
                    if (i < csvLine.length() - 1 && csvLine.charAt(i + 1) == '"') {
                        current.append('"');
                        i++; // Skip next quote
                    } else {
                        inQuotes = !inQuotes;
                    }
                } else if (c == ',' && !inQuotes) {
                    parts.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
            parts.add(current.toString());
            
            if (parts.size() < 2) {
                throw new IllegalArgumentException("Invalid CSV format: " + csvLine);
            }
            
            String id = parts.get(0).trim();
            String name = parts.get(1).trim();
            
            if (id.isEmpty() || name.isEmpty()) {
                throw new IllegalArgumentException("Member ID and name cannot be empty");
            }
            
            Member member = new Member(id, name);
            
            // Parse borrowed books if they exist
            if (parts.size() > 2 && !parts.get(2).trim().isEmpty()) {
                String booksStr = parts.get(2).trim();
                if (!booksStr.isEmpty()) {
                    String[] books = booksStr.split(";");
                    for (String book : books) {
                        if (!book.trim().isEmpty()) {
                            String[] bookParts = book.split(":", 2);
                            if (bookParts.length == 2 && !bookParts[0].trim().isEmpty()) {
                                try {
                                    member.borrowedBooks.put(bookParts[0].trim(), 
                                        LocalDate.parse(bookParts[1].trim()));
                                } catch (Exception e) {
                                    System.err.println("Skipping invalid book entry: " + book);
                                }
                            }
                        }
                    }
                }
            }
            
            return member;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing member from CSV: " + e.getMessage(), e);
        }
    }

    public void saveToFile(String filename) throws IOException {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        
        File file = new File(filename);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory: " + parent.getAbsolutePath());
        }
        
        // Write to temporary file first, then rename to ensure atomic operation
        File tempFile = new File(filename + ".tmp");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile), "UTF-8"))) {
            writer.write(toCsvString());
            writer.newLine();
        } catch (IOException e) {
            if (tempFile.exists() && !tempFile.delete()) {
                tempFile.deleteOnExit();
            }
            throw new IOException("Failed to save member to file: " + e.getMessage(), e);
        }
        
        // Rename temp file to target file
        if (file.exists() && !file.delete()) {
            throw new IOException("Failed to replace existing file: " + file.getAbsolutePath());
        }
        if (!tempFile.renameTo(file)) {
            throw new IOException("Failed to rename temporary file to: " + file.getAbsolutePath());
        }
    }

    public static Member loadFromFile(String filename) throws IOException {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotFoundException("Member file not found: " + filename);
        }
        if (!file.canRead()) {
            throw new IOException("Cannot read member file: " + filename);
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String csvLine = reader.readLine();
            if (csvLine == null || csvLine.trim().isEmpty()) {
                throw new IOException("File is empty: " + filename);
            }
            return fromCsv(csvLine.trim());
        } catch (Exception e) {
            throw new IOException("Error loading member from file: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return String.format("Member{id=%s, name='%s', borrowedBooks=%d, canBorrow=%b, hasOverdue=%b}",
                memberId, name, borrowedBooks.size(), canBorrow(), hasOverdueBooks());
    }
}
