package com.ecommerce.db;

import com.ecommerce.model.ProductVariant;
import com.ecommerce.model.ShoppingCart;
import java.sql.*;

public class CartDAO {

    private Connection connection;

    public CartDAO() {
        connection = DatabaseManager.getInstance().getConnection();
    }

    public void loadCartForUser(int userId, ShoppingCart cart) {
        String sql = "SELECT variant_id, quantity FROM user_cart WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            ProductVariantDAO variantDAO = new ProductVariantDAO();
            ProductDAO productDAO = new ProductDAO();
            while (rs.next()) {
                int variantId = rs.getInt("variant_id");
                int quantity = rs.getInt("quantity");
                ProductVariant variant = variantDAO.getVariantById(variantId);
                if (variant != null) {
                    double basePrice = productDAO.getProductById(variant.getProductId()).getPrice();
                    double finalPrice = basePrice + variant.getPriceModifier();
                    cart.addVariant(variant, quantity, finalPrice);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveCartForUser(int userId, ShoppingCart cart) {
        String deleteSql = "DELETE FROM user_cart WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String insertSql = "INSERT INTO user_cart (user_id, variant_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
            for (var item : cart.getItems()) {
                int variantId = item.getVariant().getId();
                if (variantId == 0) continue; // skip dummy variants
                pstmt.setInt(1, userId);
                pstmt.setInt(2, variantId);
                pstmt.setInt(3, item.getQuantity());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}