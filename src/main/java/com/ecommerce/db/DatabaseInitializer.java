package com.ecommerce.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void insertSampleProducts() {
        var conn = DatabaseManager.getInstance().getConnection();
        String checkSql = "SELECT COUNT(*) FROM products";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
            	String insertSql = """
            		    INSERT INTO products (name, description, category, price, stock, image_path) VALUES
            		    ('Laptop', 'High performance laptop', 'Electronics', 899.99, 10, '/images/laptop.jpg'),
            		    ('Wireless Mouse', 'Ergonomic wireless mouse', 'Electronics', 25.50, 50, '/images/mouse.jpg'),
            		    ('Mechanical Keyboard', 'RGB mechanical keyboard', 'Electronics', 79.99, 30, '/images/keyboard.jpg'),
            		    ('Office Chair', 'Ergonomic mesh chair', 'Furniture', 199.99, 5, '/images/chair.jpg'),
            		    ('Coffee Mug', 'Ceramic 15oz mug', 'Kitchen', 9.99, 100, '/images/mug.jpg'),
            		    ('Smartphone', '5G unlocked smartphone', 'Electronics', 699.99, 15, '/images/smartphone.jpg'),
            		    ('Bluetooth Headphones', 'Noise cancelling headphones', 'Electronics', 89.99, 25, '/images/headphones.jpg'),
            		    ('Desk Lamp', 'LED desk lamp with dimmer', 'Furniture', 34.99, 20, '/images/lamp.jpg'),
            		    ('Notebook', 'A5 dotted notebook', 'Stationery', 5.99, 200, '/images/notebook.jpg'),
            		    ('Water Bottle', 'Stainless steel 20oz', 'Kitchen', 15.99, 75, '/images/bottle.jpg');
            		""";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    int rows = insertStmt.executeUpdate();
                    System.out.println("✅ Inserted " + rows + " sample products.");
                }
            } else {
                System.out.println("Products already exist.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Insert error: " + e.getMessage());
        }
    }
}