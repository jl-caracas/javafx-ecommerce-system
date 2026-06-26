package com.ecommerce.db;

import com.ecommerce.model.ProductVariant;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductVariantDAO {

    private Connection connection;

    public ProductVariantDAO() {
        connection = DatabaseManager.getInstance().getConnection();
    }

    public List<ProductVariant> getVariantsForProduct(int productId) {
        List<ProductVariant> list = new ArrayList<>();
        String sql = "SELECT * FROM product_variants WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new ProductVariant(
                    rs.getInt("id"),
                    rs.getInt("product_id"),
                    rs.getString("spec_name"),
                    rs.getString("color"),
                    rs.getDouble("price_modifier"),
                    rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ADD THIS MISSING METHOD
    public ProductVariant getVariantById(int id) {
        String sql = "SELECT * FROM product_variants WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new ProductVariant(
                    rs.getInt("id"),
                    rs.getInt("product_id"),
                    rs.getString("spec_name"),
                    rs.getString("color"),
                    rs.getDouble("price_modifier"),
                    rs.getInt("stock")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addVariant(ProductVariant variant) {
        String sql = "INSERT INTO product_variants (product_id, spec_name, color, price_modifier, stock) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, variant.getProductId());
            pstmt.setString(2, variant.getSpecName());
            pstmt.setString(3, variant.getColor());
            pstmt.setDouble(4, variant.getPriceModifier());
            pstmt.setInt(5, variant.getStock());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateVariant(ProductVariant variant) {
        String sql = "UPDATE product_variants SET spec_name=?, color=?, price_modifier=?, stock=? WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, variant.getSpecName());
            pstmt.setString(2, variant.getColor());
            pstmt.setDouble(3, variant.getPriceModifier());
            pstmt.setInt(4, variant.getStock());
            pstmt.setInt(5, variant.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteVariant(int id) {
        String sql = "DELETE FROM product_variants WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteVariantsForProduct(int productId) {
        String sql = "DELETE FROM product_variants WHERE product_id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateVariantStock(int variantId, int newStock) {
        String sql = "UPDATE product_variants SET stock = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newStock);
            pstmt.setInt(2, variantId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}