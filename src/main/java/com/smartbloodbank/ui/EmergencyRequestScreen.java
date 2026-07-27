package com.smartbloodbank.ui;

import com.smartbloodbank.model.BloodBag;
import com.smartbloodbank.model.EmergencyLevel;
import com.smartbloodbank.model.Patient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Patients waiting for blood, ordered by urgency, with a detail panel
 * showing real FIFO-matched compatible bags for whichever request is
 * selected. Fulfilling reserves real stock via BloodMatcher; there is no
 * separate Approve/Reject concept in the domain model, so those actions
 * aren't fabricated here — only Fulfill and Remove, both backed by real
 * service calls.
 */
public class EmergencyRequestScreen extends Screen {

    private VBox queueContainer;
    private VBox detailContainer;
    private Label chipsRow;
    private Patient selectedPatient;

    public EmergencyRequestScreen(AppContext context, AppShell appShell) {
        super(context, appShell);
    }

    @Override
    protected String getTitle() {
        return "Emergency Requests";
    }

    @Override
    protected Node buildContent() {
        queueContainer = new VBox(12);
        detailContainer = new VBox(16);
        detailContainer.getStyleClass().add("card");
        detailContainer.setPrefWidth(340);
        detailContainer.setMinWidth(320);

        VBox leftColumn = new VBox(16, buildChipsRow(), queueContainer);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        HBox root = new HBox(20, leftColumn, detailContainer);
        renderAll();
        return root;
    }

    private Node buildChipsRow() {
        chipsRow = new Label();
        return chipsRow;
    }

    private List<Patient> pendingSorted() {
        return context.getEmergencyRequest().getPendingRequestsSorted();
    }

    private void renderAll() {
        List<Patient> pending = pendingSorted();
        if (selectedPatient == null || !pending.contains(selectedPatient)) {
            selectedPatient = pending.isEmpty() ? null : pending.get(0);
        }
        renderChips(pending);
        renderQueue(pending);
        renderDetail(selectedPatient);
    }

    private void renderChips(List<Patient> pending) {
        long critical = pending.stream().filter(p -> p.getEmergencyLevel() == EmergencyLevel.CRITICAL).count();
        chipsRow.setText(critical + " Critical · " + pending.size() + " Pending");
        chipsRow.getStyleClass().setAll("emergency-pill", "emergency-critical");
    }

    private void renderQueue(List<Patient> pending) {
        queueContainer.getChildren().clear();
        if (pending.isEmpty()) {
            Label empty = new Label("No pending emergency requests.");
            empty.getStyleClass().add("empty-state");
            queueContainer.getChildren().add(empty);
            return;
        }
        for (Patient patient : pending) {
            queueContainer.getChildren().add(buildCard(patient));
        }
    }

    private Node buildCard(Patient patient) {
        boolean critical = patient.getEmergencyLevel() == EmergencyLevel.CRITICAL;
        boolean selected = patient == selectedPatient;

        Label name = new Label(patient.getFullName());
        name.getStyleClass().add("request-card-title");

        Label typeChip = new Label(patient.getBloodType().getLabel());
        typeChip.getStyleClass().add("request-type-chip");

        Label priority = new Label(patient.getEmergencyLevel().getDescription());
        priority.getStyleClass().addAll("emergency-pill", "emergency-" + patient.getEmergencyLevel().name().toLowerCase());

        HBox titleRow = new HBox(10, name, typeChip, priority);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label meta = new Label(patient.getId() + " · " + patient.getUnitsRequired() + " unit(s) needed · Ward " + patient.getWardNumber());
        meta.getStyleClass().add("request-card-meta");

        Button fulfillButton = new Button("Fulfill (FIFO)");
        fulfillButton.getStyleClass().add("button-primary");
        wireCardAction(fulfillButton, e -> fulfill(patient));

        Button removeButton = new Button("Remove");
        removeButton.getStyleClass().add("button-secondary");
        wireCardAction(removeButton, e -> remove(patient));

        HBox actions = new HBox(8, fulfillButton, removeButton);
        actions.setPadding(new Insets(4, 0, 0, 0));

        VBox card = new VBox(6, titleRow, meta, actions);
        card.getStyleClass().add("request-card");
        if (critical) {
            card.getStyleClass().add("request-card-critical");
        }
        if (selected) {
            card.getStyleClass().add("request-card-selected");
        }
        card.setOnMouseClicked(e -> {
            selectedPatient = patient;
            renderAll();
        });
        return card;
    }

