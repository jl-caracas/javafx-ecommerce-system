package com.ecommerce.model;

public class Product {
    private int id;
    private String name;
    private String description;
    private String category;
    private double price;
    private int stock;
    private String imagePath;   // new field for image

    // Default constructor
    public Product() {}

    // Constructor without id (for creating new products)
    public Product(String name, String description, String category, 
                   double price, int stock, String imagePath) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.imagePath = imagePath;
    }

    // Full constructor with id (used when reading from database)
    public Product(int id, String name, String description, String category, 
                   double price, int stock, String imagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.imagePath = imagePath;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @Override
    public String toString() {
        return name + " - $" + price + " (Stock: " + stock + ")";
    }
}