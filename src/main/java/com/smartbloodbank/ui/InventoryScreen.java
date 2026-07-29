package com.smartbloodbank.ui;

import com.smartbloodbank.model.BloodBag;
import com.smartbloodbank.model.BloodType;
import com.smartbloodbank.model.Donor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Shows the full blood stock, grouped into a section per blood type
 * (each with its own mini table of bag ID, collected date, expiry
 * date, days until expiry, donor, and status). A row of 8 summary
 * cards up top shows the unit count for each type at a glance. From
 * here staff can click "+ Add Blood Bag" to log a new bag from an
 * eligible donor, or click "Remove" on any row to delete that bag.
 * Adding a bag calls BloodBank.addBloodBag() (and Donor.recordDonation()
 * on the chosen donor); removing calls BloodBank.removeBloodBag().
 *
 * Tracks every blood bag from donation through to use or expiry, grouped by blood type.
 */
public class InventoryScreen extends Screen {

    /** Builds the blood inventory screen for the given shared app data. */
    public InventoryScreen(AppContext context, AppShell appShell) {
        super(context, appShell);
    }

    @Override
    protected String getTitle() {
        return "Blood Inventory";
    }

    @Override
    protected Node buildContent() {
        context.getInventoryManager().updateExpiredBags();

        VBox root = new VBox(22);
        root.getChildren().add(buildToolbar());
        root.getChildren().add(buildBloodTypeSummaryRow());
        root.getChildren().addAll(buildGroupedSections());
        return root;
    }

