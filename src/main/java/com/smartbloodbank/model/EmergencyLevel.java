package com.smartbloodbank.model;

/**
 * How urgent a patient's blood request is, from LOW ("Routine") up to
 * CRITICAL ("Life-threatening"). Patients are sorted so the most urgent
 * ones are handled first, the same way a hospital would triage them.
 *
 * The integer priority is used by BloodMatcher / EmergencyRequest
 * to sort requests (higher number = handled first).
 */
public enum EmergencyLevel {
    LOW(1, "Routine"),
    MEDIUM(2, "Urgent"),
    HIGH(3, "Critical"),
    CRITICAL(4, "Life-threatening");

    private final int priority;
    private final String description;

    EmergencyLevel(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }
}
