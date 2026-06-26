package com.ecommerce.model;

public class CartItem {
    private ProductVariant variant;
    private int quantity;
    private double finalPrice;   // store the price at the time of adding (base + modifier)

    public CartItem(ProductVariant variant, int quantity, double finalPrice) {
        this.variant = variant;
        this.quantity = quantity;
        this.finalPrice = finalPrice;
    }

    public ProductVariant getVariant() { return variant; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getFinalPrice() { return finalPrice; }

    public double getTotalPrice() {
        return finalPrice * quantity;
    }

    // For backward compatibility (if needed)
    public Product getProduct() { return null; }
}