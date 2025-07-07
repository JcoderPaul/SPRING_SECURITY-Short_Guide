package me.oldboy.exception;

public class LoanServiceException extends RuntimeException {
    public LoanServiceException(String msg) {
        super(msg);
    }
}
