package com.smartbloodbank.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The persistent frame shown once a user is logged in: a dark sidebar for
 * navigation on the left (with the signed-in user's identity pinned to the
 * bottom), and whichever Screen is currently active in the center.
 * Swapping screens only replaces the center content, so the sidebar never
 * rebuilds.
 */
public class AppShell extends BorderPane {

    /** Icon shown next to each sidebar entry, matching that section's meaning. */
    private static final Map<ScreenManager.ScreenId, Ikon> NAV_ICONS = Map.of(
            ScreenManager.ScreenId.DASHBOARD, FontAwesomeSolid.TACHOMETER_ALT,
            ScreenManager.ScreenId.DONORS, FontAwesomeSolid.HAND_HOLDING_HEART,
            ScreenManager.ScreenId.PATIENTS, FontAwesomeSolid.PROCEDURES,
            ScreenManager.ScreenId.INVENTORY, FontAwesomeSolid.TINT,
            ScreenManager.ScreenId.EMERGENCY, FontAwesomeSolid.AMBULANCE,
            ScreenManager.ScreenId.REPORTS, FontAwesomeSolid.CHART_BAR,
            ScreenManager.ScreenId.SETTINGS, FontAwesomeSolid.COG);

    private final ScreenManager screenManager;
    private final AppContext context;
    private final String username;
    private final Map<ScreenManager.ScreenId, Button> navButtons = new LinkedHashMap<>();
    private final Map<ScreenManager.ScreenId, FontIcon> navIcons = new LinkedHashMap<>();
    private ScreenManager.ScreenId activeId;

    public AppShell(ScreenManager screenManager, AppContext context, ScreenManager.ScreenId initialScreen, String username) {
        this.screenManager = screenManager;
        this.context = context;
        this.username = (username == null || username.isBlank()) ? "admin" : username.trim();
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

        ImageView logo = new ImageView(ImageResources.load(AppShell.class, "images/logo.png"));
        logo.setFitWidth(32);
        logo.setFitHeight(32);
        logo.setPreserveRatio(true);

        Label brand = new Label("Smart Blood Bank");
        brand.getStyleClass().add("brand-title");
        Label brandSubtitle = new Label("MANAGEMENT SYSTEM");
        brandSubtitle.getStyleClass().add("brand-subtitle");
        VBox brandText = new VBox(2, brand, brandSubtitle);

        HBox brandBox = new HBox(10, logo, brandText);
        brandBox.getStyleClass().add("brand-box");
        brandBox.setAlignment(Pos.CENTER_LEFT);

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

        sidebar.getChildren().addAll(brandBox, navBox, spacer, buildUserFooter(), logoutButton);
        return sidebar;
    }

    private Node buildUserFooter() {
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("sidebar-avatar");
        Label initials = new Label(initialsFor(username));
        initials.getStyleClass().add("sidebar-avatar-text");
        avatar.getChildren().add(initials);

        Label name = new Label(username);
        name.getStyleClass().add("sidebar-user-name");
        Label role = new Label("Administrator");
        role.getStyleClass().add("sidebar-user-role");
        VBox textBox = new VBox(1, name, role);

        HBox footer = new HBox(10, avatar, textBox);
        footer.getStyleClass().add("sidebar-footer");
        footer.setAlignment(Pos.CENTER_LEFT);
        return footer;
    }

    private static String initialsFor(String username) {
        String trimmed = username.trim();
        if (trimmed.isEmpty()) {
            return "?";
        }
        String[] parts = trimmed.split("[._\\s-]+");
        if (parts.length >= 2 && !parts[0].isEmpty() && !parts[1].isEmpty()) {
            return ("" + Character.toUpperCase(parts[0].charAt(0)) + Character.toUpperCase(parts[1].charAt(0)));
        }
        return trimmed.substring(0, Math.min(2, trimmed.length())).toUpperCase();
    }

    private Button navButton(ScreenManager.ScreenId id, String label) {
        FontIcon icon = new FontIcon(NAV_ICONS.get(id));
        icon.setIconSize(15);
        icon.getStyleClass().add("nav-icon");
        navIcons.put(id, icon);

        Button button = new Button(label);
        button.setGraphic(icon);
        button.setGraphicTextGap(12);
        button.getStyleClass().add("sidebar-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> navigateTo(id));
        navButtons.put(id, button);
        return button;
    }

    private void setActiveButton(ScreenManager.ScreenId id) {
        for (Map.Entry<ScreenManager.ScreenId, Button> entry : navButtons.entrySet()) {
            boolean active = entry.getKey() == id;
            entry.getValue().getStyleClass().remove("sidebar-button-active");
            if (active) {
                entry.getValue().getStyleClass().add("sidebar-button-active");
            }
            FontIcon icon = navIcons.get(entry.getKey());
            icon.getStyleClass().remove("nav-icon-active");
            if (active) {
                icon.getStyleClass().add("nav-icon-active");
            }
        }
    }
}
