package com.smartbloodbank.model;

import java.time.LocalDate;

/**
 * Represents any person the system keeps track of. Right now that's
 * Donors and Patients, and this class holds the details every person has
 * in common — name, phone number, blood type, ID, registration date —
 * so that shared information and shared rules only live in one place.
 *
 * Abstract base class shared by every person known to the system
 * (Donor, Patient, and later Staff/Admin if needed).
 *
 * ABSTRACTION: this class can never be instantiated directly — only
 * meaningful subclasses can. displayInfo() and getRoleDescription()
 * are left abstract so each subclass MUST provide its own behaviour
 * (POLYMORPHISM via method overriding).
 *
 * ENCAPSULATION: every field is private; access is only through
 * getters/setters, and setters validate their input.
 */
public abstract class User {

    private static int nextId = 1000;

    private final String id;
    private String fullName;
    private String contactNumber;
    private BloodType bloodType;
    private final LocalDate registrationDate;

    /** Creates a brand-new person with a fresh auto-generated ID and today's date as the registration date. */
    protected User(String fullName, String contactNumber, BloodType bloodType) {
        this.id = "U" + (nextId++);
        setFullName(fullName);
        setContactNumber(contactNumber);
        this.bloodType = bloodType;
        this.registrationDate = LocalDate.now();
    }

    /** Used when reloading a user from a saved file, where the ID already exists. */
    protected User(String id, String fullName, String contactNumber, BloodType bloodType, LocalDate registrationDate) {
        this.id = id;
        setFullName(fullName);
        setContactNumber(contactNumber);
        this.bloodType = bloodType;
        this.registrationDate = registrationDate;
    }

    // ---------- Getters & Setters (Encapsulation) ----------

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    /** Changes the person's name; rejects empty or blank input. */
    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        this.fullName = fullName.trim();
    }

    public String getContactNumber() {
        return contactNumber;
    }

    /** Changes the person's phone number; rejects empty or blank input. */
    public void setContactNumber(String contactNumber) {
        if (contactNumber == null || contactNumber.isBlank()) {
            throw new IllegalArgumentException("Contact number cannot be empty.");
        }
        this.contactNumber = contactNumber.trim();
    }

    public BloodType getBloodType() {
        return bloodType;
    }

    public void setBloodType(BloodType bloodType) {
        this.bloodType = bloodType;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    /**
     * Keeps the auto-increment ID counter ahead of any ID reloaded from a
     * file, so users created afterwards never collide with reloaded ones.
     */
    public static void syncIdCounter(String existingId) {
        try {
            int numericPart = Integer.parseInt(existingId.substring(1));
            if (numericPart >= nextId) {
                nextId = numericPart + 1;
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
            // Malformed ID — leave the counter untouched.
        }
    }

    // ---------- Abstract behaviour (Polymorphism) ----------

    /** Each subclass returns a human-readable role, e.g. "Donor" or "Patient". */
    public abstract String getRoleDescription();

    /** Each subclass formats its own summary line for tables/reports. */
    public abstract String displayInfo();

    /** Common line format that subclasses can reuse inside displayInfo(). */
    protected String baseInfo() {
        return String.format("[%s] %s | %s | Blood Type: %s | Registered: %s",
                id, fullName, getRoleDescription(), bloodType, registrationDate);
    }
}
