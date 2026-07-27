package com.smartbloodbank.ui;

import com.smartbloodbank.model.BloodBag;
import com.smartbloodbank.model.BloodType;
import com.smartbloodbank.service.InventoryManager;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

/**
 * The first screen shown after logging in. It's a read-only overview:
 * four summary numbers (total donors, total patients, available units,
 * active alerts), a row of cards showing stock for each of the 8 blood
 * types (colored normal/low/critical), and two lists — blood types
 * running low, and blood bags about to expire. There are no buttons or
 * actions here, just information. All the numbers come from
 * BloodBank (donor/patient/stock counts) and InventoryManager
 * (low-stock and near-expiry alerts).
 *
 * Landing screen: at-a-glance KPIs, stock by blood type, and active alerts — all live from the service layer.
 */
public class DashboardScreen extends Screen {

    /** Builds the dashboard screen for the given shared app data. */
    public DashboardScreen(AppContext context, AppShell appShell) {
        super(context, appShell);
    }

    @Override
    protected String getTitle() {
        return "Dashboard";
    }

    @Override
    protected Node buildContent() {
        context.getInventoryManager().updateExpiredBags();

        VBox root = new VBox(28);
        root.getChildren().add(buildKpiRow());
        root.getChildren().add(buildBloodTypeSection());
        root.getChildren().add(buildAlertsRow());
        return root;
    }

    private Node buildKpiRow() {
        int donors = context.getBloodBank().getAllDonors().size();
        int patients = context.getBloodBank().getAllPatients().size();
        int availableUnits = context.getBloodBank().getAvailableBloodBags().size();
        int alertCount = context.getInventoryManager().getAllAlerts().size();

        HBox row = new HBox(16,
                kpiTile("Total Donors", String.valueOf(donors), "Registered donors", false),
                kpiTile("Total Patients", String.valueOf(patients), "Active patient records", false),
                kpiTile("Available Units", String.valueOf(availableUnits), "Across " + BloodType.values().length + " blood types", false),
                kpiTile("Active Alerts", String.valueOf(alertCount), "Low stock + expiring soon", true));
        for (Node tile : row.getChildren()) {
            HBox.setHgrow(tile, Priority.ALWAYS);
        }
        return row;
    }

    private Node kpiTile(String label, String value, String subtitle, boolean alert) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("kpi-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add(alert ? "kpi-value-alert" : "kpi-value");
        Label subtitleNode = new Label(subtitle);
        subtitleNode.getStyleClass().add("kpi-subtitle");

        VBox tile = new VBox(4, labelNode, valueNode, subtitleNode);
        tile.getStyleClass().add(alert ? "kpi-card-alert" : "kpi-card");
        tile.setMaxWidth(Double.MAX_VALUE);
        return tile;
    }

    private Node buildBloodTypeSection() {
        Label sectionTitle = new Label("Blood Type Availability");
        sectionTitle.getStyleClass().add("card-title");

        InventoryManager inventoryManager = context.getInventoryManager();
        List<BloodType> lowStockTypes = inventoryManager.getLowStockTypes();
        Map<BloodType, Integer> summary = context.getBloodBank().getStockSummary();

        HBox row = new HBox(12);
        for (BloodType type : BloodType.values()) {
            int units = summary.get(type);
            String tier = units == 0 ? "critical" : (lowStockTypes.contains(type) ? "low" : "normal");
            Node card = bloodTypeCard(type, units, tier);
            HBox.setHgrow(card, Priority.ALWAYS);
            row.getChildren().add(card);
        }

        VBox section = new VBox(12, sectionTitle, row);
        return section;
    }

    private Node bloodTypeCard(BloodType type, int units, String tier) {
        Label typeLabel = new Label(type.getLabel());
        typeLabel.getStyleClass().add("blood-type-label");

        Label unitsLabel = new Label(String.valueOf(units));
        unitsLabel.getStyleClass().addAll("blood-type-units", "blood-type-units-" + tier);

        Label captionLabel = new Label("units");
        captionLabel.getStyleClass().add("blood-type-units-caption");

        Label tag = new Label(switch (tier) {
            case "critical" -> "CRITICAL";
            case "low" -> "LOW";
            default -> "OK";
        });
        tag.getStyleClass().addAll("blood-type-tag", "blood-type-tag-" + tier);

        VBox card = new VBox(4, typeLabel, unitsLabel, captionLabel, tag);
        card.getStyleClass().addAll("blood-type-card", "blood-type-card-" + tier);
        card.setAlignment(Pos.CENTER);
        return card;
    }

