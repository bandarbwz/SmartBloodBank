package com.smartbloodbank.service;

import com.smartbloodbank.model.BloodBag;
import com.smartbloodbank.model.BloodType;
import com.smartbloodbank.model.Patient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Figures out which blood bags in stock can safely go to a given
 * patient, and reserves them when a request is fulfilled. It always
 * offers the stock closest to expiring first, so nothing goes to waste
 * that didn't need to.
 *
 * Finds compatible donor blood for a patient. Reuses the donor/recipient
 * rules already centralized in {@link BloodType#canDonateTo}, so this
 * class only decides ordering and stock selection, not the medical rules
 * themselves.
 */
public class BloodMatcher {

    private final BloodBank bloodBank;

    /** Wires this matcher up to the BloodBank it should search for stock in. */
    public BloodMatcher(BloodBank bloodBank) {
        this.bloodBank = bloodBank;
    }

    /**
     * All AVAILABLE, non-expired bags whose blood type can be safely given
     * to a patient of the given type, ordered closest-to-expiry first
     * (FIFO) so the oldest usable stock is always offered before newer
     * stock.
     */
    public List<BloodBag> findCompatibleBags(BloodType recipientType) {
        List<BloodBag> matches = new ArrayList<>();
        for (BloodBag bag : bloodBank.getAvailableBloodBags()) {
            if (bag.getBloodType().canDonateTo(recipientType)) {
                matches.add(bag);
            }
        }
        matches.sort(Comparator.comparing(BloodBag::getExpirationDate));
        return matches;
    }

    /** Checks whether there's enough compatible stock on hand to cover the units needed, without reserving anything. */
    public boolean canFulfill(BloodType recipientType, int unitsNeeded) {
        return findCompatibleBags(recipientType).size() >= unitsNeeded;
    }

    /**
     * Reserves the bags closest to expiry that satisfy the patient's
     * required units.
     *
     * @throws IllegalStateException if there is not enough compatible stock
     */
    public List<BloodBag> matchAndReserve(Patient patient) {
        int unitsNeeded = patient.getUnitsRequired();
        List<BloodBag> candidates = findCompatibleBags(patient.getBloodType());
        if (candidates.size() < unitsNeeded) {
            throw new IllegalStateException("Not enough compatible stock for patient " + patient.getId()
                    + ": need " + unitsNeeded + ", have " + candidates.size() + ".");
        }
        List<BloodBag> reserved = new ArrayList<>();
        for (int i = 0; i < unitsNeeded; i++) {
            BloodBag bag = candidates.get(i);
            bag.reserve();
            reserved.add(bag);
        }
        return reserved;
    }
}
