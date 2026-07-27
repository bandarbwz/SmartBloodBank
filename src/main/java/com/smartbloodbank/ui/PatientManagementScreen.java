package com.smartbloodbank.ui;

import com.smartbloodbank.model.BloodType;
import com.smartbloodbank.model.EmergencyLevel;
import com.smartbloodbank.model.Patient;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

/** Registers patients and lets staff submit their request into the emergency queue. */
public class PatientManagementScreen extends Screen {

    private TableView<Patient> table;

    public PatientManagementScreen(AppContext context, AppShell appShell) {
        super(context, appShell);
    }

    @Override
    protected String getTitle() {
        return "Patient Management";
    }

    @Override
    protected String getSubtitle() {
        return "Register patients and submit emergency blood requests.";
    }

    @Override
    protected Node buildHeaderActions() {
        Button addButton = new Button("+ Add Patient");
        addButton.getStyleClass().add("button-primary");
        addButton.setOnAction(e -> openPatientForm());
        return addButton;
    }

    @Override
    protected Node buildContent() {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");

        table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(context.getBloodBank().getAllPatients()));
        table.setPlaceholder(new Label("No patients registered yet."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Patient, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Patient, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<Patient, BloodType> typeCol = new TableColumn<>("Blood Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("bloodType"));

        TableColumn<Patient, String> wardCol = new TableColumn<>("Ward");
        wardCol.setCellValueFactory(new PropertyValueFactory<>("wardNumber"));

        TableColumn<Patient, Number> unitsCol = new TableColumn<>("Units Needed");
        unitsCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getUnitsRequired()));

        TableColumn<Patient, String> urgencyCol = new TableColumn<>("Urgency");
        urgencyCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmergencyLevel().getDescription()));

        TableColumn<Patient, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isFulfilled() ? "Fulfilled" : "Pending"));
        statusCol.setCellFactory(col -> statusPillCell());

        table.getColumns().setAll(List.of(idCol, nameCol, typeCol, wardCol, unitsCol, urgencyCol, statusCol));

        card.getChildren().addAll(buildToolbar(), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return card;
    }

    private TableCell<Patient, String> statusPillCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                    return;
                }
                Label pill = new Label(value);
                pill.getStyleClass().add("status-pill");
                pill.getStyleClass().add("Fulfilled".equals(value) ? "status-pill-success" : "status-pill-warning");
                setGraphic(pill);
            }
        };
    }

    private HBox buildToolbar() {
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

    private void openPatientForm() {
        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle("Add Patient");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        TextField nameField = new TextField();
        TextField contactField = new TextField();
        ComboBox<BloodType> typeBox = new ComboBox<>(FXCollections.observableArrayList(BloodType.values()));
        typeBox.getSelectionModel().selectFirst();
        ComboBox<EmergencyLevel> urgencyBox = new ComboBox<>(FXCollections.observableArrayList(EmergencyLevel.values()));
        urgencyBox.getSelectionModel().select(EmergencyLevel.MEDIUM);
        Spinner<Integer> unitsSpinner = new Spinner<>(1, 20, 1);
        unitsSpinner.setEditable(true);
        TextField wardField = new TextField();

        GridPane grid = formGrid();
        grid.addRow(0, formLabel("Full Name"), nameField);
        grid.addRow(1, formLabel("Contact Number"), contactField);
        grid.addRow(2, formLabel("Blood Type"), typeBox);
        grid.addRow(3, formLabel("Urgency"), urgencyBox);
        grid.addRow(4, formLabel("Units Required"), unitsSpinner);
        grid.addRow(5, formLabel("Ward Number"), wardField);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK && !nameField.getText().isBlank()
                    && !contactField.getText().isBlank() && !wardField.getText().isBlank()) {
                return new Patient(nameField.getText(), contactField.getText(), typeBox.getValue(),
                        urgencyBox.getValue(), unitsSpinner.getValue(), wardField.getText());
            }
            return null;
        });

        Optional<Patient> result = dialog.showAndWait();
        result.ifPresent(patient -> {
            context.getBloodBank().addPatient(patient);
            refreshTable();
        });
    }

    private void refreshTable() {
        table.setItems(FXCollections.observableArrayList(context.getBloodBank().getAllPatients()));
    }

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 0, 0));
        return grid;
    }

    private Label formLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
