package com.smartbloodbank.ui;

import com.smartbloodbank.model.BloodType;
import com.smartbloodbank.model.Donor;
import com.smartbloodbank.model.Patient;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

/** Read-only snapshot of donors, patients, stock levels and active alerts. */
public class ReportsScreen extends Screen {

    public ReportsScreen(AppContext context, AppShell appShell) {
        super(context, appShell);
    }

    @Override
    protected String getTitle() {
        return "Reports";
    }

    @Override
    protected String getSubtitle() {
        return "Snapshot of donors, patients, stock and alerts.";
    }

    @Override
    protected Node buildContent() {
        context.getInventoryManager().updateExpiredBags();

        VBox root = new VBox(20);
        root.getChildren().addAll(buildSummaryRow(), buildStockReportCard(), buildAlertsReportCard());
        return root;
    }

    private Node buildSummaryRow() {
        List<Donor> donors = context.getBloodBank().getAllDonors();
        List<Patient> patients = context.getBloodBank().getAllPatients();
        long fulfilled = patients.stream().filter(Patient::isFulfilled).count();
        int totalDonations = donors.stream().mapToInt(Donor::getTotalDonations).sum();

        HBox row = new HBox(16,
                statTile("TOTAL DONATIONS RECORDED", String.valueOf(totalDonations), "accent-red"),
                statTile("PATIENTS FULFILLED", fulfilled + " / " + patients.size(), "accent-green"),
                statTile("BAGS IN STOCK", String.valueOf(context.getBloodBank().getAllBloodBags().size()), "accent-blue"));
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
        tile.setMaxWidth(Double.MAX_VALUE);
        return tile;
    }

    private Node buildStockReportCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label title = new Label("Available Stock by Blood Type");
        title.getStyleClass().add("card-title");
        card.getChildren().add(title);

        Map<BloodType, Integer> summary = context.getBloodBank().getStockSummary();
        for (BloodType type : BloodType.values()) {
            Label row = new Label(type.getLabel() + "  —  " + summary.get(type) + " unit(s) available");
            row.getStyleClass().add("page-subtitle");
            card.getChildren().add(row);
        }
        return card;
    }

    private Node buildAlertsReportCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label title = new Label("Active Alerts");
        title.getStyleClass().add("card-title");
        card.getChildren().add(title);

        List<String> alerts = context.getInventoryManager().getAllAlerts();
        if (alerts.isEmpty()) {
            Label none = new Label("No active alerts.");
            none.getStyleClass().add("empty-state");
            card.getChildren().add(none);
        } else {
            for (String alert : alerts) {
                Label alertLabel = new Label(alert);
                alertLabel.setWrapText(true);
                alertLabel.setMaxWidth(Double.MAX_VALUE);
                alertLabel.getStyleClass().addAll("alert-banner",
                        alert.startsWith("LOW STOCK") ? "alert-banner-danger" : "alert-banner-warning");
                card.getChildren().add(alertLabel);
            }
        }
        return card;
    }
}
