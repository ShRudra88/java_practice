package com.oopmaster.model;

public class Department {
    private final String name;
    public Department(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Department name is required.");
        this.name = name.trim();
    }
    public String getName() { return name; }
    @Override public String toString() { return name; }
}
