package com.smartbloodbank.ui;

import com.smartbloodbank.model.BloodType;
import com.smartbloodbank.model.Patient;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/** Shows patients waiting for blood, ordered by urgency, and processes the queue via BloodMatcher. */
public class EmergencyRequestScreen extends Screen {

    private TableView<Patient> table;

    public EmergencyRequestScreen(AppContext context, AppShell appShell) {
        super(context, appShell);
    }

    @Override
    protected String getTitle() {
        return "Emergency Requests";
    }

    @Override
    protected String getSubtitle() {
        return "Patients waiting for blood, ordered by urgency.";
    }

    @Override
    protected Node buildHeaderActions() {
        Button processButton = new Button("Process Next Request");
        processButton.getStyleClass().add("button-primary");
        processButton.setOnAction(e -> processNext());
        return processButton;
    }

    @Override
    protected Node buildContent() {
        VBox root = new VBox(20);
        root.getChildren().addAll(buildNextInLineCard(), buildQueueCard());
        return root;
    }

    private Node buildNextInLineCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        Label title = new Label("Next in Line");
        title.getStyleClass().add("card-title");

        Patient next = context.getEmergencyRequest().peekNextRequest();
        Label detail = new Label(next == null
                ? "No pending emergency requests."
                : String.format("%s — %s | Needs %d unit(s) of %s | Ward %s",
                        next.getFullName(), next.getEmergencyLevel().getDescription(),
                        next.getUnitsRequired(), next.getBloodType(), next.getWardNumber()));
        detail.getStyleClass().add(next == null ? "empty-state" : "page-subtitle");

        card.getChildren().addAll(title, detail);
        return card;
    }

    private Node buildQueueCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");

        Label title = new Label("Pending Queue (by urgency)");
        title.getStyleClass().add("card-title");

        table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(context.getEmergencyRequest().getPendingRequestsSorted()));
        table.setPlaceholder(new Label("No pending requests."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Patient, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Patient, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<Patient, BloodType> typeCol = new TableColumn<>("Blood Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("bloodType"));

        TableColumn<Patient, String> urgencyCol = new TableColumn<>("Urgency");
        urgencyCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmergencyLevel().getDescription()));

        TableColumn<Patient, Number> unitsCol = new TableColumn<>("Units Needed");
        unitsCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getUnitsRequired()));

        TableColumn<Patient, String> wardCol = new TableColumn<>("Ward");
        wardCol.setCellValueFactory(new PropertyValueFactory<>("wardNumber"));

        table.getColumns().setAll(List.of(idCol, nameCol, typeCol, urgencyCol, unitsCol, wardCol));

        Button removeButton = new Button("Remove Selected From Queue");
        removeButton.getStyleClass().add("button-danger");
        removeButton.setOnAction(e -> removeSelected());

        HBox toolbar = new HBox(removeButton);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(title, toolbar, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return card;
    }

    private void processNext() {
        Patient processed = context.getEmergencyRequest().processNextRequest();
        if (processed == null) {
            showInfo(context.getEmergencyRequest().getPendingCount() == 0
                    ? "There are no pending requests."
                    : "Not enough compatible stock to fulfill the next request yet.");
            return;
        }
        showInfo("Fulfilled request for " + processed.getFullName() + " (" + processed.getUnitsRequired()
                + " unit(s) of " + processed.getBloodType() + ").");
        appShell.refreshCurrent();
    }

    private void removeSelected() {
        Patient selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a patient first.");
            return;
        }
        context.getEmergencyRequest().removeRequest(selected);
        appShell.refreshCurrent();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