    /** Runs the action and stops the click from also bubbling up to the card's own select-on-click handler. */
    private void wireCardAction(Button button, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        button.setOnAction(handler);
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
    }

    private void renderDetail(Patient patient) {
        detailContainer.getChildren().clear();
        if (patient == null) {
            Label empty = new Label("No request selected.");
            empty.getStyleClass().add("empty-state");
            detailContainer.getChildren().add(empty);
            return;
        }

        Label name = new Label(patient.getFullName());
        name.getStyleClass().add("card-title");
        Label subtitle = new Label("Patient " + patient.getId());
        subtitle.getStyleClass().add("card-subtitle");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.add(detailField("BLOOD TYPE", patient.getBloodType().getLabel()), 0, 0);
        grid.add(detailField("UNITS NEEDED", String.valueOf(patient.getUnitsRequired())), 1, 0);
        grid.add(detailField("WARD", patient.getWardNumber()), 0, 1);
        grid.add(detailField("PRIORITY", patient.getEmergencyLevel().getDescription()), 1, 1);
        for (int i = 0; i < 2; i++) {
            javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
            cc.setPercentWidth(50);
            grid.getColumnConstraints().add(cc);
        }

        Label bagsTitle = new Label("Suggested Compatible Bags (FIFO)");
        bagsTitle.getStyleClass().add("form-label");

        VBox bagsList = new VBox();
        bagsList.getStyleClass().add("section-card");
        List<BloodBag> compatible = context.getBloodMatcher().findCompatibleBags(patient.getBloodType());
        if (compatible.isEmpty()) {
            Label none = new Label("No compatible stock available.");
            none.getStyleClass().add("empty-state");
            none.setPadding(new Insets(12));
            bagsList.getChildren().add(none);
        } else {
            for (BloodBag bag : compatible.subList(0, Math.min(3, compatible.size()))) {
                bagsList.getChildren().add(bagRow(bag));
            }
        }

        detailContainer.getChildren().addAll(name, subtitle, grid, bagsTitle, bagsList);
    }

    private Node detailField(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("detail-field-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("detail-field-value");
        return new VBox(2, labelNode, valueNode);
    }

    private Node bagRow(BloodBag bag) {
        Label idType = new Label(bag.getBagId() + " · " + bag.getBloodType().getLabel());
        idType.getStyleClass().add("data-row-emphasis");
        Label expiry = new Label("Expiry " + bag.getExpirationDate());
        expiry.getStyleClass().add("kpi-subtitle");
        VBox left = new VBox(2, idType, expiry);

        Label days = new Label(bag.daysUntilExpiry() <= 0 ? "Expires today" : "Expires in " + bag.daysUntilExpiry() + "d");
        days.getStyleClass().add("expiry-label-warning");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, left, spacer, days);
        row.getStyleClass().add("data-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void fulfill(Patient patient) {
        if (!context.getBloodMatcher().canFulfill(patient.getBloodType(), patient.getUnitsRequired())) {
            showInfo("Not enough compatible stock to fulfill this request yet.");
            return;
        }
        List<BloodBag> reserved = context.getBloodMatcher().matchAndReserve(patient);
        reserved.forEach(BloodBag::markUsed);
        patient.markFulfilled();
        context.getEmergencyRequest().removeRequest(patient);
        if (patient == selectedPatient) {
            selectedPatient = null;
        }
        renderAll();
    }

    private void remove(Patient patient) {
        context.getEmergencyRequest().removeRequest(patient);
        if (patient == selectedPatient) {
            selectedPatient = null;
        }
        renderAll();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
