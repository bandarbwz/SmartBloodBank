package com.smartbloodbank.ui;

import com.smartbloodbank.model.BloodType;
import com.smartbloodbank.model.EmergencyLevel;
import com.smartbloodbank.model.Patient;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Shows every registered patient in a table (ID, name, blood type,
 * ward, units needed, emergency level, and whether their request has
 * been fulfilled). From here staff can: click "+ Add Patient" to
 * register a new patient, click "Edit" on a row to change that
 * patient's details, select a row and click "Submit Emergency Request"
 * to put them in the emergency queue, or select a row and click
 * "Delete Selected" to remove a patient. Adding/editing calls
 * BloodBank.addPatient(), and submitting a request calls
 * EmergencyRequest.submitRequest() so the patient shows up on the
 * Emergency Requests screen.
 *
 * Registers patients, submits their emergency blood requests, and tracks fulfillment status.
 */
public class PatientManagementScreen extends Screen {

    private Label countLabel;
    private VBox formContainer;
    private TableView<Patient> table;

    /** Builds the patient management screen for the given shared app data. */
    public PatientManagementScreen(AppContext context, AppShell appShell) {
        super(context, appShell);
    }

    @Override
    protected String getTitle() {
        return "Patient Management";
    }

    @Override
    protected Node buildContent() {
        countLabel = new Label();
        formContainer = new VBox();

        Button addButton = new Button("+ Add Patient");
        addButton.getStyleClass().add("button-primary");
        addButton.setOnAction(e -> openForm(null));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(10, countLabel, spacer, addButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        HBox secondaryActions = buildSecondaryActions();

        table = buildTable();

        VBox root = new VBox(16, toolbar, formContainer, secondaryActions, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        updateCount();
        return root;
    }

    private HBox buildSecondaryActions() {
        Button requestButton = new Button("Submit Emergency Request");
        requestButton.getStyleClass().add("button-secondary");
        requestButton.setOnAction(e -> submitRequestForSelected());

        Button deleteButton = new Button("Delete Selected");
        deleteButton.getStyleClass().add("button-danger");
        deleteButton.setOnAction(e -> deleteSelected());

        HBox toolbar = new HBox(10, requestButton, deleteButton);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        return toolbar;
    }

    private TableView<Patient> buildTable() {
        TableView<Patient> tableView = new TableView<>();
        tableView.setItems(FXCollections.observableArrayList(context.getBloodBank().getAllPatients()));
        tableView.setPlaceholder(new Label("No patients registered yet."));
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Patient, String> idCol = new TableColumn<>("Patient ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Patient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<Patient, BloodType> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("bloodType"));

        TableColumn<Patient, String> wardCol = new TableColumn<>("Ward");
        wardCol.setCellValueFactory(new PropertyValueFactory<>("wardNumber"));

        TableColumn<Patient, Number> unitsCol = new TableColumn<>("Units");
        unitsCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getUnitsRequired()));

        TableColumn<Patient, Patient> emergencyCol = new TableColumn<>("Emergency");
        emergencyCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue()));
        emergencyCol.setCellFactory(col -> emergencyCell());

