package com.ecommerce.ui;

import com.ecommerce.db.OrderDAO;
import com.ecommerce.db.ProductDAO;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.ShoppingCart;
import com.ecommerce.model.User;
import com.ecommerce.report.ReceiptGenerator;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CartWindow {

    private ShoppingCart cart;
    private Stage stage;
    private ListView<CartItem> listView;
    private Label totalLabel;
    private Button checkoutBtn;
    private Scene parentScene;
    private User currentUser;
    private CheckBox selectAllCheckBox;
    private Map<CartItem, Boolean> selectionMap = new HashMap<>();
    private ProductDAO productDAO;   // to get product names

    public CartWindow(ShoppingCart cart, Scene parentScene, User currentUser) {
        this.cart = cart;
        this.parentScene = parentScene;
        this.currentUser = currentUser;
        this.productDAO = new ProductDAO();
        stage = new Stage();
        stage.setTitle("Your Shopping Cart");
        buildUI();
    }

    private void buildUI() {
        listView = new ListView<>();
        listView.setItems(cart.getItems());
        listView.setCellFactory(lv -> new CartItemCell());

        totalLabel = new Label("Total (selected): ₱0.00");
        totalLabel.getStyleClass().add("text-title");

        checkoutBtn = new Button("Checkout Selected");
        checkoutBtn.getStyleClass().add("btn-success");
        checkoutBtn.setOnAction(e -> checkoutSelected());

        Button closeBtn = new Button("Continue Shopping");
        closeBtn.getStyleClass().add("btn-secondary");
        closeBtn.setOnAction(e -> stage.close());

        selectAllCheckBox = new CheckBox("Select All");
        selectAllCheckBox.setOnAction(e -> {
            boolean selected = selectAllCheckBox.isSelected();
            for (CartItem item : cart.getItems()) {
                selectionMap.put(item, selected);
            }
            listView.refresh();
            updateTotal();
        });

        HBox bottomLeft = new HBox(10, selectAllCheckBox);
        bottomLeft.setAlignment(Pos.CENTER_LEFT);

        HBox buttonBar = new HBox(15, closeBtn, checkoutBtn);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        HBox bottomBox = new HBox(20, bottomLeft, buttonBar);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));

        VBox bottomContainer = new VBox(10, totalLabel, bottomBox);
        bottomContainer.setAlignment(Pos.CENTER_RIGHT);
        bottomContainer.getStyleClass().add("bottom-bar");

        BorderPane root = new BorderPane();
        root.setCenter(listView);
        root.setBottom(bottomContainer);

        Scene scene = new Scene(root, 700, 500);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {}
        stage.setScene(scene);
        stage.centerOnScreen();
        for (CartItem item : cart.getItems()) {
            selectionMap.put(item, true);
        }
        selectAllCheckBox.setSelected(true);
        updateTotal();

        // Listen for cart changes
        cart.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends CartItem> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (CartItem added : c.getAddedSubList()) {
                        selectionMap.put(added, true);
                    }
                }
                if (c.wasRemoved()) {
                    for (CartItem removed : c.getRemoved()) {
                        selectionMap.remove(removed);
                    }
                }
            }
            updateSelectAllState();
            updateTotal();
            listView.refresh();
        });
    }

    private void updateSelectAllState() {
        if (cart.getItems().isEmpty()) {
            selectAllCheckBox.setSelected(false);
            return;
        }
        boolean allSelected = cart.getItems().stream().allMatch(item -> selectionMap.getOrDefault(item, false));
        selectAllCheckBox.setSelected(allSelected);
    }

    private void updateTotal() {
        double totalSelected = cart.getItems().stream()
                .filter(item -> selectionMap.getOrDefault(item, false))
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
        totalLabel.setText(String.format("Total (selected): ₱%.2f", totalSelected));
        checkoutBtn.setDisable(totalSelected == 0);
    }

    private void checkoutSelected() {
        if (cart.isEmpty()) return;

        ShoppingCart selectedCart = new ShoppingCart();
        for (CartItem item : cart.getItems()) {
            if (selectionMap.getOrDefault(item, false)) {
                selectedCart.addVariant(item.getVariant(), item.getQuantity(), item.getFinalPrice());
            }
        }

        if (selectedCart.isEmpty()) {
            showThemedAlert(Alert.AlertType.WARNING, "No items selected", "Please select at least one item to checkout.");
            return;
        }

        TextInputDialog nameDialog = new TextInputDialog(currentUser != null ? currentUser.getFullName() : "");
        nameDialog.setTitle("CØDE LØCK System");
        nameDialog.setHeaderText("Finalize Your Order");
        nameDialog.setContentText("Please enter your full name for the receipt:");
        if (parentScene != null && parentScene.getStylesheets().size() > 0) {
            nameDialog.getDialogPane().getStylesheets().add(parentScene.getStylesheets().get(0));
        }

        nameDialog.showAndWait().ifPresent(customerName -> {
            if (customerName.trim().isEmpty()) {
                showThemedAlert(Alert.AlertType.ERROR, "Input Error", "Customer name cannot be empty.");
                return;
            }

            OrderDAO orderDAO = new OrderDAO();
            int orderId = orderDAO.saveOrder(currentUser, selectedCart);
            if (orderId != -1) {
                // Remove selected items from original cart
                for (CartItem item : selectedCart.getItems()) {
                    cart.updateQuantity(item.getVariant(), 0);
                }
                updateTotal();
                stage.close();

                Order order = orderDAO.getOrderById(orderId);
                if (order != null) {
                    String receiptPath = "receipt_" + orderId + ".pdf";
                    ReceiptGenerator.generateReceipt(order, receiptPath);
                    try {
                        File pdfFile = new File(receiptPath);
                        if (pdfFile.exists() && java.awt.Desktop.isDesktopSupported()) {
                            java.awt.Desktop.getDesktop().open(pdfFile);
                        }
                    } catch (Exception ex) {}
                    showThemedAlert(Alert.AlertType.INFORMATION,
                            "Thank you for your purchase, " + customerName + "!",
                            "Order #" + orderId + " was placed successfully.\nYour PDF receipt has been generated.");
                }
            } else {
                showThemedAlert(Alert.AlertType.ERROR, "Checkout Failed", "Could not process order. Please check database connection and stock.");
            }
        });
    }

    private void showThemedAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("CØDE LØCK System");
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (parentScene != null && parentScene.getStylesheets().size() > 0) {
            alert.getDialogPane().getStylesheets().add(parentScene.getStylesheets().get(0));
        }
        alert.showAndWait();
    }

    public void showAndWait() {
        stage.showAndWait();
    }

    // Cache for product names (optional)
    private Map<Integer, String> productNameCache = new HashMap<>();
    private String getProductName(int productId) {
        if (!productNameCache.containsKey(productId)) {
            productNameCache.put(productId, productDAO.getProductById(productId).getName());
        }
        return productNameCache.get(productId);
    }

    private class CartItemCell extends ListCell<CartItem> {
        private HBox content;
        private CheckBox selectCheckBox;
        private Label nameLabel;
        private Label priceLabel;
        private Spinner<Integer> quantitySpinner;
        private Button removeBtn;
        private boolean updatingSpinner = false;

        public CartItemCell() {
            selectCheckBox = new CheckBox();
            selectCheckBox.setOnAction(e -> {
                CartItem item = getItem();
                if (item != null) {
                    selectionMap.put(item, selectCheckBox.isSelected());
                    updateTotal();
                    updateSelectAllState();
                }
            });

            nameLabel = new Label();
            nameLabel.setPrefWidth(280);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            priceLabel = new Label();
            priceLabel.setPrefWidth(80);
            priceLabel.getStyleClass().add("text-price");

            quantitySpinner = new Spinner<>(1, 999, 1);
            quantitySpinner.setEditable(true);
            quantitySpinner.setPrefWidth(70);

            removeBtn = new Button("X");
            removeBtn.getStyleClass().add("btn-danger");

            content = new HBox(10, selectCheckBox, nameLabel, priceLabel, quantitySpinner, removeBtn);
            content.setAlignment(Pos.CENTER_LEFT);
            content.getStyleClass().add("cart-item-cell");

            quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (updatingSpinner) return;
                CartItem currentItem = getItem();
                if (currentItem != null && newVal != null && newVal > 0) {
                    cart.updateQuantity(currentItem.getVariant(), newVal);
                    priceLabel.setText(String.format("₱%.2f", currentItem.getFinalPrice() * newVal));
                    updateTotal();
                }
            });

            removeBtn.setOnAction(e -> {
                CartItem currentItem = getItem();
                if (currentItem != null) {
                    cart.updateQuantity(currentItem.getVariant(), 0);
                    selectionMap.remove(currentItem);
                    updateTotal();
                    updateSelectAllState();
                }
            });
        }

        @Override
        protected void updateItem(CartItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            selectCheckBox.setSelected(selectionMap.getOrDefault(item, false));

            String productBaseName = getProductName(item.getVariant().getProductId());
            String spec = item.getVariant().getSpecName();
            String color = item.getVariant().getColor();
            StringBuilder display = new StringBuilder(productBaseName);
            if (spec != null && !spec.isEmpty() && !spec.equalsIgnoreCase("Standard")) {
                display.append(" (").append(spec);
                if (color != null && !color.isEmpty()) {
                    display.append(", ").append(color);
                }
                display.append(")");
            } else if (color != null && !color.isEmpty()) {
                display.append(" (").append(color).append(")");
            }
            nameLabel.setText(display.toString());
            priceLabel.setText(String.format("₱%.2f", item.getFinalPrice() * item.getQuantity()));
            updatingSpinner = true;
            quantitySpinner.getValueFactory().setValue(item.getQuantity());
            updatingSpinner = false;
            setGraphic(content);
        }
    }
}