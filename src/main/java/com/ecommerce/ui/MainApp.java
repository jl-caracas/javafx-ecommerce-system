package com.ecommerce.ui;

import com.ecommerce.db.CartDAO;
import com.ecommerce.db.DatabaseInitializer;
import com.ecommerce.db.ProductDAO;
import com.ecommerce.db.ProductVariantDAO;
import com.ecommerce.model.Product;
import com.ecommerce.model.ProductVariant;
import com.ecommerce.model.ShoppingCart;
import com.ecommerce.model.User;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
//import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class MainApp extends Application {

    private ProductDAO productDAO;
    private ProductVariantDAO variantDAO;
    private CartDAO cartDAO;
    private GridPane productGrid;
    private ComboBox<String> categoryCombo;
    private TextField searchField;
    private ShoppingCart shoppingCart = new ShoppingCart();
    private Button cartBtn;
    private Image placeholderImage;
    private Scene mainScene;
    private Stage primaryStage;
    private Button adminBtn;
    private Button historyBtn;
    private Button dashboardBtn;

    // Logged-in user
    private static User currentUser = null;

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User user) { currentUser = user; }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        DatabaseInitializer.insertSampleProducts();
        productDAO = new ProductDAO();
        variantDAO = new ProductVariantDAO();
        cartDAO = new CartDAO();
        placeholderImage = createPlaceholderImage();

        BorderPane topBar = createSearchFilterBar();

        productGrid = new GridPane();
        productGrid.setAlignment(Pos.TOP_CENTER);
        productGrid.setHgap(25);
        productGrid.setVgap(25);
        productGrid.setPadding(new Insets(30));

        ScrollPane scrollPane = new ScrollPane(productGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("scroll-pane");

        VBox mainLayout = new VBox(topBar, scrollPane);
        mainScene = new Scene(mainLayout, 1150, 750);
        try {
            mainScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS not found.");
        }

        primaryStage.setTitle("CØDE LØCK - Customer");
        primaryStage.setScene(mainScene);
        primaryStage.centerOnScreen();
        primaryStage.show();

        refreshProducts(productDAO.getAllProducts());
        updateCartBadge(false);
    }

    private BorderPane createSearchFilterBar() {
        BorderPane bar = new BorderPane();
        bar.getStyleClass().add("top-bar");

        Label logoLabel = new Label("CØDE LØCK");
        logoLabel.getStyleClass().add("logo-label");
        HBox leftBox = new HBox(logoLabel);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        categoryCombo = new ComboBox<>();
        categoryCombo.setPromptText("All Categories");
        refreshCategories();
        categoryCombo.setOnAction(e -> filterByCategory());

        // Refresh button – symbol only
        Button refreshBtn = new Button("↻");
        refreshBtn.getStyleClass().add("btn-outline");
        refreshBtn.setPrefWidth(40);
        refreshBtn.setTooltip(new Tooltip("Refresh products"));
        refreshBtn.setOnAction(e -> {
            searchField.clear();
            categoryCombo.setValue("All Categories");
            refreshProducts(productDAO.getAllProducts());
        });

        HBox centerBox = new HBox(10, categoryCombo, refreshBtn);
        centerBox.setAlignment(Pos.CENTER);

        // Search bar – reduced width
        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(170);
        searchField.getStyleClass().add("search-input");
        searchField.setOnAction(e -> performSearch());

        Button searchIconBtn = new Button("🔍");
        searchIconBtn.getStyleClass().add("search-icon-btn");
        searchIconBtn.setOnAction(e -> performSearch());

        HBox searchContainer = new HBox(searchField, searchIconBtn);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.getStyleClass().add("search-container");

        historyBtn = new Button("📦 Orders");
        historyBtn.getStyleClass().add("btn-nav");
        historyBtn.setOnAction(e -> new OrderHistoryWindow().show());

        // Dashboard button
        dashboardBtn = new Button("📊 Dashboard");
        dashboardBtn.getStyleClass().add("btn-nav");
        dashboardBtn.setOnAction(e -> showDashboard());

        cartBtn = new Button("🛒 Cart (0)");
        cartBtn.getStyleClass().add("badge-cart");
        cartBtn.setOnAction(e -> openCartWindow());

        Button userMenuBtn = new Button("👤");
        userMenuBtn.getStyleClass().add("btn-nav");
        userMenuBtn.setOnAction(e -> showUserMenu(userMenuBtn));

        adminBtn = new Button("🔧 Admin");
        adminBtn.getStyleClass().add("btn-nav");
        adminBtn.setVisible(false);
        adminBtn.setOnAction(e -> openAdminPanel());

        // Initially hide Orders & Dashboard for non-admin users
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            historyBtn.setVisible(false);
            dashboardBtn.setVisible(false);
        }

        HBox rightBox = new HBox(20, searchContainer, historyBtn, dashboardBtn, cartBtn, adminBtn, userMenuBtn);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        bar.setLeft(leftBox);
        bar.setCenter(centerBox);
        bar.setRight(rightBox);
        return bar;
    }

    private void showDashboard() {
        DashboardWindow dashboard = new DashboardWindow();
        dashboard.show();
    }

    private void showUserMenu(Button userMenuBtn) {
        ContextMenu menu = new ContextMenu();
        if (currentUser != null) {
            MenuItem logoutItem = new MenuItem("Logout (" + currentUser.getUsername() + ")");
            logoutItem.setOnAction(e -> logout());
            menu.getItems().add(logoutItem);
        } else {
            MenuItem loginItem = new MenuItem("Login");
            loginItem.setOnAction(e -> {
                LoginWindow loginWin = new LoginWindow();
                User user = loginWin.showAndWait(primaryStage);
                if (user != null) {
                    setCurrentUser(user);
                    loadCartForCurrentUser();
                    updateUIBasedOnRole();
                    refreshProducts(productDAO.getAllProducts());
                    updateCartBadge(false);
                }
            });
            menu.getItems().add(loginItem);

            MenuItem registerItem = new MenuItem("Register");
            registerItem.setOnAction(e -> {
                RegisterWindow regWin = new RegisterWindow();
                User newUser = regWin.showAndWait(primaryStage);
                if (newUser != null) {
                    setCurrentUser(newUser);
                    loadCartForCurrentUser();
                    updateUIBasedOnRole();
                    refreshProducts(productDAO.getAllProducts());
                    updateCartBadge(false);
                }
            });
            menu.getItems().add(registerItem);
        }
        menu.show(userMenuBtn, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void updateUIBasedOnRole() {
        if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
            primaryStage.setTitle("CØDE LØCK - Admin Mode");
            adminBtn.setVisible(true);
            historyBtn.setVisible(true);
            dashboardBtn.setVisible(true);
        } else {
            primaryStage.setTitle("CØDE LØCK - Customer");
            adminBtn.setVisible(false);
            historyBtn.setVisible(false);
            dashboardBtn.setVisible(false);
        }
    }

    private void loadCartForCurrentUser() {
        if (currentUser == null) return;
        shoppingCart.clear();
        cartDAO.loadCartForUser(currentUser.getId(), shoppingCart);
    }

    private void logout() {
        if (currentUser != null) {
            cartDAO.saveCartForUser(currentUser.getId(), shoppingCart);
        }
        setCurrentUser(null);
        shoppingCart.clear();
        updateCartBadge(false);
        primaryStage.setTitle("CØDE LØCK - Customer");
        LandingWindow landing = new LandingWindow();
        try {
            landing.start(primaryStage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openCartWindow() {
        CartWindow cartWindow = new CartWindow(shoppingCart, mainScene, currentUser);
        cartWindow.showAndWait();
        if (currentUser != null) {
            cartDAO.saveCartForUser(currentUser.getId(), shoppingCart);
        }
        updateCartBadge(false);
        refreshProducts(productDAO.getAllProducts());
    }

    private void updateCartBadge(boolean animate) {
        int count = shoppingCart.getTotalItemCount();
        cartBtn.setText("🛒 Cart (" + count + ")");
        if (animate) {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), cartBtn);
            st.setFromX(1.0);
            st.setFromY(1.0);
            st.setToX(1.15);
            st.setToY(1.15);
            st.setAutoReverse(true);
            st.setCycleCount(2);
            st.play();
        }
    }

    private void addToCart(Product product) {
        if (currentUser == null) {
            LoginWindow loginWindow = new LoginWindow();
            User user = loginWindow.showAndWait(primaryStage);
            if (user != null) {
                setCurrentUser(user);
                loadCartForCurrentUser();
                updateUIBasedOnRole();
            } else {
                return;
            }
        }

        // Load variants for this product
        List<ProductVariant> variants = variantDAO.getVariantsForProduct(product.getId());
        VariantSelectionWindow variantWindow = new VariantSelectionWindow(
            product,
            FXCollections.observableArrayList(variants),
            shoppingCart,
            () -> updateCartBadge(true)
        );
        variantWindow.show();
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (!keyword.isEmpty()) {
            refreshProducts(productDAO.searchByName(keyword));
        } else {
            refreshProducts(productDAO.getAllProducts());
        }
    }

    private void filterByCategory() {
        String selected = categoryCombo.getValue();
        if (selected != null && !selected.equals("All Categories")) {
            refreshProducts(productDAO.filterByCategory(selected));
        } else {
            refreshProducts(productDAO.getAllProducts());
        }
    }

    private void refreshCategories() {
        List<String> categories = productDAO.getAllCategories();
        categoryCombo.getItems().clear();
        categoryCombo.getItems().add("All Categories");
        categoryCombo.getItems().addAll(categories);
    }

    private void refreshProducts(List<Product> products) {
        productGrid.getChildren().clear();
        if (products.isEmpty()) {
            Label emptyLabel = new Label("No products found matching that criteria.");
            emptyLabel.getStyleClass().add("empty-state-label");
            productGrid.add(emptyLabel, 0, 0);
            return;
        }
        int row = 0, col = 0;
        for (Product p : products) {
            VBox card = createProductCard(p);
            productGrid.add(card, col, row);
            col++;
            if (col >= 4) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createProductCard(Product product) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(160);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        // Double‑click opens variant selection window (same as Add to Cart)
        imageView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                addToCart(product);
            }
        });

        String imagePath = product.getImagePath();
        Image img = null;
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                img = new Image(getClass().getResourceAsStream(imagePath));
            } catch (Exception e) { }
        }
        if (img == null) img = placeholderImage;
        imageView.setImage(img);

        Label nameLabel = new Label(product.getName());
        nameLabel.setPrefWidth(200);
        nameLabel.setMinHeight(40);
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.getStyleClass().add("product-name");

        Label priceLabel = new Label(String.format("₱%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("text-price");

        Label stockLabel = new Label("In Stock: " + product.getStock());
        stockLabel.getStyleClass().add("text-muted");

        Button addBtn = new Button("Add to Cart");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> addToCart(product));

        VBox card = new VBox(12, imageView, nameLabel, priceLabel, stockLabel, addBtn);
        card.getStyleClass().add("product-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(220);
        return card;
    }

    private Image createPlaceholderImage() {
        javafx.scene.image.WritableImage img = new javafx.scene.image.WritableImage(160, 160);
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(160, 160);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.LIGHTGRAY);
        gc.fillRect(0, 0, 160, 160);
        gc.setStroke(javafx.scene.paint.Color.DARKGRAY);
        gc.strokeText("No Image", 55, 85);
        canvas.snapshot(null, img);
        return img;
    }

    private void showThemedAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("CØDE LØCK System");
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (mainScene != null && mainScene.getStylesheets().size() > 0) {
            alert.getDialogPane().getStylesheets().add(mainScene.getStylesheets().get(0));
        }
        alert.showAndWait();
    }

    private void openAdminPanel() {
        AdminPanelWindow adminPanel = new AdminPanelWindow();
        adminPanel.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}