    private Node buildToolbar() {
        int totalUnits = context.getBloodBank().getAllBloodBags().size();
        long typesInStock = context.getBloodBank().getAllBloodBags().stream()
                .map(BloodBag::getBloodType).distinct().count();
        Label summary = new Label(totalUnits + " units across " + typesInStock + " blood types");
        summary.getStyleClass().add("section-label");

        Button addButton = new Button("+ Add Blood Bag");
        addButton.getStyleClass().add("button-primary");
        addButton.setOnAction(e -> openBagForm());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(10, summary, spacer, addButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        return toolbar;
    }

    private Node buildBloodTypeSummaryRow() {
        java.util.Map<BloodType, Integer> summary = context.getBloodBank().getStockSummary();
        HBox row = new HBox(12);
        for (BloodType type : BloodType.values()) {
            VBox card = new VBox(4);
            card.getStyleClass().addAll("blood-type-card", "blood-type-card-normal");
            card.setAlignment(Pos.CENTER);

            Label typeLabel = new Label(type.getLabel());
            typeLabel.getStyleClass().add("blood-type-label");
            Label unitsLabel = new Label(String.valueOf(summary.get(type)));
            unitsLabel.getStyleClass().addAll("blood-type-units", "blood-type-units-normal");
            Label caption = new Label("units");
            caption.getStyleClass().add("blood-type-units-caption");

            card.getChildren().addAll(typeLabel, unitsLabel, caption);
            HBox.setHgrow(card, Priority.ALWAYS);
            row.getChildren().add(card);
        }
        return row;
    }

    private List<Node> buildGroupedSections() {
        List<Node> sections = new ArrayList<>();
        List<BloodBag> allBags = context.getBloodBank().getAllBloodBags();

        for (BloodType type : BloodType.values()) {
            List<BloodBag> bags = allBags.stream()
                    .filter(b -> b.getBloodType() == type)
                    .sorted(Comparator.comparing(BloodBag::getExpirationDate))
                    .collect(Collectors.toList());
            if (bags.isEmpty()) {
                continue;
            }
            sections.add(buildGroupSection(type, bags));
        }
        return sections;
    }

    private Node buildGroupSection(BloodType type, List<BloodBag> bags) {
        Label groupTitle = new Label("Type " + type.getLabel());
        groupTitle.getStyleClass().add("card-title");
        Label groupCount = new Label(bags.size() + " units");
        groupCount.getStyleClass().add("card-subtitle");
        HBox header = new HBox(10, groupTitle, groupCount);
        header.setAlignment(Pos.BASELINE_LEFT);

        VBox tableCard = new VBox();
        tableCard.getStyleClass().add("section-card");
        tableCard.getChildren().add(headerRow());
        for (BloodBag bag : bags) {
            tableCard.getChildren().add(dataRow(bag));
        }

        VBox section = new VBox(8, header, tableCard);
        return section;
    }

    private Node headerRow() {
        HBox row = new HBox();
        row.getStyleClass().add("section-header-row");
        row.getChildren().addAll(
                columnLabel("BAG ID", 1.1),
                columnLabel("COLLECTED", 1.1),
                columnLabel("EXPIRY", 1.1),
                columnLabel("EXPIRES IN", 1.3),
                columnLabel("DONOR", 1.0),
                columnLabel("STATUS", 1.0),
                columnLabel("", 0.7));
        return row;
    }

    private Label columnLabel(String text, double growFactor) {
        Label label = new Label(text);
        label.getStyleClass().add("section-header-label");
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPrefWidth(80 * growFactor);
        return label;
    }

    private Node dataRow(BloodBag bag) {
        Label id = cellLabel(bag.getBagId(), true, 1.1);
        Label collected = cellLabel(bag.getDonationDate().toString(), false, 1.1);
        Label expiry = cellLabel(bag.getExpirationDate().toString(), false, 1.1);

        Node expiresIn = expiryIndicator(bag);
        HBox.setHgrow(expiresIn, Priority.ALWAYS);
        expiresIn.setStyle("-fx-pref-width: " + (80 * 1.3) + "px;");

        Label donor = cellLabel(bag.getDonorId(), false, 1.0);

        Label status = new Label(bag.getStatus().name());
        status.getStyleClass().addAll("status-pill", switch (bag.getStatus()) {
            case AVAILABLE -> "status-pill-available";
            case RESERVED -> "status-pill-reserved";
            case USED -> "status-pill-used";
            case EXPIRED -> "status-pill-expired";
        });
        HBox statusBox = new HBox(status);
        HBox.setHgrow(statusBox, Priority.ALWAYS);
        statusBox.setPrefWidth(80);

        Button remove = new Button("Remove");
        remove.getStyleClass().add("button-small");
        remove.setOnAction(e -> {
            context.getBloodBank().removeBloodBag(bag.getBagId());
            appShell.refreshCurrent();
        });
        HBox actionsBox = new HBox(remove);
        actionsBox.setPrefWidth(56);

        HBox row = new HBox(id, collected, expiry, expiresIn, donor, statusBox, actionsBox);
        row.getStyleClass().add("data-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label cellLabel(String text, boolean emphasis, double growFactor) {
        Label label = new Label(text);
        label.getStyleClass().add(emphasis ? "data-row-emphasis" : "data-row-text");
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setPrefWidth(80 * growFactor);
        return label;
    }

    private Node expiryIndicator(BloodBag bag) {
        long days = bag.daysUntilExpiry();
        int warningDays = context.getInventoryManager().getExpiryWarningDays();
        String text;
        String styleClass;
        if (bag.getStatus() == BloodBag.Status.EXPIRED) {
            text = "Expired " + Math.abs(days) + "d ago";
            styleClass = "expiry-label-danger";
        } else if (days <= 0) {
            text = "Expires today";
            styleClass = "expiry-label-danger";
        } else if (days <= warningDays) {
            text = "Expires in " + days + "d";
            styleClass = "expiry-label-warning";
        } else {
            text = "Expires in " + days + "d";
            styleClass = "expiry-label-neutral";
        }
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
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
        attachToOwner(dialog);
        dialog.setTitle("Add Blood Bag");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        ComboBox<Donor> donorBox = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(eligibleDonors));
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

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 0, 0));
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
            appShell.refreshCurrent();
        });
    }

    private Label formLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }
}
