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
 * Central in-memory store for the whole system. ENCAPSULATION: the
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

    public void addDonor(Donor donor) {
        donors.put(donor.getId(), donor);
    }

    public boolean removeDonor(String donorId) {
        return donors.remove(donorId) != null;
    }

    public Donor getDonor(String donorId) {
        return donors.get(donorId);
    }

    public List<Donor> getAllDonors() {
        return new ArrayList<>(donors.values());
    }

    // ---------- Patients ----------

    public void addPatient(Patient patient) {
        patients.put(patient.getId(), patient);
    }

    public boolean removePatient(String patientId) {
        return patients.remove(patientId) != null;
    }

    public Patient getPatient(String patientId) {
        return patients.get(patientId);
    }

    public List<Patient> getAllPatients() {
        return new ArrayList<>(patients.values());
    }

    // ---------- Blood bags ----------

    public void addBloodBag(BloodBag bag) {
        bloodBags.add(bag);
    }

    public boolean removeBloodBag(String bagId) {
        return bloodBags.removeIf(bag -> bag.getBagId().equals(bagId));
    }

    public BloodBag getBloodBag(String bagId) {
        for (BloodBag bag : bloodBags) {
            if (bag.getBagId().equals(bagId)) {
                return bag;
            }
        }
        return null;
    }

    public List<BloodBag> getAllBloodBags() {
        return new ArrayList<>(bloodBags);
    }

    public List<BloodBag> getBloodBagsByType(BloodType type) {
        List<BloodBag> result = new ArrayList<>();
        for (BloodBag bag : bloodBags) {
            if (bag.getBloodType() == type) {
                result.add(bag);
            }
        }
        return result;
    }

    public List<BloodBag> getAvailableBloodBags() {
        List<BloodBag> result = new ArrayList<>();
        for (BloodBag bag : bloodBags) {
            if (bag.getStatus() == BloodBag.Status.AVAILABLE && !bag.isExpired()) {
                result.add(bag);
            }
        }
        return result;
    }

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
