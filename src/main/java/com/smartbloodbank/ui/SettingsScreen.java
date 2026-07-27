package com.smartbloodbank.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Manual data persistence controls and basic system information. */
public class SettingsScreen extends Screen {

    public SettingsScreen(AppContext context, AppShell appShell) {
        super(context, appShell);
    }

    @Override
    protected String getTitle() {
        return "Settings";
    }

    @Override
    protected String getSubtitle() {
        return "Manage data persistence and view system information.";
    }

    @Override
    protected Node buildContent() {
        VBox root = new VBox(20);
        root.getChildren().addAll(buildDataCard(), buildAboutCard());
        return root;
    }

    private Node buildDataCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        Label title = new Label("Data Persistence");
        title.getStyleClass().add("card-title");
        Label description = new Label(
                "Donors, patients and blood bags are saved to the data/ folder next to the application.");
        description.getStyleClass().add("page-subtitle");
        description.setWrapText(true);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("page-subtitle");

        Button saveButton = new Button("Save Now");
        saveButton.getStyleClass().add("button-primary");
        saveButton.setOnAction(e -> {
            context.saveData();
            statusLabel.setText("Data saved successfully.");
        });

        HBox buttons = new HBox(10, saveButton);

        card.getChildren().addAll(title, description, buttons, statusLabel);
        return card;
    }

    private Node buildAboutCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        Label title = new Label("About");
        title.getStyleClass().add("card-title");
        Label version = new Label("Smart Blood Bank Management System — v1.0.0");
        version.getStyleClass().add("page-subtitle");
        Label credentials = new Label("Demo login: admin / admin123");
        credentials.getStyleClass().add("page-subtitle");
        card.getChildren().addAll(title, version, credentials);
        return card;
    }
}
