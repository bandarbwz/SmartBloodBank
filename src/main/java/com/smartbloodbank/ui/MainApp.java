package com.smartbloodbank.ui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The starting point of the whole application — this is the class Java
 * actually runs first. It sets up the app's data, opens the window on
 * the Login screen, and makes sure everything is saved when the app is
 * closed.
 */
public class MainApp extends Application {

    private AppContext context;

    /** Called by JavaFX when the app launches: loads saved (or seeded) data and opens the window. */
    @Override
    public void start(Stage primaryStage) {
        context = new AppContext();
        context.loadData();

        ScreenManager screenManager = new ScreenManager(primaryStage, context);

        // Dev convenience only: -Dsbb.debug.screen=<ScreenId> jumps straight
        // into the app shell, skipping the (non-authenticating) login gate,
        // so a specific screen can be reached quickly during development.
        String debugScreen = System.getProperty("sbb.debug.screen");
        if (debugScreen != null) {
            screenManager.showApp(ScreenManager.ScreenId.valueOf(debugScreen.trim().toUpperCase()), "admin");
        } else {
            screenManager.showLogin();
        }
    }

    /** Called by JavaFX when the app is closing: saves the current data so nothing is lost. */
    @Override
    public void stop() {
        if (context != null) {
            context.saveData();
        }
    }

    /** The Java entry point — hands control over to JavaFX, which then calls start() above. */
    public static void main(String[] args) {
        launch(args);
    }
}
