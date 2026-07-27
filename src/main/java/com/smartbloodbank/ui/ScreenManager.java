package com.smartbloodbank.ui;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Controls the single application window and switches what's showing in
 * it — either the Login screen, or the main app (sidebar + whichever
 * screen the user picked). Think of it as the "remote control" for the
 * window's content.
 *
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

    /** Sets up the app window (title, minimum size, taskbar icon) and remembers it for later use. */
    public ScreenManager(Stage stage, AppContext context) {
        this.stage = stage;
        this.context = context;
        stage.setTitle("Smart Blood Bank Management System");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("images/logo.png")));
    }

    /** Switches the window to show the Login screen. */
    public void showLogin() {
        LoginScreen loginScreen = new LoginScreen(this);
        setScene(loginScreen);
    }

    /** Switches the window to show the main app shell, starting on the given screen, for the given signed-in user. */
    public void showApp(ScreenId initialScreen, String username) {
        AppShell appShell = new AppShell(this, context, initialScreen, username);
        setScene(appShell);
    }

    /** Signs the user out by sending them back to the Login screen. */
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
