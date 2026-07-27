package com.smartbloodbank.service;

import com.smartbloodbank.model.BloodBag;
import com.smartbloodbank.model.BloodType;
import com.smartbloodbank.model.Donor;
import com.smartbloodbank.model.Patient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds every donor, patient and blood bag the app currently knows
 * about, all in memory while the app is running. Every other class that
 * needs this data goes through BloodBank instead of keeping its own
 * copy, so there's a single, consistent source of truth.
 *
 * ENCAPSULATION: the
 * ArrayList/HashMap fields are private — every other service class talks
 * to this data only through the methods below, never the collections
 * directly.
 *
 * Donors and patients are keyed by ID in a HashMap for fast lookup;
 * blood bags are kept in an ArrayList since they are most often scanned
 * as a whole (by type, by status, by expiry) rather than fetched by ID.
 */
public class BloodBank {

    private final Map<String, Donor> donors = new HashMap<>();
    private final Map<String, Patient> patients = new HashMap<>();
    private final List<BloodBag> bloodBags = new ArrayList<>();

    // ---------- Donors ----------

    /** Registers a donor so the rest of the app can find them by ID. */
    public void addDonor(Donor donor) {
        donors.put(donor.getId(), donor);
    }

    /** Removes a donor by ID; returns true if a donor was actually removed. */
    public boolean removeDonor(String donorId) {
        return donors.remove(donorId) != null;
    }

    /** Looks up a single donor by ID, or null if there's no such donor. */
    public Donor getDonor(String donorId) {
        return donors.get(donorId);
    }

    /** Returns every registered donor as a new list (safe to modify without affecting BloodBank). */
    public List<Donor> getAllDonors() {
        return new ArrayList<>(donors.values());
    }

    // ---------- Patients ----------

    /** Registers a patient so the rest of the app can find them by ID. */
    public void addPatient(Patient patient) {
        patients.put(patient.getId(), patient);
    }

    /** Removes a patient by ID; returns true if a patient was actually removed. */
    public boolean removePatient(String patientId) {
        return patients.remove(patientId) != null;
    }

    /** Looks up a single patient by ID, or null if there's no such patient. */
    public Patient getPatient(String patientId) {
        return patients.get(patientId);
    }

    /** Returns every registered patient as a new list (safe to modify without affecting BloodBank). */
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patients.values());
    }

    // ---------- Blood bags ----------

    /** Adds a newly collected (or reloaded) blood bag to the inventory. */
    public void addBloodBag(BloodBag bag) {
        bloodBags.add(bag);
    }

    /** Removes a blood bag by its bag ID; returns true if a bag was actually removed. */
    public boolean removeBloodBag(String bagId) {
        return bloodBags.removeIf(bag -> bag.getBagId().equals(bagId));
    }

    /** Looks up a single blood bag by its bag ID, or null if there's no such bag. */
    public BloodBag getBloodBag(String bagId) {
        for (BloodBag bag : bloodBags) {
            if (bag.getBagId().equals(bagId)) {
                return bag;
            }
        }
        return null;
    }

    /** Returns every blood bag on record, of any status, as a new list. */
    public List<BloodBag> getAllBloodBags() {
        return new ArrayList<>(bloodBags);
    }

    /** Returns every blood bag of a given blood type, regardless of status. */
    public List<BloodBag> getBloodBagsByType(BloodType type) {
        List<BloodBag> result = new ArrayList<>();
        for (BloodBag bag : bloodBags) {
            if (bag.getBloodType() == type) {
                result.add(bag);
            }
        }
        return result;
    }

    /** Returns every blood bag that is currently AVAILABLE and not expired — i.e. usable stock. */
    public List<BloodBag> getAvailableBloodBags() {
        List<BloodBag> result = new ArrayList<>();
        for (BloodBag bag : bloodBags) {
            if (bag.getStatus() == BloodBag.Status.AVAILABLE && !bag.isExpired()) {
                result.add(bag);
            }
        }
        return result;
    }

    /** Counts how many usable (available, non-expired) units of one blood type are in stock. */
    public int getAvailableUnitCount(BloodType type) {
        int count = 0;
        for (BloodBag bag : bloodBags) {
            if (bag.getBloodType() == type && bag.getStatus() == BloodBag.Status.AVAILABLE && !bag.isExpired()) {
                count++;
            }
        }
        return count;
    }

    /** Snapshot of how many available units are on hand for every blood type. */
    public Map<BloodType, Integer> getStockSummary() {
        Map<BloodType, Integer> summary = new HashMap<>();
        for (BloodType type : BloodType.values()) {
            summary.put(type, getAvailableUnitCount(type));
        }
        return summary;
    }
}
