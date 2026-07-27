package com.smartbloodbank.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The persistent frame shown once a user is logged in: a sidebar for
 * navigation on the left, and whichever Screen is currently active in
 * the center. Swapping screens only replaces the center content, so the
 * sidebar never rebuilds.
 */
public class AppShell extends BorderPane {

    private final ScreenManager screenManager;
    private final AppContext context;
    private final Map<ScreenManager.ScreenId, Button> navButtons = new LinkedHashMap<>();
    private ScreenManager.ScreenId activeId;

    public AppShell(ScreenManager screenManager, AppContext context, ScreenManager.ScreenId initialScreen) {
        this.screenManager = screenManager;
        this.context = context;
        getStyleClass().add("app-shell");
        setLeft(buildSidebar());
        navigateTo(initialScreen);
    }

    public void navigateTo(ScreenManager.ScreenId id) {
        setCenter(createScreen(id));
        setActiveButton(id);
        this.activeId = id;
    }

    /** Rebuilds the current screen so it reflects the latest data. */
    public void refreshCurrent() {
        navigateTo(activeId);
    }

    private Node createScreen(ScreenManager.ScreenId id) {
        return switch (id) {
            case DASHBOARD -> new DashboardScreen(context, this);
            case DONORS -> new DonorManagementScreen(context, this);
            case PATIENTS -> new PatientManagementScreen(context, this);
            case INVENTORY -> new InventoryScreen(context, this);
            case EMERGENCY -> new EmergencyRequestScreen(context, this);
            case REPORTS -> new ReportsScreen(context, this);
            case SETTINGS -> new SettingsScreen(context, this);
        };
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        VBox brandBox = new VBox(2);
        brandBox.getStyleClass().add("brand-box");
        Label brand = new Label("Smart Blood Bank");
        brand.getStyleClass().add("brand-title");
        Label brandSubtitle = new Label("Management System");
        brandSubtitle.getStyleClass().add("brand-subtitle");
        brandBox.getChildren().addAll(brand, brandSubtitle);

        VBox navBox = new VBox(2,
                navButton(ScreenManager.ScreenId.DASHBOARD, "Dashboard"),
                navButton(ScreenManager.ScreenId.DONORS, "Donor Management"),
                navButton(ScreenManager.ScreenId.PATIENTS, "Patient Management"),
                navButton(ScreenManager.ScreenId.INVENTORY, "Blood Inventory"),
                navButton(ScreenManager.ScreenId.EMERGENCY, "Emergency Requests"),
                navButton(ScreenManager.ScreenId.REPORTS, "Reports"),
                navButton(ScreenManager.ScreenId.SETTINGS, "Settings"));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Log Out");
        logoutButton.getStyleClass().add("sidebar-button");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setOnAction(e -> {
            context.saveData();
            screenManager.logout();
        });

        sidebar.getChildren().addAll(brandBox, navBox, spacer, logoutButton);
        return sidebar;
    }

    private Button navButton(ScreenManager.ScreenId id, String label) {
        Button button = new Button(label);
        button.getStyleClass().add("sidebar-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> navigateTo(id));
        navButtons.put(id, button);
        return button;
    }

    private void setActiveButton(ScreenManager.ScreenId id) {
        for (Map.Entry<ScreenManager.ScreenId, Button> entry : navButtons.entrySet()) {
            entry.getValue().getStyleClass().remove("sidebar-button-active");
            if (entry.getKey() == id) {
                entry.getValue().getStyleClass().add("sidebar-button-active");
            }
        }
    }
}
