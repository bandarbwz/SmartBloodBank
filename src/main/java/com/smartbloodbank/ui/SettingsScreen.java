package com.smartbloodbank.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Manual data persistence controls, real operating configuration, and
 * basic system information. Deliberately does not include a staff
 * profile, facility info, notification toggles or user-management table
 * — none of those have a backing entity in the domain model, and adding
 * editable-looking controls that don't persist anything would be
 * misleading rather than helpful.
 */
public class SettingsScreen extends Screen {

    public SettingsScreen(AppContext context, AppShell appShell) {
        super(context, appShell);
    }

    @Override
    protected String getTitle() {
        return "Settings";
    }

    @Override
    protected Node buildContent() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col, cloneConstraint(col));

        grid.add(buildDataCard(), 0, 0);
        grid.add(buildAboutCard(), 1, 0);

        Node thresholds = buildThresholdsCard();
        GridPane.setColumnSpan(thresholds, 2);
        grid.add(thresholds, 0, 1);

        return grid;
    }

    private ColumnConstraints cloneConstraint(ColumnConstraints source) {
        ColumnConstraints copy = new ColumnConstraints();
        copy.setPercentWidth(source.getPercentWidth());
        return copy;
    }

    private Node buildDataCard() {
        Label title = new Label("Data Persistence");
        title.getStyleClass().add("card-title");
        Label description = new Label(
                "Donors, patients and blood bags are saved to the data/ folder next to the application.");
        description.getStyleClass().add("card-subtitle");
        description.setWrapText(true);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("card-subtitle");

        Button saveButton = new Button("Save Now");
        saveButton.getStyleClass().add("button-primary");
        saveButton.setOnAction(e -> {
            context.saveData();
            statusLabel.setText("Data saved successfully.");
        });

        VBox card = new VBox(14, title, description, saveButton, statusLabel);
        card.getStyleClass().add("card");
        return card;
    }

    private Node buildAboutCard() {
        Label title = new Label("About");
        title.getStyleClass().add("card-title");
        Label version = new Label("Smart Blood Bank Management System — v1.0.0");
        version.getStyleClass().add("card-subtitle");
        Label credentials = new Label("Demo login: admin / admin123");
        credentials.getStyleClass().add("card-subtitle");
        VBox card = new VBox(8, title, version, credentials);
        card.getStyleClass().add("card");
        return card;
    }

    private Node buildThresholdsCard() {
        Label title = new Label("Alert Thresholds");
        title.getStyleClass().add("card-title");
        Label subtitle = new Label("Controls when Low Stock and Expiring Soon alerts trigger (read-only)");
        subtitle.getStyleClass().add("card-subtitle");

        HBox fields = new HBox(32,
                thresholdField("LOW STOCK THRESHOLD",
                        context.getInventoryManager().getLowStockThreshold() + " units"),
                thresholdField("EXPIRY WARNING WINDOW",
                        context.getInventoryManager().getExpiryWarningDays() + " days"));

        VBox card = new VBox(14, title, subtitle, fields);
        card.getStyleClass().add("card");
        return card;
    }

    private Node thresholdField(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("detail-field-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("detail-field-value");
        valueNode.setStyle("-fx-font-size: 18px;");
        return new VBox(4, labelNode, valueNode);
    }
}
