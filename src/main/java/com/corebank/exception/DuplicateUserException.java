package com.corebank.exception;

public class DuplicateUserException extends Exception {


    public DuplicateUserException() {
        super();
    }


    public DuplicateUserException(String message) {
        super(message);
    }


    public DuplicateUserException(String message, Throwable cause) {
        super(message, cause);
    }


    public DuplicateUserException(Throwable cause) {
        super(cause);
    }
}
