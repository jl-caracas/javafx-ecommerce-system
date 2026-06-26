package com.ecommerce.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ShoppingCart {
    private ObservableList<CartItem> items = FXCollections.observableArrayList();

    // For backward compatibility 
    public void addProduct(Product product, int quantity) {
        // Create a dummy variant from the product
        ProductVariant dummy = new ProductVariant(0, product.getId(), "Standard", "", 0, product.getStock());
        addVariant(dummy, quantity, product.getPrice());
    }

    public void addVariant(ProductVariant variant, int quantity, double finalPrice) {
        for (CartItem item : items) {
            if (item.getVariant().getId() == variant.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(variant, quantity, finalPrice));
    }

    public void removeVariant(ProductVariant variant) {
        items.removeIf(item -> item.getVariant().getId() == variant.getId());
    }

    public void updateQuantity(ProductVariant variant, int newQuantity) {
        if (newQuantity <= 0) {
            removeVariant(variant);
            return;
        }
        for (CartItem item : items) {
            if (item.getVariant().getId() == variant.getId()) {
                item.setQuantity(newQuantity);
                break;
            }
        }
    }

    public ObservableList<CartItem> getItems() {
        return items;
    }

    public double getTotal() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    public int getTotalItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}