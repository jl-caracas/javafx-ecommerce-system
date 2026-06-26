package com.ecommerce.ui;

import com.ecommerce.db.OrderDAO;
import com.ecommerce.db.ProductDAO;
import com.ecommerce.db.ProductVariantDAO;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.model.ProductVariant;
import com.ecommerce.report.ReceiptGenerator;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminPanelWindow {

    private Stage stage;
    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private ProductVariantDAO variantDAO;
    private ObservableList<Product> productList;
    private TableView<Product> productTable;

    public AdminPanelWindow() {
        productDAO  = new ProductDAO();
        orderDAO    = new OrderDAO();
        variantDAO  = new ProductVariantDAO();
        productList = FXCollections.observableArrayList();
        stage = new Stage();
        stage.setTitle("Admin Panel");
        buildUI();
    }

    private void buildUI() {
        TabPane tabPane = new TabPane();

        Tab productsTab = new Tab("Products");
        productsTab.setClosable(false);
        productsTab.setContent(createProductManagementPane());

        Tab pendingTab = new Tab("Pending Orders");
        pendingTab.setClosable(false);
        pendingTab.setContent(createOrdersTable("PENDING"));

        Tab completedTab = new Tab("Completed Orders");
        completedTab.setClosable(false);
        completedTab.setContent(createOrdersTable("COMPLETED"));

        tabPane.getTabs().addAll(productsTab, pendingTab, completedTab);

        productsTab.setOnSelectionChanged(e -> {
            if (productsTab.isSelected()) refreshProductTable();
        });

        VBox root = new VBox(tabPane);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root, 1000, 700);
        try {
            scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {}
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    // ─────────────────────────────────────────────────────────────────────────
    private VBox createProductManagementPane() {
        productTable = new TableView<>();
        productTable.setItems(productList);
        productTable.setPrefHeight(500);
        productTable.setPlaceholder(new Label("No products found."));

        // FIX: Columns stretch to fill the full table width instead of
        //      leaving dead space on the right.
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(50);

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setMaxWidth(100);

        TableColumn<Product, Double> priceCol = new TableColumn<>("Base Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setMaxWidth(90);
        priceCol.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("₱%.2f", item));
            }
        });

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setMaxWidth(70);

        productTable.getColumns().addAll(idCol, nameCol, descCol, categoryCol, priceCol, stockCol);

        // FIX: Double-click a row to open the Edit dialog directly.
        productTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showProductDialog(row.getItem());
                }
            });
            return row;
        });

        // CRUD buttons
        Button addBtn     = new Button("➕ Add Product");
        Button editBtn    = new Button("✏️ Edit Product");
        Button deleteBtn  = new Button("🗑 Delete Product");
        Button refreshBtn = new Button("↻ Refresh");

        addBtn.setOnAction(e -> showProductDialog(null));
        editBtn.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) showProductDialog(selected);
            else showAlert("Please select a product to edit.\n(You can also double-click a row.)");
        });
        
        // FIXED SYNTAX ERROR HERE
        deleteBtn.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete '" + selected.getName() + "'?\nThis will also delete all its variants.",
                    ButtonType.YES, ButtonType.NO);
                confirm.setTitle("Confirm Delete");
                if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                    productDAO.deleteProduct(selected.getId());
                    variantDAO.deleteVariantsForProduct(selected.getId());
                    refreshProductTable();
                }
            } else {
                showAlert("Please select a product to delete.");
            }
        });
        
        refreshBtn.setOnAction(e -> refreshProductTable());

        // Tip label
        Label tipLabel = new Label("💡 Tip: Double-click a row to edit it quickly.");
        tipLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

        HBox buttonBar = new HBox(10, addBtn, editBtn, deleteBtn, refreshBtn);
        buttonBar.setPadding(new Insets(5, 0, 5, 0));

        VBox vbox = new VBox(8, productTable, buttonBar, tipLabel);
        vbox.setPadding(new Insets(10));
        refreshProductTable();
        return vbox;
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void showProductDialog(Product existing) {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Add Product" : "Edit Product");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField      = new TextField();
        TextField descField      = new TextField();
        TextField categoryField  = new TextField();
        TextField basePriceField = new TextField();

        // Image
        ImageView imagePreview = new ImageView();
        imagePreview.setFitWidth(100);
        imagePreview.setFitHeight(100);
        imagePreview.setPreserveRatio(true);
        Button uploadBtn = new Button("Upload Image");
        TextField imagePathField = new TextField();
        imagePathField.setEditable(false);
        imagePathField.setPromptText("Image will be saved automatically");

        if (existing != null) {
            nameField.setText(existing.getName());
            descField.setText(existing.getDescription());
            categoryField.setText(existing.getCategory());
            basePriceField.setText(String.valueOf(existing.getPrice()));
            if (existing.getImagePath() != null && !existing.getImagePath().isEmpty()) {
                try {
                    imagePreview.setImage(
                        new Image(getClass().getResourceAsStream(existing.getImagePath())));
                    imagePathField.setText(existing.getImagePath());
                } catch (Exception ignored) {}
            }
        }

        uploadBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Product Image");
            fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File selected = fc.showOpenDialog(dialog);
            if (selected != null) {
                try {
                    File imagesDir = new File("src/main/resources/images/");
                    if (!imagesDir.exists()) imagesDir.mkdirs();
                    String ext      = selected.getName().substring(selected.getName().lastIndexOf("."));
                    String fileName = System.currentTimeMillis() + ext;
                    File dest = new File("src/main/resources/images/" + fileName);
                    Files.copy(selected.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    String relPath = "/images/" + fileName;
                    imagePathField.setText(relPath);
                    imagePreview.setImage(new Image(dest.toURI().toString()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showAlert("Could not save image.");
                }
            }
        });

        // Variants table
        Label variantsLabel = new Label("Product Variants  (Spec · Color · Price Modifier · Stock)");
        variantsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        TableView<ProductVariant> variantsTable = new TableView<>();
        ObservableList<ProductVariant> variantList = FXCollections.observableArrayList();
        if (existing != null) variantList.setAll(variantDAO.getVariantsForProduct(existing.getId()));
        variantsTable.setItems(variantList);
        variantsTable.setPrefHeight(150);
        variantsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ProductVariant, String> specCol = new TableColumn<>("Specification");
        specCol.setCellValueFactory(new PropertyValueFactory<>("specName"));
        TableColumn<ProductVariant, String> colorCol = new TableColumn<>("Color");
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));
        TableColumn<ProductVariant, Double> modifierCol = new TableColumn<>("Price +");
        modifierCol.setCellValueFactory(new PropertyValueFactory<>("priceModifier"));
        modifierCol.setMaxWidth(80);
        modifierCol.setCellFactory(col -> new TableCell<ProductVariant, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("+₱%.2f", item));
            }
        });
        TableColumn<ProductVariant, Integer> stockVariantCol = new TableColumn<>("Stock");
        stockVariantCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockVariantCol.setMaxWidth(60);

        TableColumn<ProductVariant, Void> actionVariantCol = new TableColumn<>("Del");
        actionVariantCol.setMaxWidth(55);
        actionVariantCol.setCellFactory(col -> new TableCell<ProductVariant, Void>() {
            private final Button del = new Button("✕");
            { del.setOnAction(e -> variantList.remove(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : del);
            }
        });
        variantsTable.getColumns().addAll(specCol, colorCol, modifierCol, stockVariantCol, actionVariantCol);

        Button addVariantBtn = new Button("+ Add Variant");
        addVariantBtn.setOnAction(e -> {
            Stage varDialog = new Stage();
            varDialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            varDialog.setTitle("Add Variant");
            GridPane vg = new GridPane();
            vg.setHgap(10); vg.setVgap(10); vg.setPadding(new Insets(20));

            TextField specField     = new TextField(); specField.setPromptText("e.g. 8GB/256GB");
            TextField colorField    = new TextField(); colorField.setPromptText("e.g. Black");
            TextField modField      = new TextField(); modField.setPromptText("Additional price");
            TextField stockVarField = new TextField(); stockVarField.setPromptText("Stock quantity");

            vg.add(new Label("Specification:"),  0, 0); vg.add(specField,     1, 0);
            vg.add(new Label("Color:"),           0, 1); vg.add(colorField,    1, 1);
            vg.add(new Label("Price Modifier:"),  0, 2); vg.add(modField,      1, 2);
            vg.add(new Label("Stock:"),           0, 3); vg.add(stockVarField, 1, 3);

            Button saveVarBtn   = new Button("Save");
            Button cancelVarBtn = new Button("Cancel");
            // Allow Enter to confirm the variant dialog too
            saveVarBtn.setDefaultButton(true);

            saveVarBtn.setOnAction(ev -> {
                try {
                    String spec  = specField.getText().trim();
                    String color = colorField.getText().trim();
                    double mod   = Double.parseDouble(modField.getText().trim());
                    int    stock = Integer.parseInt(stockVarField.getText().trim());
                    if (spec.isEmpty()) { showAlert("Specification is required."); return; }
                    variantList.add(new ProductVariant(0, 0, spec, color, mod, stock));
                    varDialog.close();
                } catch (NumberFormatException ex) {
                    showAlert("Please enter valid numbers for modifier and stock.");
                }
            });
            cancelVarBtn.setOnAction(ev -> varDialog.close());
            vg.add(new HBox(10, saveVarBtn, cancelVarBtn), 1, 4);
            varDialog.setScene(new Scene(vg, 380, 240));
            varDialog.centerOnScreen();
            varDialog.showAndWait();
        });

        VBox variantsBox = new VBox(5, variantsLabel, variantsTable, addVariantBtn);

        grid.add(new Label("Name:"),       0, 0); grid.add(nameField,      1, 0);
        grid.add(new Label("Description:"),0, 1); grid.add(descField,      1, 1);
        grid.add(new Label("Category:"),   0, 2); grid.add(categoryField,  1, 2);
        grid.add(new Label("Base Price:"), 0, 3); grid.add(basePriceField, 1, 3);
        grid.add(new Label("Image:"),      0, 4); grid.add(uploadBtn,      1, 4);
        grid.add(imagePreview,             1, 5);
        grid.add(imagePathField,           1, 6);
        grid.add(variantsBox,              0, 7, 2, 1);

        Button saveBtn   = new Button("💾 Save");
        Button cancelBtn = new Button("Cancel");
        saveBtn.setDefaultButton(true);   // Enter = Save in this dialog too

        saveBtn.setOnAction(ev -> {
            try {
                String name      = nameField.getText().trim();
                String desc      = descField.getText().trim();
                String cat       = categoryField.getText().trim();
                double basePrice = Double.parseDouble(basePriceField.getText().trim());
                String imgPath   = imagePathField.getText().trim();

                if (name.isEmpty() || cat.isEmpty() || basePrice < 0) {
                    showAlert("Name, category, and a valid base price are required.");
                    return;
                }

                Product product;
                if (existing == null) {
                    product = new Product(name, desc, cat, basePrice, 0,
                                         imgPath.isEmpty() ? null : imgPath);
                    int newId = productDAO.addProduct(product);
                    if (newId == -1) {
                        showAlert("Error adding product.");
                        return;
                    }
                    product.setId(newId);
                } else {
                    product = existing;
                    product.setName(name);
                    product.setDescription(desc);
                    product.setCategory(cat);
                    product.setPrice(basePrice);
                    product.setImagePath(imgPath.isEmpty() ? null : imgPath);
                    productDAO.updateProduct(product);
                    variantDAO.deleteVariantsForProduct(product.getId());
                }

                for (ProductVariant v : variantList) {
                    v.setProductId(product.getId());
                    variantDAO.addVariant(v);
                }
                refreshProductTable();
                dialog.close();

            } catch (NumberFormatException ex) {
                showAlert("Please enter a valid number for base price.");
            }
        });
        cancelBtn.setOnAction(ev -> dialog.close());

        HBox btnBox = new HBox(10, saveBtn, cancelBtn);
        btnBox.setPadding(new Insets(10, 0, 0, 0));
        grid.add(btnBox, 1, 8);

        dialog.setScene(new Scene(grid, 650, 650));
        dialog.centerOnScreen();
        dialog.showAndWait();
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void refreshProductTable() {
        productList.setAll(productDAO.getAllProducts());
        productTable.refresh();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Admin Panel");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ─────────────────────────────────────────────────────────────────────────
    private VBox createOrdersTable(String statusFilter) {
        ObservableList<Order> orderList = FXCollections.observableArrayList();
        TableView<Order> orderTable = new TableView<>(orderList);
        orderTable.setPlaceholder(new Label("No orders found."));
        // FIX: stretch columns to fill table width
        // Use a custom resize policy that respects min/pref widths instead of squishing columns
        orderTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        ScrollPane scrollPane = new ScrollPane(orderTable);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        TableColumn<Order, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        idCol.setMinWidth(50);
        idCol.setMaxWidth(50);

        TableColumn<Order, String> nameCol = new TableColumn<>("Customer");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomerName()));
        nameCol.setMinWidth(140);

        TableColumn<Order, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getOrderDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a"))));
        dateCol.setMinWidth(160);

        TableColumn<Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(c -> new SimpleStringProperty(
            String.format("₱%.2f", c.getValue().getTotal())));
        totalCol.setMinWidth(80);
        totalCol.setMaxWidth(80);

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setMinWidth(80);
        statusCol.setMaxWidth(80);

        TableColumn<Order, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setMinWidth(180);
        actionCol.setMaxWidth(180);
        actionCol.setCellFactory(col -> new TableCell<Order, Void>() {
            private final HBox   buttons     = new HBox(5);
            private final Button viewBtn     = new Button("View");
            private final Button completeBtn = new Button("Complete");

            {
                String s = "-fx-background-color: #e0e0e0; -fx-text-fill: #333; "
                         + "-fx-font-size: 11px; -fx-padding: 4 8 4 8; -fx-background-radius: 5;";
                viewBtn.setStyle(s);
                completeBtn.setStyle(s);

                viewBtn.setOnAction(e -> viewOrderReceipt(
                    getTableView().getItems().get(getIndex())));

                completeBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    if ("PENDING".equals(order.getStatus())) {
                        if (orderDAO.completeOrder(order.getId())) {
                            order.setStatus("COMPLETED");
                            getTableView().refresh();
                            new Alert(Alert.AlertType.INFORMATION,
                                "Order #" + order.getId() + " marked as COMPLETED.").showAndWait();
                            orderList.setAll(orderDAO.getOrdersByStatus(statusFilter));
                        }
                    } else {
                        new Alert(Alert.AlertType.WARNING, "Order already completed.").showAndWait();
                    }
                });

                buttons.getChildren().add(viewBtn);
                if ("PENDING".equals(statusFilter)) buttons.getChildren().add(completeBtn);
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        orderTable.getColumns().addAll(idCol, nameCol, dateCol, totalCol, statusCol, actionCol);
        orderList.setAll(orderDAO.getOrdersByStatus(statusFilter));

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.setOnAction(e -> orderList.setAll(orderDAO.getOrdersByStatus(statusFilter)));

        VBox vbox = new VBox(10, scrollPane, refreshBtn);
        vbox.setPadding(new Insets(10));
        return vbox;
    }

    private void viewOrderReceipt(Order order) {
        Order full = orderDAO.getOrderById(order.getId());
        if (full != null) {
            String path = "receipt_admin_" + full.getId() + ".pdf";
            ReceiptGenerator.generateReceipt(full, path);
            try {
                File pdf = new File(path);
                if (pdf.exists() && java.awt.Desktop.isDesktopSupported())
                    java.awt.Desktop.getDesktop().open(pdf);
            } catch (Exception ignored) {
                System.out.println("Could not open PDF automatically.");
            }
        } else {
            new Alert(Alert.AlertType.ERROR, "Could not retrieve order details.").showAndWait();
        }
    }

    public void show() {
        refreshProductTable();
        stage.show();
    }
}