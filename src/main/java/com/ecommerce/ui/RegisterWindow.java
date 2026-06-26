package com.ecommerce.ui;

import com.ecommerce.db.UserDAO;
import com.ecommerce.model.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterWindow {

    private User registeredUser = null;

    public User showAndWait(Stage owner) {
        Stage stage = new Stage();
        // Only set owner when it's a valid, showing stage.
        // Passing a closed/null stage as initOwner crashes on some platforms.
        if (owner != null && owner.isShowing()) {
            stage.initOwner(owner);
        }
        stage.setTitle("Register");
        stage.setResizable(false);

        Label titleLabel = new Label("Create a new account");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField     usernameField = new TextField();
        usernameField.setPromptText("Username *");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password *");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name *");

        TextField addressField = new TextField();
        addressField.setPromptText("Address");

        TextField gcashField = new TextField();
        gcashField.setPromptText("GCash Number");

        Button registerBtn = new Button("Register");
        Button cancelBtn   = new Button("Cancel");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        // ── Validation ──
        registerBtn.setDisable(true);
        Runnable validate = () -> {
            boolean ok = !usernameField.getText().trim().isEmpty()
                      && !passwordField.getText().isEmpty()
                      && !fullNameField.getText().trim().isEmpty();
            registerBtn.setDisable(!ok);
        };
        usernameField.textProperty().addListener((obs, old, val) -> validate.run());
        passwordField.textProperty().addListener((obs, old, val) -> validate.run());
        fullNameField.textProperty().addListener((obs, old, val) -> validate.run());

        // ── FIX: Enter key triggers Register ──
        registerBtn.setDefaultButton(true);

        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String fullName = fullNameField.getText().trim();
            String address  = addressField.getText().trim();
            String gcash    = gcashField.getText().trim();

            User newUser = new User(0, username, password, fullName, address, gcash);
            UserDAO userDAO = new UserDAO();
            boolean success = userDAO.registerUser(newUser);
            if (success) {
                User logged = userDAO.login(username, password);
                if (logged != null) {
                    registeredUser = logged;
                    stage.close();
                } else {
                    messageLabel.setText("Auto-login failed, but account was created.");
                }
            } else {
                messageLabel.setText("Username already exists or an error occurred.");
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        VBox layout = new VBox(10,
                titleLabel,
                usernameField, passwordField, fullNameField,
                addressField, gcashField,
                registerBtn, cancelBtn, messageLabel);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        // ─────────────────────────────────────────────────────────────────
        // FIX: The original code called stage.sizeToScene() before showing,
        // then applied a ±1px width hack inside setOnShown to force a redraw.
        //
        // Root cause: In JavaFX, sizeToScene() is unreliable before the
        // scene's CSS has been applied (which only happens after show()).
        // The ±1px hack was trying to trigger a layout pass, but it caused
        // the window to flash or appear blank until manually resized.
        //
        // Proper fix:
        //   1. Set an explicit scene size so the window opens at the right size.
        //   2. Call sizeToScene() + centerOnScreen() INSIDE Platform.runLater
        //      inside setOnShown, so it runs AFTER JavaFX has done its first
        //      full layout and CSS pass. No width hacks needed.
        // ─────────────────────────────────────────────────────────────────
        Scene scene = new Scene(layout, 350, 430);
        stage.setScene(scene);
        stage.centerOnScreen();

        stage.setOnShown(ev -> Platform.runLater(() -> {
            // At this point CSS is applied and layout is complete.
            stage.sizeToScene();       // resize to actual preferred size
            stage.centerOnScreen();    // re-center after potential resize
            usernameField.requestFocus();
        }));

        stage.showAndWait();
        return registeredUser;
    }
}
