package com.ticketsystem.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String role, int userId) {
        super(role + " #" + userId + " not found");
    }
}
