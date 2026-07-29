package com.smartbloodbank.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Shared page layout used by every screen in the app: a title bar at the
 * top (with today's date) and a scrollable content area below it. Each
 * individual screen (Dashboard, Donor Management, etc.) only has to
 * supply its title and its own content — this class takes care of
 * building the consistent frame around it.
 *
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

    /** Builds the shared title bar + scrollable body frame around whatever content the subclass provides. */
    protected Screen(AppContext context, AppShell appShell) {
        this.context = context;
        this.appShell = appShell;
        setTop(buildHeader());
        setCenter(buildBody());
    }

    /** Each screen returns the title shown in the top bar, e.g. "Dashboard". */
    protected abstract String getTitle();

    /** Each screen builds and returns its own main content, placed below the top bar. */
    protected abstract Node buildContent();

    /** Optional header-right action button(s), placed before the date; none by default. */
    protected Node buildHeaderActions() {
        return null;
    }

    /**
     * Shows a simple OK-only information popup, e.g. a validation error or
     * a confirmation like "not eligible to donate". Anchored to the main
     * window via {@link #attachToOwner} so it opens as an in-app popup
     * instead of a separate floating OS window.
     */
    protected void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        attachToOwner(alert);
        alert.showAndWait();
    }

    /**
     * Anchors any Alert or Dialog to this screen's own window (its owner)
     * and makes it window-modal, so it appears centered on top of the main
     * app window and blocks interaction with it until dismissed — instead
     * of popping up as a separate, disconnected window that steals focus.
     * Call this right after creating any {@code new Alert(...)} or
     * {@code new Dialog<>()}, before showing it.
     */
    protected void attachToOwner(Dialog<?> dialog) {
        if (getScene() != null && getScene().getWindow() != null) {
            dialog.initOwner(getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
        }
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
