package com.ecommerce.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_FILE = "ecommerce.db";

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DB_FILE;
            connection = DriverManager.getConnection(url);
            System.out.println("✅ Connected to SQLite database. File: " + DB_FILE);
            createTables();
            addMissingColumns();
            insertDefaultAdmin();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTables() {
        String createProducts = """
            CREATE TABLE IF NOT EXISTS products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT,
                category TEXT,
                price REAL NOT NULL,
                stock INTEGER NOT NULL,
                image_path TEXT
            );
        """;
        String createOrders = """
            CREATE TABLE IF NOT EXISTS orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_name TEXT NOT NULL,
                order_date TEXT NOT NULL,
                total REAL NOT NULL
            );
        """;
        String createOrderItems = """
            CREATE TABLE IF NOT EXISTS order_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                product_name TEXT NOT NULL,
                product_price REAL NOT NULL,
                quantity INTEGER NOT NULL,
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
            );
        """;
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                full_name TEXT NOT NULL,
                address TEXT,
                gcash_number TEXT,
                role TEXT DEFAULT 'CUSTOMER'
            );
        """;
        String createProductVariants = """
            CREATE TABLE IF NOT EXISTS product_variants (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                product_id INTEGER NOT NULL,
                spec_name TEXT NOT NULL,
                color TEXT,
                price_modifier REAL DEFAULT 0,
                stock INTEGER NOT NULL,
                FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
            );
        """;
        // New user_cart table: stores variant_id instead of product_id
        String createUserCart = """
            CREATE TABLE IF NOT EXISTS user_cart (
                user_id INTEGER NOT NULL,
                variant_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                PRIMARY KEY (user_id, variant_id),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
            );
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createProducts);
            stmt.execute(createOrders);
            stmt.execute(createOrderItems);
            stmt.execute(createUsers);
            stmt.execute(createProductVariants);
            stmt.execute(createUserCart);
            System.out.println("✅ Tables ready.");
        } catch (SQLException e) {
            System.out.println("❌ Table creation error: " + e.getMessage());
        }
    }

    private void addMissingColumns() {
        String[] alterStatements = {
            "ALTER TABLE orders ADD COLUMN user_id INTEGER DEFAULT 0",
            "ALTER TABLE orders ADD COLUMN status TEXT DEFAULT 'PENDING'"
        };
        try (Statement stmt = connection.createStatement()) {
            for (String sql : alterStatements) {
                try {
                    stmt.execute(sql);
                    System.out.println("✅ Added missing column: " + sql);
                } catch (SQLException e) {
                    if (!e.getMessage().contains("duplicate column name")) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertDefaultAdmin() {
        String checkAdmin = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
        String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt());
        String insertAdmin = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertAdmin)) {
            var rs = connection.createStatement().executeQuery(checkAdmin);
            if (rs.next() && rs.getInt(1) == 0) {
                pstmt.setString(1, "admin");
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, "Administrator");
                pstmt.setString(4, "ADMIN");
                pstmt.executeUpdate();
                System.out.println("✅ Default admin user created: admin / admin123");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}