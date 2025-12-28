package com.kjl.servicejava.exception;

public class InvalidOrderTypeException extends OrderValidationException {
    public InvalidOrderTypeException(String message) {
        super(message);
    }
}
