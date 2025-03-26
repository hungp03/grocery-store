package com.store.grocery.util.exception;

public class CannotDeleteException extends RuntimeException {
    public CannotDeleteException(String message) {
        super(message);
    }
}
