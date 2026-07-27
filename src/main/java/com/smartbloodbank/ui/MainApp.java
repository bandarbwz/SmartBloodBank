package com.smartbloodbank.ui;

import javafx.application.Application;
import javafx.stage.Stage;

/** Application entry point: loads saved data, shows the Login screen, and saves on exit. */
public class MainApp extends Application {

    private AppContext context;

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

    @Override
    public void stop() {
        if (context != null) {
            context.saveData();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
