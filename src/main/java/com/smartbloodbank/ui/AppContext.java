package com.smartbloodbank.ui;

import com.smartbloodbank.model.Patient;
import com.smartbloodbank.service.BloodBank;
import com.smartbloodbank.service.BloodMatcher;
import com.smartbloodbank.service.DemoDataSeeder;
import com.smartbloodbank.service.EmergencyRequest;
import com.smartbloodbank.service.FileManager;
import com.smartbloodbank.service.InventoryManager;

import java.io.IOException;

/**
 * Holds the single shared instance of every service class for the
 * lifetime of the application, and drives loading/saving through
 * FileManager. Passed down to every screen so they all operate on the
 * same underlying data.
 */
public class AppContext {

    private final BloodBank bloodBank = new BloodBank();
    private final InventoryManager inventoryManager = new InventoryManager(bloodBank);
    private final BloodMatcher bloodMatcher = new BloodMatcher(bloodBank);
    private final EmergencyRequest emergencyRequest = new EmergencyRequest(bloodMatcher);
    private final FileManager fileManager = new FileManager();

    public BloodBank getBloodBank() {
        return bloodBank;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public BloodMatcher getBloodMatcher() {
        return bloodMatcher;
    }

    public EmergencyRequest getEmergencyRequest() {
        return emergencyRequest;
    }

    public void loadData() {
        try {
            fileManager.loadAll(bloodBank);
        } catch (IOException e) {
            System.err.println("Could not load saved data: " + e.getMessage());
        }

        if (bloodBank.getAllDonors().isEmpty() && bloodBank.getAllPatients().isEmpty()) {
            new DemoDataSeeder().seed(bloodBank);
        }

        inventoryManager.updateExpiredBags();
        for (Patient patient : bloodBank.getAllPatients()) {
            if (!patient.isFulfilled()) {
                emergencyRequest.submitRequest(patient);
            }
        }
    }

    public void saveData() {
        try {
            fileManager.saveAll(bloodBank);
        } catch (IOException e) {
            System.err.println("Could not save data: " + e.getMessage());
        }
    }
}
