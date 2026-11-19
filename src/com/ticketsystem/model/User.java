package com.ticketsystem.model;

/**
 * Common fields shared by every kind of user.
 * Abstract because a plain "User" never exists on its own.
 */
public abstract class User {
    private final int id;
    private final String name;
    private final String email;

    protected User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public abstract String getRole();

    @Override
    public String toString() {
        return getRole() + "#" + id + " " + name + " <" + email + ">";
    }
}
