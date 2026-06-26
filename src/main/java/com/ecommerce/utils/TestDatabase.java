package com.ecommerce.utils;

import com.ecommerce.db.DatabaseManager;
import com.ecommerce.db.DatabaseInitializer;

public class TestDatabase {
    public static void main(String[] args) {
        // Initialize connection and tables
        DatabaseManager db = DatabaseManager.getInstance();
        DatabaseInitializer.insertSampleProducts();

        System.out.println("✅ Test completed. Look for 'ecommerce.db' in your project folder.");

        // Close connection (normally you close when app exits)
        db.closeConnection();
    }
}