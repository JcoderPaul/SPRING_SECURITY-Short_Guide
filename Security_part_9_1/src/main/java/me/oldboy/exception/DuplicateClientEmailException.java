package me.oldboy.exception;

public class DuplicateClientEmailException extends RuntimeException {
    public DuplicateClientEmailException(String msg) {
        super(msg);
    }
}
