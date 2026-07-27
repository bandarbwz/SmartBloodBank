package com.smartbloodbank.ui;

import com.smartbloodbank.model.BloodBag;
import com.smartbloodbank.model.BloodType;
import com.smartbloodbank.model.Donor;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Registers donors and lets staff record a completed donation, which creates a new BloodBag. */
public class DonorManagementScreen extends Screen {

    private TableView<Donor> table;

    public DonorManagementScreen(AppContext context, AppShell appShell) {
        super(context, appShell);
    }

    @Override
    protected String getTitle() {
        return "Donor Management";
    }

    @Override
    protected String getSubtitle() {
        return "Register donors and track donation eligibility.";
    }

    @Override
    protected Node buildHeaderActions() {
        Button addButton = new Button("+ Add Donor");
        addButton.getStyleClass().add("button-primary");
        addButton.setOnAction(e -> openDonorForm());
        return addButton;
    }

    @Override
    protected Node buildContent() {
        VBox card = new VBox(14);
        card.getStyleClass().add("card");

        table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(context.getBloodBank().getAllDonors()));
        table.setPlaceholder(new Label("No donors registered yet."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Donor, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Donor, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<Donor, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        TableColumn<Donor, BloodType> typeCol = new TableColumn<>("Blood Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("bloodType"));

        TableColumn<Donor, Number> donationsCol = new TableColumn<>("Donations");
        donationsCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getTotalDonations()));

        TableColumn<Donor, String> eligibleCol = new TableColumn<>("Eligible Today");
        eligibleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isEligibleToDonate()
                ? "Yes" : "No (" + d.getValue().daysUntilEligible() + "d)"));

        table.getColumns().setAll(List.of(idCol, nameCol, contactCol, typeCol, donationsCol, eligibleCol));

        card.getChildren().addAll(buildToolbar(), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return card;
    }

    private HBox buildToolbar() {
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
        refreshTable();
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

    private void openDonorForm() {
        Dialog<Donor> dialog = new Dialog<>();
        dialog.setTitle("Add Donor");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        TextField nameField = new TextField();
        TextField contactField = new TextField();
        ComboBox<BloodType> typeBox = new ComboBox<>(FXCollections.observableArrayList(BloodType.values()));
        typeBox.getSelectionModel().selectFirst();

        GridPane grid = formGrid();
        grid.addRow(0, formLabel("Full Name"), nameField);
        grid.addRow(1, formLabel("Contact Number"), contactField);
        grid.addRow(2, formLabel("Blood Type"), typeBox);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK && !nameField.getText().isBlank() && !contactField.getText().isBlank()) {
                return new Donor(nameField.getText(), contactField.getText(), typeBox.getValue());
            }
            return null;
        });

        Optional<Donor> result = dialog.showAndWait();
        result.ifPresent(donor -> {
            context.getBloodBank().addDonor(donor);
            refreshTable();
        });
    }

    private void refreshTable() {
        table.setItems(FXCollections.observableArrayList(context.getBloodBank().getAllDonors()));
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
