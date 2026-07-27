package com.smartbloodbank.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ABSTRACTION: common chrome shared by every screen (a top bar with the
 * screen title and today's date, and a scrollable body below it). Each
 * concrete screen only supplies its title and body content — the same
 * abstract-parent/concrete-child split used by User and its subclasses in
 * the model layer (POLYMORPHISM via method overriding).
 */
public abstract class Screen extends BorderPane {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy");

    protected final AppContext context;
    protected final AppShell appShell;

    protected Screen(AppContext context, AppShell appShell) {
        this.context = context;
        this.appShell = appShell;
        setTop(buildHeader());
        setCenter(buildBody());
    }

    protected abstract String getTitle();

    protected abstract Node buildContent();

    /** Optional header-right action button(s), placed before the date; none by default. */
    protected Node buildHeaderActions() {
        return null;
    }

    private Node buildHeader() {
        Label title = new Label(getTitle());
        title.getStyleClass().add("page-title");
        HBox.setHgrow(title, Priority.ALWAYS);

        HBox header = new HBox(20);
        header.getStyleClass().add("top-bar");
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(title);

        Node actions = buildHeaderActions();
        if (actions != null) {
            header.getChildren().add(actions);
        }

        Label date = new Label(LocalDate.now().format(DATE_FORMAT));
        date.getStyleClass().add("top-bar-date");
        header.getChildren().add(date);

        return header;
    }

    private Node buildBody() {
        ScrollPane scrollPane = new ScrollPane(buildContent());
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox body = new VBox(scrollPane);
        body.getStyleClass().add("content-area");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return body;
    }
}
