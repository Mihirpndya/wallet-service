package com.rs.payments.wallet.exception;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String message) {
        super(message);
    }
}

