package com.ecommerce.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LandingWindow extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Welcome label
        Label welcomeLabel = new Label("WELCOME TO CØDE LØCK");
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        welcomeLabel.setStyle("-fx-text-fill: #1E3A8A;");
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.GRAY);
        welcomeLabel.setEffect(shadow);

        // Tagline
        Label tagline = new Label("Your one‑stop shop for quality tech & lifestyle");
        tagline.setFont(Font.font("Segoe UI", 16));
        tagline.setStyle("-fx-text-fill: #555555;");

        // Shop Now button
        Button shopNowBtn = new Button("SHOP NOW");
        shopNowBtn.getStyleClass().add("shop-now-btn");
        shopNowBtn.setPrefWidth(180);
        shopNowBtn.setPrefHeight(50);
        shopNowBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        shopNowBtn.setOnAction(e -> openCatalog());

        // Layout container
        VBox root = new VBox(25, welcomeLabel, tagline, shopNowBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f9f9f9, #eaeaea);");

        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.setTitle("CØDE LØCK – Landing");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void openCatalog() {
        MainApp mainApp = new MainApp();
        try {
            mainApp.start(primaryStage); // reuse the same stage
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}