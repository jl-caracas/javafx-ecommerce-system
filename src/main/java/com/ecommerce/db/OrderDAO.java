package com.ecommerce.db;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.ShoppingCart;
import com.ecommerce.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    private Connection connection;
    private ProductDAO productDAO;

    public OrderDAO() {
        connection = DatabaseManager.getInstance().getConnection();
        productDAO = new ProductDAO();
    }

    public int saveOrder(User user, ShoppingCart cart) {
        int orderId = -1;
        try {
            connection.setAutoCommit(false);

            String orderSql = "INSERT INTO orders (customer_name, user_id, order_date, total, status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, user.getFullName());
                pstmt.setInt(2, user.getId());
                pstmt.setString(3, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                pstmt.setDouble(4, cart.getTotal());
                pstmt.setString(5, "PENDING");
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) orderId = rs.getInt(1);
            }

            if (orderId == -1) throw new SQLException("Failed to get order ID");

            String itemSql = "INSERT INTO order_items (order_id, product_name, product_price, quantity) VALUES (?, ?, ?, ?)";
            String updateVariantStockSql = "UPDATE product_variants SET stock = stock - ? WHERE id = ? AND stock >= ?";
            String updateProductStockSql = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";

            for (CartItem item : cart.getItems()) {
                String productName = productDAO.getProductById(item.getVariant().getProductId()).getName();
                String spec = item.getVariant().getSpecName();
                String color = item.getVariant().getColor();
                StringBuilder displayName = new StringBuilder(productName);
                if (spec != null && !spec.isEmpty() && !spec.equalsIgnoreCase("Standard")) {
                    displayName.append(" (").append(spec);
                    if (color != null && !color.isEmpty()) displayName.append(", ").append(color);
                    displayName.append(")");
                } else if (color != null && !color.isEmpty()) {
                    displayName.append(" (").append(color).append(")");
                }

                try (PreparedStatement pstmt = connection.prepareStatement(itemSql)) {
                    pstmt.setInt(1, orderId);
                    pstmt.setString(2, displayName.toString());
                    pstmt.setDouble(3, item.getFinalPrice());
                    pstmt.setInt(4, item.getQuantity());
                    pstmt.executeUpdate();
                }

                // Stock reduction
                if (item.getVariant().getId() > 0) {
                    try (PreparedStatement pstmt = connection.prepareStatement(updateVariantStockSql)) {
                        pstmt.setInt(1, item.getQuantity());
                        pstmt.setInt(2, item.getVariant().getId());
                        pstmt.setInt(3, item.getQuantity());
                        if (pstmt.executeUpdate() == 0)
                            throw new SQLException("Insufficient stock for variant: " + displayName);
                    }
                } else {
                    try (PreparedStatement pstmt = connection.prepareStatement(updateProductStockSql)) {
                        pstmt.setInt(1, item.getQuantity());
                        pstmt.setInt(2, item.getVariant().getProductId());
                        pstmt.setInt(3, item.getQuantity());
                        if (pstmt.executeUpdate() == 0)
                            throw new SQLException("Insufficient stock for product: " + productName);
                    }
                }
            }

            connection.commit();
            System.out.println("Order saved with ID: " + orderId);
        } catch (SQLException e) {
            e.printStackTrace();
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return -1;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
        return orderId;
    }


    public Order getOrderById(int orderId) {
        String orderSql = "SELECT * FROM orders WHERE id = ?";
        String itemsSql = "SELECT * FROM order_items WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(orderSql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String customerName = rs.getString("customer_name");
                int userId = rs.getInt("user_id");
                LocalDateTime orderDate = LocalDateTime.parse(rs.getString("order_date"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                double total = rs.getDouble("total");
                String status = rs.getString("status");
                List<OrderItem> items = new ArrayList<>();
                try (PreparedStatement pstmt2 = connection.prepareStatement(itemsSql)) {
                    pstmt2.setInt(1, orderId);
                    ResultSet rs2 = pstmt2.executeQuery();
                    while (rs2.next()) {
                        items.add(new OrderItem(
                            rs2.getInt("id"),
                            rs2.getInt("order_id"),
                            rs2.getString("product_name"),
                            rs2.getDouble("product_price"),
                            rs2.getInt("quantity")
                        ));
                    }
                }
                return new Order(orderId, customerName, userId, orderDate, total, status, items);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id, customer_name, user_id, order_date, total, status FROM orders ORDER BY order_date DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(new Order(
                    rs.getInt("id"),
                    rs.getString("customer_name"),
                    rs.getInt("user_id"),
                    LocalDateTime.parse(rs.getString("order_date"), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    rs.getDouble("total"),
                    rs.getString("status"),
                    new ArrayList<>()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                orders.add(new Order(
                    rs.getInt("id"),
                    rs.getString("customer_name"),
                    rs.getInt("user_id"),
                    LocalDateTime.parse(rs.getString("order_date"), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    rs.getDouble("total"),
                    rs.getString("status"),
                    new ArrayList<>()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = ? ORDER BY order_date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                orders.add(new Order(
                    rs.getInt("id"),
                    rs.getString("customer_name"),
                    rs.getInt("user_id"),
                    LocalDateTime.parse(rs.getString("order_date"), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    rs.getDouble("total"),
                    rs.getString("status"),
                    new ArrayList<>()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public boolean completeOrder(int orderId) {
        String sql = "UPDATE orders SET status = 'COMPLETED' WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}