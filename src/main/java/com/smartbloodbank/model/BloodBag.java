package com.smartbloodbank.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents one physical bag of donated blood, from the day it's
 * collected until it's used or thrown away. It knows its own blood type,
 * which donor it came from, when it expires, and what state it's
 * currently in (available, reserved, used, or expired).
 *
 * ENCAPSULATION: all fields private, status transitions only happen
 * through controlled methods (reserve(), markUsed(), markExpired())
 * instead of a public setter — this protects the object from being put
 * into an invalid state from outside code.
 */
public class BloodBag {

    public enum Status { AVAILABLE, RESERVED, USED, EXPIRED }

    private static int nextBagId = 1;
    /** Whole blood is typically usable for ~42 days after donation. */
    private static final int SHELF_LIFE_DAYS = 42;

    private final String bagId;
    private final BloodType bloodType;
    private final String donorId;
    private final LocalDate donationDate;
    private final LocalDate expirationDate;
    private Status status;

    /** Creates a brand-new bag with a fresh auto-generated ID, AVAILABLE status, and an expiry date 42 days out. */
    public BloodBag(BloodType bloodType, String donorId, LocalDate donationDate) {
        this.bagId = "BB" + String.format("%04d", nextBagId++);
        this.bloodType = bloodType;
        this.donorId = donorId;
        this.donationDate = donationDate;
        this.expirationDate = donationDate.plusDays(SHELF_LIFE_DAYS);
        this.status = Status.AVAILABLE;
    }

    /** Constructor used when loading an existing bag back from a file. */
    public BloodBag(String bagId, BloodType bloodType, String donorId, LocalDate donationDate,
                     LocalDate expirationDate, Status status) {
        this.bagId = bagId;
        this.bloodType = bloodType;
        this.donorId = donorId;
        this.donationDate = donationDate;
        this.expirationDate = expirationDate;
        this.status = status;
    }

    public String getBagId() {
        return bagId;
    }

    public BloodType getBloodType() {
        return bloodType;
    }

    public String getDonorId() {
        return donorId;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public Status getStatus() {
        return status;
    }

    /** Checks whether today's date is past this bag's expiration date. */
    public boolean isExpired() {
        return LocalDate.now().isAfter(expirationDate);
    }

    /** How many days are left until this bag expires (negative if it already has). */
    public long daysUntilExpiry() {
        return ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
    }

    /** Sets this bag aside for a specific patient; only allowed while it's still AVAILABLE. */
    public void reserve() {
        if (status != Status.AVAILABLE) {
            throw new IllegalStateException("Only an AVAILABLE bag can be reserved.");
        }
        status = Status.RESERVED;
    }

    /** Marks a reserved bag as given to a patient; only allowed while it's RESERVED. */
    public void markUsed() {
        if (status != Status.RESERVED) {
            throw new IllegalStateException("Only a RESERVED bag can be marked as used.");
        }
        status = Status.USED;
    }

    /** Marks this bag as expired, e.g. once its shelf life has passed without being used. */
    public void markExpired() {
        status = Status.EXPIRED;
    }

    /**
     * Keeps the auto-increment bag ID counter ahead of any ID reloaded from
     * a file, so bags created afterwards never collide with reloaded ones.
     */
    public static void syncBagIdCounter(String existingBagId) {
        try {
            int numericPart = Integer.parseInt(existingBagId.substring(2));
            if (numericPart >= nextBagId) {
                nextBagId = numericPart + 1;
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
            // Malformed ID — leave the counter untouched.
        }
    }

    /** Formats this bag as a single readable line, used for quick debugging/console output. */
    @Override
    public String toString() {
        return String.format("[%s] %s | Donor: %s | Donated: %s | Expires: %s | Status: %s",
                bagId, bloodType, donorId, donationDate, expirationDate, status);
    }
}