    private Node buildAlertsRow() {
        HBox row = new HBox(20, buildLowStockCard(), buildExpiringSoonCard());
        HBox.setHgrow(row.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
        return row;
    }

    private Node buildLowStockCard() {
        Label title = new Label("Low Stock Alerts");
        title.getStyleClass().add("card-title");
        Label subtitle = new Label("Blood types below the safety threshold");
        subtitle.getStyleClass().add("card-subtitle");

        VBox card = new VBox(2, title, subtitle);
        card.getStyleClass().add("card");

        List<BloodType> lowStockTypes = context.getInventoryManager().getLowStockTypes();
        Map<BloodType, Integer> summary = context.getBloodBank().getStockSummary();
        if (lowStockTypes.isEmpty()) {
            Label none = new Label("All blood types are adequately stocked.");
            none.getStyleClass().add("empty-state");
            VBox.setMargin(none, new javafx.geometry.Insets(12, 0, 0, 0));
            card.getChildren().add(none);
        } else {
            for (BloodType type : lowStockTypes) {
                int units = summary.get(type);
                boolean critical = units == 0;
                card.getChildren().add(lowStockRow(type, units, critical));
            }
        }
        return card;
    }

    private Node lowStockRow(BloodType type, int units, boolean critical) {
        Region dot = new Region();
        dot.getStyleClass().addAll("alert-dot", critical ? "alert-dot-danger" : "alert-dot-warning");

        Label typeLabel = new Label(type.getLabel());
        typeLabel.getStyleClass().add("data-row-emphasis");
        HBox left = new HBox(10, dot, typeLabel);
        left.setAlignment(Pos.CENTER_LEFT);

        Label unitsLabel = new Label(units + " units left");
        unitsLabel.getStyleClass().add("data-row-text");

        Label severity = new Label(critical ? "Critical" : "Low");
        severity.getStyleClass().addAll("status-pill", critical ? "status-pill-expired" : "status-pill-reserved");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, left, spacer, unitsLabel, severity);
        row.getStyleClass().add("alert-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node buildExpiringSoonCard() {
        Label title = new Label("Expiring Soon");
        title.getStyleClass().add("card-title");
        Label subtitle = new Label("Available units nearing their expiry date");
        subtitle.getStyleClass().add("card-subtitle");

        VBox card = new VBox(2, title, subtitle);
        card.getStyleClass().add("card");

        List<BloodBag> nearingExpiry = context.getInventoryManager().getBagsNearingExpiry();
        if (nearingExpiry.isEmpty()) {
            Label none = new Label("No units expiring soon.");
            none.getStyleClass().add("empty-state");
            VBox.setMargin(none, new javafx.geometry.Insets(12, 0, 0, 0));
            card.getChildren().add(none);
        } else {
            for (BloodBag bag : nearingExpiry) {
                card.getChildren().add(expiringRow(bag));
            }
        }
        return card;
    }

    private Node expiringRow(BloodBag bag) {
        Label idType = new Label(bag.getBagId() + " · " + bag.getBloodType().getLabel());
        idType.getStyleClass().add("data-row-emphasis");
        Label expiry = new Label("Expiry " + bag.getExpirationDate());
        expiry.getStyleClass().add("kpi-subtitle");
        VBox left = new VBox(2, idType, expiry);

        long daysLeft = bag.daysUntilExpiry();
        String label = daysLeft <= 0 ? "Expires today" : "Expires in " + daysLeft + "d";
        Label tag = new Label(label);
        tag.getStyleClass().addAll("status-pill", "status-pill-expired");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, left, spacer, tag);
        row.getStyleClass().add("alert-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
