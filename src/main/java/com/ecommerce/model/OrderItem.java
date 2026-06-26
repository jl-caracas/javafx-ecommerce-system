package com.ecommerce.model;

public class OrderItem {
    private int id;
    private int orderId;
    private String productName;
    private double productPrice;
    private int quantity;

    public OrderItem(int id, int orderId, String productName, double productPrice, int quantity) {
        this.id = id;
        this.orderId = orderId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}