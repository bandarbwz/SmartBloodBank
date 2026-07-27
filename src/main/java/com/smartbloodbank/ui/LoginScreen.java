package com.smartbloodbank.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * First screen shown when the app launches. Not a real authentication
 * system — just a gate in front of the app shell, using a single demo
 * credential, since staff accounts are out of scope for this project.
 * Laid out as a split panel to match the design reference: a brand panel
 * on the left, the sign-in form on the right.
 */
public class LoginScreen extends HBox {

    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "admin123";

    public LoginScreen(ScreenManager screenManager) {
        getStyleClass().add("login-root");

        VBox brandPanel = buildBrandPanel();
        HBox.setHgrow(brandPanel, Priority.ALWAYS);

        VBox formPanel = buildFormPanel(screenManager);
        HBox.setHgrow(formPanel, Priority.ALWAYS);

        brandPanel.setPrefWidth(420);
        brandPanel.setMinWidth(360);
        formPanel.setPrefWidth(580);

        getChildren().addAll(brandPanel, formPanel);
    }

    private VBox buildBrandPanel() {
        ImageView logoView = new ImageView(ImageResources.load(LoginScreen.class, "images/logo.png"));
        logoView.setFitWidth(80);
        logoView.setFitHeight(80);
        logoView.setPreserveRatio(true);

        Text smartText = new Text("Smart ");
        smartText.getStyleClass().add("login-brand-title");
        Text bloodText = new Text("Blood");
        bloodText.getStyleClass().addAll("login-brand-title", "login-brand-title-accent");
        Text bankText = new Text(" Bank");
        bankText.getStyleClass().add("login-brand-title");
        TextFlow titleFlow = new TextFlow(smartText, bloodText, bankText);
        titleFlow.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label subtitle = new Label("MANAGEMENT SYSTEM");
        subtitle.getStyleClass().add("login-brand-subtitle");

        Label tagline = new Label("Real-time donor, patient and inventory management for hospital blood banks.");
        tagline.getStyleClass().add("login-brand-tagline");
        tagline.setMaxWidth(280);

        VBox titleBox = new VBox(6, titleFlow, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        VBox logoAndTitle = new VBox(8, logoView, titleBox);
        logoAndTitle.setAlignment(Pos.CENTER);

        VBox panel = new VBox(24, logoAndTitle, tagline);
        panel.getStyleClass().add("login-brand-panel");
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(48));
        return panel;
    }

    private VBox buildFormPanel(ScreenManager screenManager) {
        Label title = new Label("Sign in");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label("Authorized personnel only");
        subtitle.getStyleClass().add("login-subtitle");

        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("form-label");
        TextField userField = new TextField();
        userField.setPromptText("e.g. admin");

        Label passLabel = new Label("Password");
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
        loginButton.setAlignment(Pos.CENTER);

        Runnable attemptLogin = () -> {
            String enteredUsername = userField.getText().trim();
            boolean validUser = VALID_USERNAME.equals(enteredUsername);
            boolean validPass = VALID_PASSWORD.equals(passField.getText());
            if (validUser && validPass) {
                screenManager.showApp(ScreenManager.ScreenId.DASHBOARD, enteredUsername);
            } else {
                error.setVisible(true);
            }
        };
        loginButton.setOnAction(e -> attemptLogin.run());
        passField.setOnAction(e -> attemptLogin.run());

        Label hint = new Label("Access is limited to verified hospital staff and administrators. (Demo: admin / admin123)");
        hint.getStyleClass().add("login-hint");
        hint.setMaxWidth(320);

        VBox form = new VBox(18,
                labeledField(userLabel, userField),
                labeledField(passLabel, passField),
                error,
                loginButton);
        form.setMaxWidth(320);

        Region topSpacer = new Region();
        topSpacer.setPrefHeight(32);

        VBox formContent = new VBox(4, title, subtitle, topSpacer, form, spacer(16), hint);
        formContent.setAlignment(Pos.CENTER_LEFT);
        formContent.setMaxWidth(320);

        VBox panel = new VBox(formContent);
        panel.getStyleClass().add("login-form-panel");
        panel.setAlignment(Pos.CENTER);
        return panel;
    }

    private VBox labeledField(Label label, javafx.scene.control.Control field) {
        return new VBox(6, label, field);
    }

    private Region spacer(double height) {
        Region region = new Region();
        region.setPrefHeight(height);
        return region;
    }
}
