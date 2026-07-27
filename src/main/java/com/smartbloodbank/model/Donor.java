package com.smartbloodbank.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a person who donates blood. On top of the shared name/phone/
 * blood-type details from User, it remembers every date this person has
 * donated on and enforces the real-world safety rule that a person must
 * wait at least 90 days between whole-blood donations.
 *
 * INHERITANCE: Donor extends the abstract User class.
 * Adds donor-specific state: donation history and eligibility rules
 * (a donor must wait at least 90 days between whole-blood donations).
 */
public class Donor extends User {

    private static final int MIN_DAYS_BETWEEN_DONATIONS = 90;

    private final List<LocalDate> donationHistory = new ArrayList<>();

    /** Registers a brand-new donor who hasn't donated yet. */
    public Donor(String fullName, String contactNumber, BloodType bloodType) {
        super(fullName, contactNumber, bloodType);
    }

    /** Rebuilds a donor from a previously saved file, reusing their original ID and registration date. */
    public Donor(String id, String fullName, String contactNumber, BloodType bloodType, LocalDate registrationDate) {
        super(id, fullName, contactNumber, bloodType, registrationDate);
    }

    /** Adds a new donation date to this donor's history (called when a blood bag is collected from them). */
    public void recordDonation(LocalDate date) {
        donationHistory.add(date);
    }

    /** Returns every donation date on record for this donor, oldest first, as a read-only list. */
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

    /** How many days are left before this donor is eligible again; 0 if they already are. */
    public long daysUntilEligible() {
        if (donationHistory.isEmpty()) return 0;
        LocalDate lastDonation = donationHistory.get(donationHistory.size() - 1);
        long daysSince = ChronoUnit.DAYS.between(lastDonation, LocalDate.now());
        return Math.max(0, MIN_DAYS_BETWEEN_DONATIONS - daysSince);
    }

    // ---------- Polymorphism: overriding abstract methods from User ----------

    /** Returns "Donor" so shared code (tables, reports) can label this person's role. */
    @Override
    public String getRoleDescription() {
        return "Donor";
    }

    /** Builds this donor's one-line summary for tables and reports, including donation count and eligibility. */
    @Override
    public String displayInfo() {
        return baseInfo() + String.format(" | Total Donations: %d | Eligible Now: %s",
                getTotalDonations(), isEligibleToDonate() ? "Yes" : "No (" + daysUntilEligible() + " days left)");
    }
}
