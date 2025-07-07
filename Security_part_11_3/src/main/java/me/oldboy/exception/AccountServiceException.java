package me.oldboy.exception;

public class AccountServiceException extends RuntimeException {
    public AccountServiceException(String msg) {
        super(msg);
    }
}
