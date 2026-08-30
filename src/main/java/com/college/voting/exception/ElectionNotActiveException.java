package com.college.voting.exception;

public class ElectionNotActiveException extends RuntimeException {
    public ElectionNotActiveException(String message) {
        super(message);
    }
}
