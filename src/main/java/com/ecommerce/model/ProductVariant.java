package com.ecommerce.model;

public class ProductVariant {
    private int id;
    private int productId;
    private String specName;
    private String color;
    private double priceModifier;
    private int stock;

    public ProductVariant() {}

    public ProductVariant(int id, int productId, String specName, String color, double priceModifier, int stock) {
        this.id = id;
        this.productId = productId;
        this.specName = specName;
        this.color = color;
        this.priceModifier = priceModifier;
        this.stock = stock;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getSpecName() { return specName; }
    public void setSpecName(String specName) { this.specName = specName; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public double getPriceModifier() { return priceModifier; }
    public void setPriceModifier(double priceModifier) { this.priceModifier = priceModifier; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    // Helper: get final price (requires base product price)
    public double getFinalPrice(double basePrice) {
        return basePrice + priceModifier;
    }

    // For display in cart and receipt
    public String getDisplayName(String baseProductName) {
        StringBuilder sb = new StringBuilder(baseProductName);
        if (specName != null && !specName.isEmpty()) sb.append(" (").append(specName);
        if (color != null && !color.isEmpty()) sb.append(", ").append(color);
        if (specName != null && !specName.isEmpty()) sb.append(")");
        return sb.toString();
    }
}