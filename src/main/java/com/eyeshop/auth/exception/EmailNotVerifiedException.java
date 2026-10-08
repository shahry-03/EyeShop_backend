package com.eyeshop.auth.exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String message) {
        super(message);
    }

    public EmailNotVerifiedException() {
        super("Please verify your email address before logging in");
    }
}