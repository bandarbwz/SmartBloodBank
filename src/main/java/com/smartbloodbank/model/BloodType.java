package com.smartbloodbank.model;

import java.util.Arrays;
import java.util.List;

/**
 * The 8 standard human blood types (O-, O+, A-, A+, B-, B+, AB-, AB+),
 * plus the real-world rules for which types can safely donate to which.
 * Any code that needs to check compatibility asks this enum instead of
 * repeating the rules itself, so there's only one place to get them right.
 *
 * Keeping the compatibility rules INSIDE the enum (instead of scattering
 * if/else chains across the codebase) is a small but deliberate OOP design
 * choice: it centralizes domain knowledge in one place.
 */
public enum BloodType {
    O_NEGATIVE("O-"),
    O_POSITIVE("O+"),
    A_NEGATIVE("A-"),
    A_POSITIVE("A+"),
    B_NEGATIVE("B-"),
    B_POSITIVE("B+"),
    AB_NEGATIVE("AB-"),
    AB_POSITIVE("AB+");

    private final String label;

    BloodType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Returns true if blood from "this" donor type can be safely
     * transfused into a patient of the given recipient type.
     */
    public boolean canDonateTo(BloodType recipient) {
        return switch (this) {
            case O_NEGATIVE -> true; // universal donor
            case O_POSITIVE -> List.of(O_POSITIVE, A_POSITIVE, B_POSITIVE, AB_POSITIVE).contains(recipient);
            case A_NEGATIVE -> List.of(A_NEGATIVE, A_POSITIVE, AB_NEGATIVE, AB_POSITIVE).contains(recipient);
            case A_POSITIVE -> List.of(A_POSITIVE, AB_POSITIVE).contains(recipient);
            case B_NEGATIVE -> List.of(B_NEGATIVE, B_POSITIVE, AB_NEGATIVE, AB_POSITIVE).contains(recipient);
            case B_POSITIVE -> List.of(B_POSITIVE, AB_POSITIVE).contains(recipient);
            case AB_NEGATIVE -> List.of(AB_NEGATIVE, AB_POSITIVE).contains(recipient);
            case AB_POSITIVE -> recipient == AB_POSITIVE; // can only give to AB+
        };
    }

    /** Parses labels like "O+" or "AB-" back into the enum constant. */
    public static BloodType fromLabel(String label) {
        return Arrays.stream(values())
                .filter(bt -> bt.label.equalsIgnoreCase(label.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown blood type: " + label));
    }

    /** Displays the short label (e.g. "O+") instead of the raw enum constant name. */
    @Override
    public String toString() {
        return label;
    }
}
