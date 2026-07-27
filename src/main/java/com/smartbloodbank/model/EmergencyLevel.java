package com.smartbloodbank.model;

/**
 * Priority level of a patient's blood request.
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
