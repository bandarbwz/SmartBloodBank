package com.smartbloodbank.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * First screen shown when the app launches. Not a real authentication
 * system — just a gate in front of the app shell, using a single demo
 * credential, since staff accounts are out of scope for this project.
 */
public class LoginScreen extends StackPane {

    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "admin123";

    public LoginScreen(ScreenManager screenManager) {
        getStyleClass().add("login-root");

        Label title = new Label("Smart Blood Bank");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label("Sign in to manage donors, patients and blood stock.");
        subtitle.getStyleClass().add("login-subtitle");

        Label userLabel = new Label("USERNAME");
        userLabel.getStyleClass().add("form-label");
        TextField userField = new TextField();
        userField.setPromptText("admin");

        Label passLabel = new Label("PASSWORD");
        passLabel.getStyleClass().add("form-label");
        PasswordField passField = new PasswordField();
        passField.setPromptText("••••••••");

        Label error = new Label("Invalid username or password.");
        error.getStyleClass().add("login-error");
        error.setVisible(false);
        error.managedProperty().bind(error.visibleProperty());

        Button loginButton = new Button("Sign In");
        loginButton.getStyleClass().add("button-primary");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        Runnable attemptLogin = () -> {
            boolean validUser = VALID_USERNAME.equals(userField.getText().trim());
            boolean validPass = VALID_PASSWORD.equals(passField.getText());
            if (validUser && validPass) {
                screenManager.showApp(ScreenManager.ScreenId.DASHBOARD);
            } else {
                error.setVisible(true);
            }
        };
        loginButton.setOnAction(e -> attemptLogin.run());
        passField.setOnAction(e -> attemptLogin.run());

        Label hint = new Label("Demo credentials — admin / admin123");
        hint.getStyleClass().add("login-subtitle");

        VBox form = new VBox(10, userLabel, userField, passLabel, passField, error, loginButton, hint);
        form.setMaxWidth(300);

        Region spacer = new Region();
        spacer.setPrefHeight(16);

        VBox card = new VBox(4, title, subtitle, spacer, form);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(380);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        getChildren().add(card);
    }
}
