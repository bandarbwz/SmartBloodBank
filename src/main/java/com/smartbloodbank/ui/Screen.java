package com.smartbloodbank.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * ABSTRACTION: common chrome shared by every screen (a header with a
 * title/subtitle/optional action, and a scrollable body below it). Each
 * concrete screen only supplies its title, subtitle and body content —
 * the same abstract-parent/concrete-child split used by User and its
 * subclasses in the model layer (POLYMORPHISM via method overriding).
 */
public abstract class Screen extends BorderPane {

    protected final AppContext context;
    protected final AppShell appShell;

    protected Screen(AppContext context, AppShell appShell) {
        this.context = context;
        this.appShell = appShell;
        setTop(buildHeader());
        setCenter(buildBody());
    }

    protected abstract String getTitle();

    protected abstract String getSubtitle();

    protected abstract Node buildContent();

    /** Optional header-right action button(s); none by default. */
    protected Node buildHeaderActions() {
        return null;
    }

    private Node buildHeader() {
        VBox titleBox = new VBox(2);
        Label title = new Label(getTitle());
        title.getStyleClass().add("page-title");
        Label subtitle = new Label(getSubtitle());
        subtitle.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        HBox header = new HBox();
        header.getStyleClass().add("top-bar");
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(titleBox);

        Node actions = buildHeaderActions();
        if (actions != null) {
            header.getChildren().add(actions);
        }
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
