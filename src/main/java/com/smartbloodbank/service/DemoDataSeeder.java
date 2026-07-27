package com.smartbloodbank.service;

import com.smartbloodbank.model.BloodBag;
import com.smartbloodbank.model.BloodType;
import com.smartbloodbank.model.Donor;
import com.smartbloodbank.model.EmergencyLevel;
import com.smartbloodbank.model.Patient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Populates a fresh {@link BloodBank} with sample donors, patients and blood
 * bags so the app has data to demo immediately, instead of opening on an
 * empty inventory. Only meant to run once, on a brand-new install where no
 * saved data file exists yet — see {@code AppContext#loadData()}.
 */
public class DemoDataSeeder {

    private record DonorSeed(String name, String phone, BloodType type) {
    }

    private record PatientSeed(String name, String phone, BloodType type, EmergencyLevel level,
                                int units, String ward) {
    }

    private record BagSeed(int donorIndex, BloodType type, int daysAgoDonated) {
    }

    private static final List<DonorSeed> DONOR_SEEDS = List.of(
            new DonorSeed("Ahmed Hassan", "010-111-2222", BloodType.O_NEGATIVE),
            new DonorSeed("Sarah Mitchell", "010-222-3333", BloodType.A_POSITIVE),
            new DonorSeed("Youssef Ibrahim", "010-333-4444", BloodType.B_POSITIVE),
            new DonorSeed("Maria Gonzalez", "010-444-5555", BloodType.AB_POSITIVE),
            new DonorSeed("Daniel Osei", "010-555-6666", BloodType.O_POSITIVE),
            new DonorSeed("Fatima Al-Sayed", "010-666-7777", BloodType.A_NEGATIVE),
            new DonorSeed("James O'Connor", "010-777-8888", BloodType.B_NEGATIVE),
            new DonorSeed("Layla Karimi", "010-888-9999", BloodType.AB_NEGATIVE),
            new DonorSeed("Kevin Zhang", "010-999-0000", BloodType.O_POSITIVE),
            new DonorSeed("Amara Okafor", "010-000-1111", BloodType.A_POSITIVE),
            new DonorSeed("Miriam Costa", "010-101-1212", BloodType.B_POSITIVE),
            new DonorSeed("Tariq Farouk", "010-101-2323", BloodType.AB_POSITIVE),
            new DonorSeed("Elena Volkov", "010-101-3434", BloodType.A_NEGATIVE),
            new DonorSeed("Chinedu Obi", "010-101-4545", BloodType.B_NEGATIVE)
    );

    private static final List<PatientSeed> PATIENT_SEEDS = List.of(
            new PatientSeed("Omar Abdallah", "011-111-2222", BloodType.O_NEGATIVE, EmergencyLevel.CRITICAL, 4, "ICU-1"),
            new PatientSeed("Emily Carter", "011-222-3333", BloodType.A_POSITIVE, EmergencyLevel.HIGH, 2, "Surgery-3"),
            new PatientSeed("Hassan Malik", "011-333-4444", BloodType.B_POSITIVE, EmergencyLevel.MEDIUM, 1, "Ward-5"),
            new PatientSeed("Sofia Rossi", "011-444-5555", BloodType.AB_POSITIVE, EmergencyLevel.LOW, 1, "Ward-2"),
            new PatientSeed("Chidi Eze", "011-555-6666", BloodType.O_POSITIVE, EmergencyLevel.CRITICAL, 3, "ICU-2"),
            new PatientSeed("Nadia Petrov", "011-666-7777", BloodType.A_NEGATIVE, EmergencyLevel.HIGH, 2, "Surgery-1"),
            new PatientSeed("Liam Walsh", "011-777-8888", BloodType.B_NEGATIVE, EmergencyLevel.MEDIUM, 1, "Ward-7"),
            new PatientSeed("Aisha Rahman", "011-888-9999", BloodType.AB_NEGATIVE, EmergencyLevel.LOW, 1, "Ward-4"),
            new PatientSeed("Marcus Lee", "011-999-0000", BloodType.O_POSITIVE, EmergencyLevel.HIGH, 2, "Surgery-2"),
            new PatientSeed("Grace Adeyemi", "011-000-1111", BloodType.A_POSITIVE, EmergencyLevel.CRITICAL, 3, "ICU-3")
    );

    /**
     * Donation dates listed oldest-first per donor so each donor's
     * {@code donationHistory} stays in chronological order, and — where a
     * donor has more than one entry — spaced at least 90 days apart, same
     * as the real {@code isEligibleToDonate()} rule would require of an
     * actual person. Donors 2, 3, 5 and 6 are given a last donation over
     * 90 days ago so they show up as eligible to donate again; the
     * resulting bag is correspondingly old enough to have expired, which
     * also gives the Reports screen's "Expired Bags" metric something
     * real to show. Donors 10-13 exist because donors 2, 3, 5 and 6 are
     * the only ones of their blood type, so once their bag expires that
     * type would otherwise have zero available stock — a fresh donor
     * keeps every type represented in the AVAILABLE inventory. Several
     * entries land within 5 days of the 42-day shelf life across
     * different types (O+, A+, B+, A-) so the expiry alert has real,
     * varied data to demonstrate on.
     */
    private static final List<BagSeed> BAG_SEEDS = List.of(
            new BagSeed(0, BloodType.O_NEGATIVE, 100),
            new BagSeed(0, BloodType.O_NEGATIVE, 5),
            new BagSeed(1, BloodType.A_POSITIVE, 10),
            new BagSeed(2, BloodType.B_POSITIVE, 95),
            new BagSeed(3, BloodType.AB_POSITIVE, 110),
            new BagSeed(4, BloodType.O_POSITIVE, 115),
            new BagSeed(4, BloodType.O_POSITIVE, 18),
            new BagSeed(5, BloodType.A_NEGATIVE, 130),
            new BagSeed(6, BloodType.B_NEGATIVE, 150),
            new BagSeed(7, BloodType.AB_NEGATIVE, 100),
            new BagSeed(7, BloodType.AB_NEGATIVE, 2),
            new BagSeed(8, BloodType.O_POSITIVE, 40),
            new BagSeed(9, BloodType.A_POSITIVE, 41),
            new BagSeed(10, BloodType.B_POSITIVE, 39),
            new BagSeed(11, BloodType.AB_POSITIVE, 14),
            new BagSeed(12, BloodType.A_NEGATIVE, 37),
            new BagSeed(13, BloodType.B_NEGATIVE, 28)
    );

    public void seed(BloodBank bloodBank) {
        List<Donor> donors = seedDonors(bloodBank);
        seedPatients(bloodBank);
        seedBloodBags(bloodBank, donors);
    }

    private List<Donor> seedDonors(BloodBank bloodBank) {
        List<Donor> donors = new ArrayList<>();
        for (DonorSeed seed : DONOR_SEEDS) {
            Donor donor = new Donor(seed.name(), seed.phone(), seed.type());
            bloodBank.addDonor(donor);
            donors.add(donor);
        }
        return donors;
    }

    private void seedPatients(BloodBank bloodBank) {
        for (PatientSeed seed : PATIENT_SEEDS) {
            Patient patient = new Patient(seed.name(), seed.phone(), seed.type(),
                    seed.level(), seed.units(), seed.ward());
            bloodBank.addPatient(patient);
        }
    }

    private void seedBloodBags(BloodBank bloodBank, List<Donor> donors) {
        LocalDate today = LocalDate.now();
        for (BagSeed seed : BAG_SEEDS) {
            Donor donor = donors.get(seed.donorIndex());
            LocalDate donationDate = today.minusDays(seed.daysAgoDonated());
            bloodBank.addBloodBag(new BloodBag(seed.type(), donor.getId(), donationDate));
            donor.recordDonation(donationDate);
        }
    }
}
