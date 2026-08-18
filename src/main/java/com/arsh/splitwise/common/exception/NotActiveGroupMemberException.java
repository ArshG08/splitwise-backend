package com.arsh.splitwise.common.exception;

public class NotActiveGroupMemberException extends RuntimeException {
    public NotActiveGroupMemberException(String message) {
        super(message);
    }
}