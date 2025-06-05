package me.oldboy.exception;

public class ClientServiceException extends RuntimeException {
    public ClientServiceException(String msg) {
        super(msg);
    }
}
