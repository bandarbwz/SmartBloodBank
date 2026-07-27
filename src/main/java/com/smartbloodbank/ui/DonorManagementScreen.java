package com.smartbloodbank.ui;

import com.smartbloodbank.model.BloodBag;
import com.smartbloodbank.model.BloodType;
import com.smartbloodbank.model.Donor;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/**
 * Shows every registered donor in a table (ID, name, blood type, phone,
 * last donation date, total donations, and whether they're currently
 * eligible to donate again). From here staff can: click "+ Add Donor" to
 * register a new donor, click "Edit" on a row to change that donor's
 * details, select a row and click "Record Donation" to log a fresh
 * donation, or select a row and click "Delete Selected" to remove a
 * donor. Adding/editing a donor calls BloodBank.addDonor(), and
 * recording a donation creates a new BloodBag and calls
 * BloodBank.addBloodBag() so it shows up in the inventory too.
 *
 * Registers donors, tracks donation eligibility, and lets staff log a completed donation.
 */
public class DonorManagementScreen extends Screen {

    private Label countLabel;
    private VBox formContainer;
    private TableView<Donor> table;

    /** Builds the donor management screen for the given shared app data. */
    public DonorManagementScreen(AppContext context, AppShell appShell) {
        super(context, appShell);
    }

    @Override
    protected String getTitle() {
        return "Donor Management";
    }

    @Override
    protected Node buildContent() {
        countLabel = new Label();
        formContainer = new VBox();

        Button addButton = new Button("+ Add Donor");
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
        Button recordDonationButton = new Button("Record Donation");
        recordDonationButton.getStyleClass().add("button-secondary");
        recordDonationButton.setOnAction(e -> recordDonationForSelected());

        Button deleteButton = new Button("Delete Selected");
        deleteButton.getStyleClass().add("button-danger");
        deleteButton.setOnAction(e -> deleteSelected());

        HBox toolbar = new HBox(10, recordDonationButton, deleteButton);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        return toolbar;
    }

    private TableView<Donor> buildTable() {
        TableView<Donor> tableView = new TableView<>();
        tableView.setItems(FXCollections.observableArrayList(context.getBloodBank().getAllDonors()));
        tableView.setPlaceholder(new Label("No donors registered yet."));
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Donor, String> idCol = new TableColumn<>("Donor ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Donor, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<Donor, BloodType> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("bloodType"));

        TableColumn<Donor, String> contactCol = new TableColumn<>("Phone");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        TableColumn<Donor, String> lastDonationCol = new TableColumn<>("Last Donation");
        lastDonationCol.setCellValueFactory(d -> {
            List<LocalDate> history = d.getValue().getDonationHistory();
            String text = history.isEmpty() ? "—" : history.get(history.size() - 1).toString();
            return new SimpleStringProperty(text);
        });

        TableColumn<Donor, Number> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getTotalDonations()));

        TableColumn<Donor, Donor> eligibilityCol = new TableColumn<>("Eligibility");
        eligibilityCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue()));
        eligibilityCol.setCellFactory(col -> eligibilityCell());

        TableColumn<Donor, Donor> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue()));
        actionsCol.setCellFactory(col -> editCell());

        tableView.getColumns().setAll(List.of(idCol, nameCol, typeCol, contactCol, lastDonationCol, totalCol, eligibilityCol, actionsCol));
        return tableView;
    }

    private TableCell<Donor, Donor> eligibilityCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Donor donor, boolean empty) {
                super.updateItem(donor, empty);
                if (empty || donor == null) {
                    setGraphic(null);
                    return;
                }
                boolean eligible = donor.isEligibleToDonate();
                String text = eligible ? "Eligible" : "From " + LocalDate.now().plusDays(donor.daysUntilEligible());
                Label pill = new Label(text);
                pill.getStyleClass().addAll("status-pill", eligible ? "status-pill-available" : "status-pill-reserved");
                setGraphic(pill);
            }
        };
    }

    private TableCell<Donor, Donor> editCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Donor donor, boolean empty) {
                super.updateItem(donor, empty);
                if (empty || donor == null) {
                    setGraphic(null);
                    return;
                }
                Button editButton = new Button("Edit");
                editButton.getStyleClass().add("button-small");
                editButton.setOnAction(e -> openForm(donor));
                setGraphic(editButton);
            }
        };
    }

    private void openForm(Donor donor) {
        formContainer.getChildren().setAll(buildFormCard(donor));
    }

    private void closeForm() {
        formContainer.getChildren().clear();
    }

    private Node buildFormCard(Donor donor) {
        boolean editing = donor != null;

        Label title = new Label(editing ? "Edit Donor" : "Add New Donor");
        title.getStyleClass().add("card-title");

        TextField nameField = new TextField(editing ? donor.getFullName() : "");
        TextField contactField = new TextField(editing ? donor.getContactNumber() : "");
        ComboBox<BloodType> typeBox = new ComboBox<>(FXCollections.observableArrayList(BloodType.values()));
        typeBox.getSelectionModel().select(editing ? donor.getBloodType() : BloodType.O_POSITIVE);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(6);
        grid.addRow(0, formLabel("Full Name"), formLabel("Blood Type"), formLabel("Phone"));
        grid.addRow(1, nameField, typeBox, contactField);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(contactField, Priority.ALWAYS);
        for (int col = 0; col < 3; col++) {
            javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
            cc.setPercentWidth(100.0 / 3);
            grid.getColumnConstraints().add(cc);
        }
        nameField.setMaxWidth(Double.MAX_VALUE);
        contactField.setMaxWidth(Double.MAX_VALUE);
        typeBox.setMaxWidth(Double.MAX_VALUE);

        Label error = new Label("Full name and phone number are required.");
        error.getStyleClass().add("login-error");
        error.setVisible(false);
        error.managedProperty().bind(error.visibleProperty());

        Button saveButton = new Button(editing ? "Save Donor" : "Add Donor");
        saveButton.getStyleClass().add("button-primary");
        saveButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            if (name.isBlank() || contact.isBlank()) {
                error.setVisible(true);
                return;
            }
            if (editing) {
                donor.setFullName(name);
                donor.setContactNumber(contact);
                donor.setBloodType(typeBox.getValue());
            } else {
                context.getBloodBank().addDonor(new Donor(name, contact, typeBox.getValue()));
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

    private void recordDonationForSelected() {
        Donor selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a donor first.");
            return;
        }
        if (!selected.isEligibleToDonate()) {
            showInfo(selected.getFullName() + " is not eligible to donate for another "
                    + selected.daysUntilEligible() + " day(s).");
            return;
        }
        selected.recordDonation(LocalDate.now());
        context.getBloodBank().addBloodBag(new BloodBag(selected.getBloodType(), selected.getId(), LocalDate.now()));
        table.refresh();
    }

    private void deleteSelected() {
        Donor selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a donor first.");
            return;
        }
        context.getBloodBank().removeDonor(selected.getId());
        refreshTable();
    }

    private void refreshTable() {
        table.setItems(FXCollections.observableArrayList(context.getBloodBank().getAllDonors()));
        updateCount();
    }

    private void updateCount() {
        countLabel.setText(context.getBloodBank().getAllDonors().size() + " registered donors");
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
