package com.smartbloodbank.model;

import java.time.LocalDate;

/**
 * INHERITANCE: Patient extends the abstract User class, same as Donor —
 * this is the shared hierarchy required by the project brief:
 *
 *        User
 *        ├── Donor
 *        └── Patient
 *
 * ABSTRACTION (via interface): Patient implements Comparable<Patient> so
 * that a list/priority-queue of patients can be sorted automatically by
 * urgency without any external if/else sorting logic — the object knows
 * how to compare itself to another of its own kind.
 */
public class Patient extends User implements Comparable<Patient> {

    private EmergencyLevel emergencyLevel;
    private int unitsRequired;
    private String wardNumber;
    private boolean fulfilled;

    public Patient(String fullName, String contactNumber, BloodType bloodType,
                    EmergencyLevel emergencyLevel, int unitsRequired, String wardNumber) {
        super(fullName, contactNumber, bloodType);
        this.emergencyLevel = emergencyLevel;
        setUnitsRequired(unitsRequired);
        this.wardNumber = wardNumber;
        this.fulfilled = false;
    }

    public Patient(String id, String fullName, String contactNumber, BloodType bloodType, LocalDate registrationDate,
                    EmergencyLevel emergencyLevel, int unitsRequired, String wardNumber, boolean fulfilled) {
        super(id, fullName, contactNumber, bloodType, registrationDate);
        this.emergencyLevel = emergencyLevel;
        setUnitsRequired(unitsRequired);
        this.wardNumber = wardNumber;
        this.fulfilled = fulfilled;
    }

    // ---------- Getters & Setters (Encapsulation) ----------

    public EmergencyLevel getEmergencyLevel() {
        return emergencyLevel;
    }

    public void setEmergencyLevel(EmergencyLevel emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
    }

    public int getUnitsRequired() {
        return unitsRequired;
    }

    public void setUnitsRequired(int unitsRequired) {
        if (unitsRequired <= 0) {
            throw new IllegalArgumentException("Units required must be positive.");
        }
        this.unitsRequired = unitsRequired;
    }

    public String getWardNumber() {
        return wardNumber;
    }

    public void setWardNumber(String wardNumber) {
        this.wardNumber = wardNumber;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void markFulfilled() {
        this.fulfilled = true;
    }

    // ---------- Comparable (interface-based abstraction) ----------

    /**
     * Higher emergency priority comes first. If two patients share the
     * same urgency, the one who registered earlier (FIFO) goes first —
     * mirrors real triage behaviour.
     */
    @Override
    public int compareTo(Patient other) {
        int byUrgency = Integer.compare(other.emergencyLevel.getPriority(), this.emergencyLevel.getPriority());
        if (byUrgency != 0) return byUrgency;
        return this.getRegistrationDate().compareTo(other.getRegistrationDate());
    }

    // ---------- Polymorphism: overriding abstract methods from User ----------

    @Override
    public String getRoleDescription() {
        return "Patient";
    }

    @Override
    public String displayInfo() {
        return baseInfo() + String.format(" | Ward: %s | Units Needed: %d | Priority: %s | Status: %s",
                wardNumber, unitsRequired, emergencyLevel.getDescription(),
                fulfilled ? "Fulfilled" : "Pending");
    }
}
