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
        screenManager.showLogin();
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
