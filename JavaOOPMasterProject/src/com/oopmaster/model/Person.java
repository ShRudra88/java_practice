package com.oopmaster.model;

/** Abstract parent class: demonstrates abstraction and inheritance. */
public abstract class Person {
    private final int id;                 // encapsulation + final field
    private String name;
    private String email;

    protected Person(int id, String name, String email) {
        if (id <= 0) throw new IllegalArgumentException("ID must be positive.");
        this.id = id;
        setName(name);
        setEmail(email);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be empty.");
        this.name = name.trim();
    }
    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || !email.contains("@")) throw new IllegalArgumentException("Enter a valid email.");
        this.email = email.trim();
    }

    public abstract String getRole(); // abstract method

    public void introduce() { // inherited concrete method
        System.out.println(getRole() + " | " + id + " | " + name + " | " + email);
    }
}
