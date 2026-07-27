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
 * Sets up and holds one shared copy of every service class (BloodBank,
 * InventoryManager, etc.) for as long as the app is running, and is
 * responsible for loading data in when the app starts and saving it
 * back out when it closes. Every screen is given this same AppContext
 * so they're all reading and writing the same underlying data.
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

    /**
     * Loads saved donors/patients/blood bags from disk (via FileManager), or —
     * if this is a brand-new install with nothing saved yet — fills the app
     * with sample data (via DemoDataSeeder) instead. Also refreshes expired
     * bags and re-queues any unfulfilled patients into the emergency request
     * line, so everything is consistent as soon as the app opens.
     */
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

    /** Writes the current donors/patients/blood bags out to disk (via FileManager), e.g. on app exit or Log Out. */
    public void saveData() {
        try {
            fileManager.saveAll(bloodBank);
        } catch (IOException e) {
            System.err.println("Could not save data: " + e.getMessage());
        }
    }
}
