package com.ecommerce.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private int id;
    private String customerName;
    private int userId;
    private LocalDateTime orderDate;
    private double total;
    private String status;   // "PENDING" or "COMPLETED"
    private List<OrderItem> items;

    // Full constructor
    public Order(int id, String customerName, int userId, LocalDateTime orderDate, double total, String status, List<OrderItem> items) {
        this.id = id;
        this.customerName = customerName;
        this.userId = userId;
        this.orderDate = orderDate;
        this.total = total;
        this.status = status;
        this.items = items;
    }

    // (for backward compatibility)
    public Order(int id, String customerName, LocalDateTime orderDate, double total, List<OrderItem> items) {
        this(id, customerName, 0, orderDate, total, "PENDING", items);
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}