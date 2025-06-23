package com.library.exception;

public class BookLimitExceededException extends Exception {
    public BookLimitExceededException(String message) {
        super(message);
    }
}
