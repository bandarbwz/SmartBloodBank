package com.smartbloodbank.model;

import java.time.LocalDate;

/**
 * Represents a patient who needs blood. On top of the shared name/phone/
 * blood-type details from User, it tracks how urgent their need is, how
 * many units they need, which ward they're in, and whether their request
 * has been fulfilled yet.
 *
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

    /** Registers a brand-new patient whose request has not been fulfilled yet. */
    public Patient(String fullName, String contactNumber, BloodType bloodType,
                    EmergencyLevel emergencyLevel, int unitsRequired, String wardNumber) {
        super(fullName, contactNumber, bloodType);
        this.emergencyLevel = emergencyLevel;
        setUnitsRequired(unitsRequired);
        this.wardNumber = wardNumber;
        this.fulfilled = false;
    }

    /** Rebuilds a patient from a previously saved file, reusing their original ID, registration date and status. */
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

    /** Changes how many units this patient needs; rejects zero or negative values. */
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

    /** Marks this patient's request as fulfilled once their blood units have been reserved and given. */
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

    /** Returns "Patient" so shared code (tables, reports) can label this person's role. */
    @Override
    public String getRoleDescription() {
        return "Patient";
    }

    /** Builds this patient's one-line summary for tables and reports, including ward, units and status. */
    @Override
    public String displayInfo() {
        return baseInfo() + String.format(" | Ward: %s | Units Needed: %d | Priority: %s | Status: %s",
                wardNumber, unitsRequired, emergencyLevel.getDescription(),
                fulfilled ? "Fulfilled" : "Pending");
    }
}
