package com.smartbloodbank.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * INHERITANCE: Donor extends the abstract User class.
 * Adds donor-specific state: donation history and eligibility rules
 * (a donor must wait at least 90 days between whole-blood donations).
 */
public class Donor extends User {

    private static final int MIN_DAYS_BETWEEN_DONATIONS = 90;

    private final List<LocalDate> donationHistory = new ArrayList<>();

    public Donor(String fullName, String contactNumber, BloodType bloodType) {
        super(fullName, contactNumber, bloodType);
    }

    public Donor(String id, String fullName, String contactNumber, BloodType bloodType, LocalDate registrationDate) {
        super(id, fullName, contactNumber, bloodType, registrationDate);
    }

    public void recordDonation(LocalDate date) {
        donationHistory.add(date);
    }

    public List<LocalDate> getDonationHistory() {
        return Collections.unmodifiableList(donationHistory);
    }

    public int getTotalDonations() {
        return donationHistory.size();
    }

    /** Business rule: is this donor allowed to donate again today? */
    public boolean isEligibleToDonate() {
        if (donationHistory.isEmpty()) {
            return true;
        }
        LocalDate lastDonation = donationHistory.get(donationHistory.size() - 1);
        return ChronoUnit.DAYS.between(lastDonation, LocalDate.now()) >= MIN_DAYS_BETWEEN_DONATIONS;
    }

    public long daysUntilEligible() {
        if (donationHistory.isEmpty()) return 0;
        LocalDate lastDonation = donationHistory.get(donationHistory.size() - 1);
        long daysSince = ChronoUnit.DAYS.between(lastDonation, LocalDate.now());
        return Math.max(0, MIN_DAYS_BETWEEN_DONATIONS - daysSince);
    }

    // ---------- Polymorphism: overriding abstract methods from User ----------

    @Override
    public String getRoleDescription() {
        return "Donor";
    }

    @Override
    public String displayInfo() {
        return baseInfo() + String.format(" | Total Donations: %d | Eligible Now: %s",
                getTotalDonations(), isEligibleToDonate() ? "Yes" : "No (" + daysUntilEligible() + " days left)");
    }
}