        TableColumn<Patient, Patient> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue()));
        statusCol.setCellFactory(col -> statusCell());

        TableColumn<Patient, Patient> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue()));
        actionsCol.setCellFactory(col -> editCell());

        tableView.getColumns().setAll(List.of(idCol, nameCol, typeCol, wardCol, unitsCol, emergencyCol, statusCol, actionsCol));
        return tableView;
    }

    private TableCell<Patient, Patient> emergencyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) {
                    setGraphic(null);
                    return;
                }
                Label pill = new Label(patient.getEmergencyLevel().getDescription());
                pill.getStyleClass().addAll("emergency-pill", "emergency-" + patient.getEmergencyLevel().name().toLowerCase());
                setGraphic(pill);
            }
        };
    }

    private TableCell<Patient, Patient> statusCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) {
                    setGraphic(null);
                    return;
                }
                Label pill = new Label(patient.isFulfilled() ? "Fulfilled" : "Pending");
                pill.getStyleClass().addAll("status-pill", patient.isFulfilled() ? "status-pill-available" : "status-pill-reserved");
                setGraphic(pill);
            }
        };
    }

    private TableCell<Patient, Patient> editCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                if (empty || patient == null) {
                    setGraphic(null);
                    return;
                }
                Button editButton = new Button("Edit");
                editButton.getStyleClass().add("button-small");
                editButton.setOnAction(e -> openForm(patient));
                setGraphic(editButton);
            }
        };
    }

    private void openForm(Patient patient) {
        formContainer.getChildren().setAll(buildFormCard(patient));
    }

    private void closeForm() {
        formContainer.getChildren().clear();
    }

    private Node buildFormCard(Patient patient) {
        boolean editing = patient != null;

        Label title = new Label(editing ? "Edit Patient" : "Add New Patient");
        title.getStyleClass().add("card-title");

        TextField nameField = new TextField(editing ? patient.getFullName() : "");
        TextField contactField = new TextField(editing ? patient.getContactNumber() : "");
        ComboBox<BloodType> typeBox = new ComboBox<>(FXCollections.observableArrayList(BloodType.values()));
        typeBox.getSelectionModel().select(editing ? patient.getBloodType() : BloodType.O_POSITIVE);
        TextField wardField = new TextField(editing ? patient.getWardNumber() : "");
        ComboBox<EmergencyLevel> urgencyBox = new ComboBox<>(FXCollections.observableArrayList(EmergencyLevel.values()));
        urgencyBox.getSelectionModel().select(editing ? patient.getEmergencyLevel() : EmergencyLevel.MEDIUM);
        Spinner<Integer> unitsSpinner = new Spinner<>(1, 20, editing ? patient.getUnitsRequired() : 1);
        unitsSpinner.setEditable(true);
        unitsSpinner.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(6);
        grid.addRow(0, formLabel("Full Name"), formLabel("Blood Type"), formLabel("Contact"));
        grid.addRow(1, nameField, typeBox, contactField);
        grid.addRow(2, formLabel("Ward"), formLabel("Emergency Level"), formLabel("Units Required"));
        grid.addRow(3, wardField, urgencyBox, unitsSpinner);
        for (Node field : List.of(nameField, typeBox, contactField, wardField, urgencyBox, unitsSpinner)) {
            GridPane.setHgrow(field, Priority.ALWAYS);
            if (field instanceof javafx.scene.control.Control control) {
                control.setMaxWidth(Double.MAX_VALUE);
            }
        }
        for (int col = 0; col < 3; col++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 3);
            grid.getColumnConstraints().add(cc);
        }

        Label error = new Label("Full name, contact and ward are required.");
        error.getStyleClass().add("login-error");
        error.setVisible(false);
        error.managedProperty().bind(error.visibleProperty());

        Button saveButton = new Button(editing ? "Save Patient" : "Add Patient");
        saveButton.getStyleClass().add("button-primary");
        saveButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            String ward = wardField.getText().trim();
            if (name.isBlank() || contact.isBlank() || ward.isBlank()) {
                error.setVisible(true);
                return;
            }
            if (editing) {
                patient.setFullName(name);
                patient.setContactNumber(contact);
                patient.setBloodType(typeBox.getValue());
                patient.setWardNumber(ward);
                patient.setEmergencyLevel(urgencyBox.getValue());
                patient.setUnitsRequired(unitsSpinner.getValue());
            } else {
                context.getBloodBank().addPatient(new Patient(name, contact, typeBox.getValue(),
                        urgencyBox.getValue(), unitsSpinner.getValue(), ward));
            }
            closeForm();
            refreshTable();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("button-secondary");
        cancelButton.setOnAction(e -> closeForm());

        HBox buttons = new HBox(10, saveButton, cancelButton);

        VBox card = new VBox(16, title, grid, error, buttons);
        card.getStyleClass().add("card");
        return card;
    }

    private Label formLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    private void submitRequestForSelected() {
        Patient selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a patient first.");
            return;
        }
        if (selected.isFulfilled()) {
            showInfo(selected.getFullName() + "'s request has already been fulfilled.");
            return;
        }
        context.getEmergencyRequest().submitRequest(selected);
        showInfo(selected.getFullName() + " has been added to the Emergency Requests queue.");
    }

    private void deleteSelected() {
        Patient selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a patient first.");
            return;
        }
        context.getBloodBank().removePatient(selected.getId());
        context.getEmergencyRequest().removeRequest(selected);
        refreshTable();
    }

    private void refreshTable() {
        table.setItems(FXCollections.observableArrayList(context.getBloodBank().getAllPatients()));
        updateCount();
    }

    private void updateCount() {
        countLabel.setText(context.getBloodBank().getAllPatients().size() + " active patient records");
    }
}
