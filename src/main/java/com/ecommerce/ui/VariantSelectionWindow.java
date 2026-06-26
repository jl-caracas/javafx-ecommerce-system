package com.ecommerce.ui;

import com.ecommerce.db.ProductVariantDAO;
import com.ecommerce.model.Product;
import com.ecommerce.model.ProductVariant;
import com.ecommerce.model.ShoppingCart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VariantSelectionWindow {

    private Stage stage;
    private Product product;
    private ObservableList<ProductVariant> variants;
    private ShoppingCart cart;
    private Runnable onAddedCallback;

    public VariantSelectionWindow(Product product, ObservableList<ProductVariant> variants, ShoppingCart cart, Runnable onAdded) {
        this.product = product;
        this.variants = variants;
        this.cart = cart;
        this.onAddedCallback = onAdded;
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Select Product Variant - " + product.getName());
        buildUI();
    }

    private void buildUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        // Product image
        ImageView productImage = new ImageView();
        productImage.setFitWidth(180);
        productImage.setFitHeight(180);
        productImage.setPreserveRatio(true);
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            try {
                Image img = new Image(getClass().getResourceAsStream(product.getImagePath()));
                productImage.setImage(img);
            } catch (Exception e) {}
        }
        if (productImage.getImage() == null) {
            productImage.setImage(createPlaceholderImage());
        }

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label descLabel = new Label(product.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(380);

        Label basePriceLabel = new Label(String.format("Base Price: ₱%.2f", product.getPrice()));
        basePriceLabel.setStyle("-fx-font-weight: bold;");

        // Variants table – seamless design that blends with white window background
        TableView<ProductVariant> variantTable = new TableView<>();
        variantTable.setItems(variants);
        variantTable.setPlaceholder(new Label("No variants available. You can only buy the base product."));
        variantTable.setStyle("-fx-background-color: transparent; -fx-table-cell-border-color: transparent; -fx-border-color: transparent;");
        variantTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProductVariant, String> specCol = new TableColumn<>("Specification");
        specCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSpecName()));
        specCol.setMinWidth(140);

        TableColumn<ProductVariant, String> colorCol = new TableColumn<>("Color");
        colorCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getColor()));
        colorCol.setMinWidth(80);

        TableColumn<ProductVariant, Double> modifierCol = new TableColumn<>("Additional");
        modifierCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPriceModifier()).asObject());
        modifierCol.setCellFactory(col -> new TableCell<ProductVariant, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("+₱%.2f", item));
                // Style cell to blend with white background
                if (!empty) {
                    setStyle("-fx-background-color: transparent; -fx-table-cell-border-color: transparent;");
                }
            }
        });
        modifierCol.setMinWidth(90);

        TableColumn<ProductVariant, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getStock()).asObject());
        stockCol.setMinWidth(60);

        variantTable.getColumns().addAll(specCol, colorCol, modifierCol, stockCol);
        variantTable.setPrefHeight(160);

        // Style table rows to be transparent
        variantTable.setRowFactory(tv -> {
            TableRow<ProductVariant> row = new TableRow<>();
            row.setStyle("-fx-background-color: transparent; -fx-table-cell-border-color: transparent;");
            return row;
        });

        // Quantity spinner
        Label qtyLabel = new Label("Quantity:");
        Spinner<Integer> qtySpinner = new Spinner<>(1, 100, 1);
        qtySpinner.setEditable(true);

        // Selected variant label
        Label selectedLabel = new Label("No variant selected");
        selectedLabel.setStyle("-fx-text-fill: red;");

        variantTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedLabel.setText("Selected: " + selected.getSpecName() + (selected.getColor() != null ? " - " + selected.getColor() : ""));
                selectedLabel.setStyle("-fx-text-fill: green;");
                // Dynamically set max quantity based on stock
                qtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, selected.getStock(), 1));
            } else {
                selectedLabel.setText("No variant selected");
                selectedLabel.setStyle("-fx-text-fill: red;");
            }
        });

        // Add to Cart button
        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.setStyle("-fx-background-color: #ff5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        addToCartBtn.setOnAction(e -> {
            ProductVariant selected = variantTable.getSelectionModel().getSelectedItem();
            if (selected == null && !variants.isEmpty()) {
                showAlert("Please select a variant.");
                return;
            }
            if (selected == null) {
                // No variants – add base product as a dummy variant (price modifier 0)
                selected = new ProductVariant(0, product.getId(), "Standard", "", 0, product.getStock());
            }
            int quantity = qtySpinner.getValue();
            if (quantity > selected.getStock()) {
                showAlert("Only " + selected.getStock() + " units available for this variant.");
                return;
            }
            double finalPrice = product.getPrice() + selected.getPriceModifier();
            cart.addVariant(selected, quantity, finalPrice);
            if (onAddedCallback != null) onAddedCallback.run();
            stage.close();
        });

        VBox variantBox = new VBox(8, new Label("Select Configuration:"), variantTable);
        if (variants.isEmpty()) {
            variantBox.setVisible(false);
        }

        root.getChildren().addAll(productImage, nameLabel, descLabel, basePriceLabel, variantBox, qtyLabel, qtySpinner, selectedLabel, addToCartBtn);
        root.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(root, 460, 680);
        stage.setScene(scene);
        stage.centerOnScreen();

        // Style ONLY the column headers (not the data rows) with orange background + white text
        // Must be done after scene is shown so JavaFX has created the header nodes
        stage.setOnShown(ev -> {
            for (javafx.scene.Node header : variantTable.lookupAll(".column-header")) {
                header.setStyle("-fx-background-color: #ff5722; -fx-border-color: transparent; -fx-padding: 4px;");
                // Also find the label inside the header and make it white + bold
                for (javafx.scene.Node child : ((javafx.scene.layout.Region) header).getChildrenUnmodifiable()) {
                    if (child instanceof Label) {
                        child.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
                    }
                }
            }
        });
    }

    private Image createPlaceholderImage() {
        javafx.scene.image.WritableImage img = new javafx.scene.image.WritableImage(180, 180);
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(180, 180);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.LIGHTGRAY);
        gc.fillRect(0, 0, 180, 180);
        gc.setStroke(javafx.scene.paint.Color.DARKGRAY);
        gc.strokeText("No Image", 60, 95);
        canvas.snapshot(null, img);
        return img;
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Selection Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void show() {
        stage.showAndWait();
    }
}