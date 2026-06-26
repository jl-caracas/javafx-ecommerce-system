package com.ecommerce.ui;

import com.ecommerce.db.UserDAO;
import com.ecommerce.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginWindow {

    private User loggedInUser = null;

    public User showAndWait(Stage owner) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.setTitle("Login");
        stage.setResizable(false);

        Label titleLabel = new Label("Log in to continue");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField     usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginBtn  = new Button("Login");
        Button cancelBtn = new Button("Cancel");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        // ── Validation: enable Login only when both fields are filled ──
        loginBtn.setDisable(true);
        Runnable validate = () -> {
            boolean ok = !usernameField.getText().trim().isEmpty()
                      && !passwordField.getText().isEmpty();
            loginBtn.setDisable(!ok);
        };
        usernameField.textProperty().addListener((obs, old, val) -> validate.run());
        passwordField.textProperty().addListener((obs, old, val) -> validate.run());

        // ── FIX: pressing Enter anywhere in the form triggers Login ──
        //    setDefaultButton(true) makes JavaFX fire this button when the
        //    user presses Enter, no matter which control currently has focus.
        //    It respects the disabled state, so it won't fire while empty.
        loginBtn.setDefaultButton(true);

        Hyperlink registerLink = new Hyperlink("Don't have an account? Register now");

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            UserDAO userDAO = new UserDAO();
            User user = userDAO.login(username, password);
            if (user != null) {
                loggedInUser = user;
                stage.close();
            } else {
                messageLabel.setText("Invalid username or password");
                passwordField.clear();           // clear password on failed attempt
                passwordField.requestFocus();
            }
        });

        cancelBtn.setOnAction(e -> stage.close());

        registerLink.setOnAction(e -> {
            stage.close();
            RegisterWindow reg = new RegisterWindow();
            // Pass primary stage owner so register window has a valid parent.
            // We use owner (the main app stage) instead of the already-closed
            // login stage to avoid JavaFX owner-chain issues.
            Stage parentStage = (owner != null) ? owner : stage;
            User newUser = reg.showAndWait(parentStage);
            if (newUser != null) {
                loggedInUser = newUser;
            }
        });

        VBox layout = new VBox(10,
                titleLabel, usernameField, passwordField,
                loginBtn, cancelBtn, registerLink, messageLabel);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Scene scene = new Scene(layout, 350, 320);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.setOnShown(ev -> usernameField.requestFocus());

        stage.showAndWait();
        return loggedInUser;
    }
}
