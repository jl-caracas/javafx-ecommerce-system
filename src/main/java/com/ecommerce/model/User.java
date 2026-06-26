package com.ecommerce.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String address;
    private String gcashNumber;
    private String role;   // "CUSTOMER" or "ADMIN"

    // Full constructor with role
    public User(int id, String username, String password, String fullName, 
                String address, String gcashNumber, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.address = address;
        this.gcashNumber = gcashNumber;
        this.role = role;
    }

    // Convenience constructor for new registrations (default role CUSTOMER)
    public User(int id, String username, String password, String fullName, String address, String gcashNumber) {
        this(id, username, password, fullName, address, gcashNumber, "CUSTOMER");
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getGcashNumber() { return gcashNumber; }
    public void setGcashNumber(String gcashNumber) { this.gcashNumber = gcashNumber; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}