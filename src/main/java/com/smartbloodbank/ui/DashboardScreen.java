package com.smartbloodbank.ui;

import com.smartbloodbank.model.BloodType;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

/** Landing screen: at-a-glance stats, active alerts, and stock by blood type. */
public class DashboardScreen extends Screen {

    public DashboardScreen(AppContext context, AppShell appShell) {
        super(context, appShell);
    }

    @Override
    protected String getTitle() {
        return "Dashboard";
    }

    @Override
    protected String getSubtitle() {
        return "Live overview of stock, donors, patients and alerts.";
    }

    @Override
    protected Node buildContent() {
        context.getInventoryManager().updateExpiredBags();

        VBox root = new VBox(20);
        root.getChildren().add(buildStatRow());

        List<String> alerts = context.getInventoryManager().getAllAlerts();
        if (!alerts.isEmpty()) {
            root.getChildren().add(buildAlertsCard(alerts));
        }

        root.getChildren().add(buildStockCard());
        return root;
    }

    private Node buildStatRow() {
        int donors = context.getBloodBank().getAllDonors().size();
        int patients = context.getBloodBank().getAllPatients().size();
        int availableUnits = context.getBloodBank().getAvailableBloodBags().size();
        int pendingRequests = context.getEmergencyRequest().getPendingCount();

        HBox row = new HBox(16,
                statTile("TOTAL DONORS", String.valueOf(donors), "accent-red"),
                statTile("TOTAL PATIENTS", String.valueOf(patients), "accent-blue"),
                statTile("AVAILABLE UNITS", String.valueOf(availableUnits), "accent-green"),
                statTile("PENDING REQUESTS", String.valueOf(pendingRequests), "accent-amber"));
        for (Node tile : row.getChildren()) {
            HBox.setHgrow(tile, Priority.ALWAYS);
        }
        return row;
    }

    private Node statTile(String label, String value, String accentClass) {
        Region accent = new Region();
        accent.getStyleClass().addAll("stat-tile-accent", accentClass);
        accent.setPrefHeight(34);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-tile-value");
        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-tile-label");
        VBox textBox = new VBox(4, valueLabel, textLabel);

        HBox tile = new HBox(12, accent, textBox);
        tile.getStyleClass().add("stat-tile");
        tile.setAlignment(Pos.CENTER_LEFT);
        tile.setMaxWidth(Double.MAX_VALUE);
        return tile;
    }

    private Node buildAlertsCard(List<String> alerts) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label title = new Label("Alerts");
        title.getStyleClass().add("card-title");
        card.getChildren().add(title);

        for (String alert : alerts) {
            Label alertLabel = new Label(alert);
            alertLabel.setWrapText(true);
            alertLabel.setMaxWidth(Double.MAX_VALUE);
            alertLabel.getStyleClass().addAll("alert-banner",
                    alert.startsWith("LOW STOCK") ? "alert-banner-danger" : "alert-banner-warning");
            card.getChildren().add(alertLabel);
        }
        return card;
    }

    private Node buildStockCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        Label title = new Label("Stock by Blood Type");
        title.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(28);
        grid.setVgap(14);

        Map<BloodType, Integer> summary = context.getBloodBank().getStockSummary();
        int col = 0;
        int row = 0;
        for (BloodType type : BloodType.values()) {
            VBox cell = new VBox(2);
            Label typeLabel = new Label(type.getLabel());
            typeLabel.getStyleClass().add("form-label");
            Label countLabel = new Label(summary.get(type) + " units");
            countLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1B1F27;");
            cell.getChildren().addAll(typeLabel, countLabel);
            grid.add(cell, col, row);
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }

        card.getChildren().addAll(title, grid);
        return card;
    }
}
