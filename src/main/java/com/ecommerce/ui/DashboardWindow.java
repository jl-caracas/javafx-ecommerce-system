package com.ecommerce.ui;

import com.ecommerce.db.OrderDAO;
import com.ecommerce.db.ProductDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class DashboardWindow {

    private Stage stage;
    private ProductDAO productDAO;
    private OrderDAO orderDAO;

    private static final String CARD_BG = "-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; -fx-border-color: #e8e8e8; -fx-border-radius: 12;";

    public DashboardWindow() {
        productDAO = new ProductDAO();
        orderDAO = new OrderDAO();
        stage = new Stage();
        stage.setTitle("CØDE LØCK - Dashboard");
        buildUI();
    }

    private void buildUI() {
        int totalProducts = productDAO.getAllProducts().size();
        int pendingOrders = orderDAO.getOrdersByStatus("PENDING").size();
        int completedOrders = orderDAO.getOrdersByStatus("COMPLETED").size();
        double totalRevenue = orderDAO.getAllOrders().stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .mapToDouble(o -> o.getTotal())
                .sum();

        // ── Header ──
        Label headerLabel = new Label("📊 DASHBOARD");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        headerLabel.setTextFill(Color.web("#1E3A8A"));
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(30, 58, 138, 0.3));
        headerLabel.setEffect(shadow);

        // ── Stats Cards ──
        VBox productsCard   = createStatCard("📦", "Total Products", String.valueOf(totalProducts), "#3B82F6");
        VBox pendingCard    = createStatCard("⏳", "Pending Orders", String.valueOf(pendingOrders), "#F59E0B");
        VBox completedCard  = createStatCard("✅", "Completed Orders", String.valueOf(completedOrders), "#10B981");
        VBox revenueCard    = createStatCard("💰", "Total Revenue", String.format("₱%.2f", totalRevenue), "#8B5CF6");

        HBox statsRow = new HBox(20, productsCard, pendingCard, completedCard, revenueCard);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(20, 0, 20, 0));

        // ── Pie Chart ──
        PieChart pieChart = new PieChart();
        pieChart.getData().add(new PieChart.Data("Pending", Math.max(pendingOrders, 1)));
        pieChart.getData().add(new PieChart.Data("Completed", Math.max(completedOrders, 1)));
        pieChart.setTitle("Order Status Distribution");
        pieChart.setPrefWidth(350);
        pieChart.setPrefHeight(280);
        pieChart.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15;");

        // Apply modern colors to pie slices
        pieChart.getData().get(0).getNode().setStyle("-fx-pie-color: #F59E0B;");
        pieChart.getData().get(1).getNode().setStyle("-fx-pie-color: #10B981;");

        // Legend customizations
        pieChart.setLegendSide(Side.BOTTOM);
        pieChart.setLabelsVisible(true);

        // ── Summary Text ──
        Label summaryLabel = new Label();
        if (pendingOrders > 0) {
            summaryLabel.setText("⚠️ You have " + pendingOrders + " pending order(s) waiting for processing.");
            summaryLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: #FEF3C7; -fx-background-radius: 8; -fx-padding: 12 20;");
        } else {
            summaryLabel.setText("✅ All orders are completed. Great job!");
            summaryLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: #D1FAE5; -fx-background-radius: 8; -fx-padding: 12 20;");
        }

        VBox chartBox = new VBox(10, pieChart, summaryLabel);
        chartBox.setAlignment(Pos.CENTER);
        chartBox.setStyle(CARD_BG);
        chartBox.setMaxWidth(500);

        // ── Main Layout ──
        VBox root = new VBox(10, headerLabel, statsRow, chartBox);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f1f5f9;");

        Scene scene = new Scene(root, 780, 580);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {}
        stage.setScene(scene);
    }

    private VBox createStatCard(String emoji, String title, String value, String colorHex) {
        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font("Segoe UI", 32));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        titleLabel.setTextFill(Color.web("#64748B"));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        valueLabel.setTextFill(Color.web(colorHex));

        VBox card = new VBox(8, emojiLabel, valueLabel, titleLabel);
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(160);
        card.setPrefWidth(160);
        card.setPrefHeight(130);
        card.setStyle(CARD_BG);
        card.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.08)));

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(CARD_BG + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);"));
        card.setOnMouseExited(e -> card.setStyle(CARD_BG + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);")); 

        return card;
    }

    public void show() {
        stage.centerOnScreen();
        stage.show();
    }
}