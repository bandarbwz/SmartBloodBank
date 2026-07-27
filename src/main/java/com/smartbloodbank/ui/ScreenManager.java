package com.smartbloodbank.ui;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Owns the single primary Stage and decides which top-level scene is
 * showing: the Login screen, or the authenticated app shell (sidebar +
 * whichever screen is active).
 */
public class ScreenManager {

    /** Every screen reachable from the sidebar once logged in. */
    public enum ScreenId {
        DASHBOARD, DONORS, PATIENTS, INVENTORY, EMERGENCY, REPORTS, SETTINGS
    }

    private static final double WINDOW_WIDTH = 1200;
    private static final double WINDOW_HEIGHT = 780;

    private final Stage stage;
    private final AppContext context;

    public ScreenManager(Stage stage, AppContext context) {
        this.stage = stage;
        this.context = context;
        stage.setTitle("Smart Blood Bank Management System");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
    }

    public void showLogin() {
        LoginScreen loginScreen = new LoginScreen(this);
        setScene(loginScreen);
    }

    public void showApp(ScreenId initialScreen) {
        AppShell appShell = new AppShell(this, context, initialScreen);
        setScene(appShell);
    }

    public void logout() {
        showLogin();
    }

    private void setScene(javafx.scene.Parent root) {
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
