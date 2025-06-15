package me.oldboy.exception;

public class EmptyCurrentClientException extends RuntimeException {
    public EmptyCurrentClientException(String msg) {
        super(msg);
    }
}
