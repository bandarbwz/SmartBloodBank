package com.smartbloodbank.ui;

import com.smartbloodbank.model.BloodBag;
import com.smartbloodbank.model.BloodType;
import com.smartbloodbank.model.Donor;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Tracks every blood bag from donation through to use or expiry. */
public class InventoryScreen extends Screen {

    private TableView<BloodBag> table;

    public InventoryScreen(AppContext context, AppShell appShell) {
        super(context, appShell);
    }

    @Override
    protected String getTitle() {
        return "Blood Inventory";
    }

    @Override
    protected String getSubtitle() {
        return "Track every blood bag from donation to expiry.";
    }

    @Override
    protected Node buildHeaderActions() {
        Button addButton = new Button("+ Add Blood Bag");
        addButton.getStyleClass().add("button-primary");
        addButton.setOnAction(e -> openBagForm());
        return addButton;
    }

    @Override
    protected Node buildContent() {
        context.getInventoryManager().updateExpiredBags();

        VBox card = new VBox(14);
        card.getStyleClass().add("card");

        table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(context.getBloodBank().getAllBloodBags()));
        table.setPlaceholder(new Label("No blood bags recorded yet."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<BloodBag, String> idCol = new TableColumn<>("Bag ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("bagId"));

        TableColumn<BloodBag, BloodType> typeCol = new TableColumn<>("Blood Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("bloodType"));

        TableColumn<BloodBag, String> donorCol = new TableColumn<>("Donor ID");
        donorCol.setCellValueFactory(new PropertyValueFactory<>("donorId"));

        TableColumn<BloodBag, String> donatedCol = new TableColumn<>("Donated");
        donatedCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDonationDate().toString()));

        TableColumn<BloodBag, String> expiresCol = new TableColumn<>("Expires");
        expiresCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getExpirationDate().toString()));

        TableColumn<BloodBag, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        statusCol.setCellFactory(col -> statusPillCell());

        table.getColumns().setAll(List.of(idCol, typeCol, donorCol, donatedCol, expiresCol, statusCol));

        card.getChildren().addAll(buildToolbar(), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return card;
    }

    private TableCell<BloodBag, String> statusPillCell() {
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
                pill.getStyleClass().add(switch (value) {
                    case "AVAILABLE" -> "status-pill-success";
                    case "RESERVED" -> "status-pill-warning";
                    case "EXPIRED" -> "status-pill-danger";
                    default -> "status-pill-neutral";
                });
                setGraphic(pill);
            }
        };
    }

    private HBox buildToolbar() {
        Button refreshButton = new Button("Refresh Expiry Status");
        refreshButton.getStyleClass().add("button-secondary");
        refreshButton.setOnAction(e -> {
            context.getInventoryManager().updateExpiredBags();
            refreshTable();
        });

        Button deleteButton = new Button("Delete Selected");
        deleteButton.getStyleClass().add("button-danger");
        deleteButton.setOnAction(e -> deleteSelected());

        HBox toolbar = new HBox(10, refreshButton, deleteButton);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        return toolbar;
    }

    private void deleteSelected() {
        BloodBag selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a blood bag first.");
            return;
        }
        context.getBloodBank().removeBloodBag(selected.getBagId());
        refreshTable();
    }

    private void openBagForm() {
        List<Donor> eligibleDonors = context.getBloodBank().getAllDonors().stream()
                .filter(Donor::isEligibleToDonate)
                .collect(Collectors.toList());
        if (eligibleDonors.isEmpty()) {
            showInfo("No donors are currently eligible to donate (90-day minimum interval between donations).");
            return;
        }

        Dialog<BloodBag> dialog = new Dialog<>();
        dialog.setTitle("Add Blood Bag");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        ComboBox<Donor> donorBox = new ComboBox<>(FXCollections.observableArrayList(eligibleDonors));
        donorBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Donor donor) {
                return donor == null ? "" : donor.getId() + " — " + donor.getFullName() + " (" + donor.getBloodType() + ")";
            }

            @Override
            public Donor fromString(String string) {
                return null;
            }
        });
        donorBox.getSelectionModel().selectFirst();

        DatePicker donationDatePicker = new DatePicker(LocalDate.now());

        GridPane grid = formGrid();
        grid.addRow(0, formLabel("Donor"), donorBox);
        grid.addRow(1, formLabel("Donation Date"), donationDatePicker);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            Donor donor = donorBox.getValue();
            LocalDate date = donationDatePicker.getValue();
            if (donor == null || date == null || date.isAfter(LocalDate.now())) {
                return null;
            }
            donor.recordDonation(date);
            return new BloodBag(donor.getBloodType(), donor.getId(), date);
        });

        Optional<BloodBag> result = dialog.showAndWait();
        result.ifPresent(bag -> {
            context.getBloodBank().addBloodBag(bag);
            refreshTable();
        });
    }

    private void refreshTable() {
        table.setItems(FXCollections.observableArrayList(context.getBloodBank().getAllBloodBags()));
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
