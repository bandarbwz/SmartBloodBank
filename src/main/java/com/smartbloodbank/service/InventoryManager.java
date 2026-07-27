package com.smartbloodbank.service;

import com.smartbloodbank.model.BloodBag;
import com.smartbloodbank.model.BloodType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Keeps an eye on the blood stock and warns when something needs
 * attention — either a blood type is running low, or some bags are
 * about to expire. It doesn't store the stock itself; it just reads
 * from BloodBank and reports on it.
 *
 * Watches the stock held in a {@link BloodBank} and raises low-stock and
 * near-expiry alerts. Kept separate from BloodBank itself so the storage
 * class stays a plain data holder and this class owns the business rules
 * (single-responsibility, easy to unit test independently).
 */
public class InventoryManager {

    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int EXPIRY_WARNING_DAYS = 7;

    private final BloodBank bloodBank;

    /** Wires this manager up to the BloodBank whose stock it should watch. */
    public InventoryManager(BloodBank bloodBank) {
        this.bloodBank = bloodBank;
    }

    /** The unit count below which a blood type is considered low stock. */
    public int getLowStockThreshold() {
        return LOW_STOCK_THRESHOLD;
    }

    /** How many days before expiry a bag starts showing up in the "expiring soon" alerts. */
    public int getExpiryWarningDays() {
        return EXPIRY_WARNING_DAYS;
    }

    /**
     * Moves any AVAILABLE bag whose shelf life has passed into EXPIRED
     * status. Should be called periodically (e.g. on app startup and
     * before generating reports) so stock counts stay accurate.
     *
     * @return how many bags were newly marked expired
     */
    public int updateExpiredBags() {
        int updated = 0;
        for (BloodBag bag : bloodBank.getAllBloodBags()) {
            if (bag.getStatus() == BloodBag.Status.AVAILABLE && bag.isExpired()) {
                bag.markExpired();
                updated++;
            }
        }
        return updated;
    }

    /** Lists every blood type whose available stock is below the low-stock threshold. */
    public List<BloodType> getLowStockTypes() {
        List<BloodType> lowStock = new ArrayList<>();
        for (BloodType type : BloodType.values()) {
            if (bloodBank.getAvailableUnitCount(type) < LOW_STOCK_THRESHOLD) {
                lowStock.add(type);
            }
        }
        return lowStock;
    }

    /** Builds a human-readable warning message for each blood type that is running low. */
    public List<String> getLowStockAlerts() {
        List<String> alerts = new ArrayList<>();
        for (BloodType type : getLowStockTypes()) {
            alerts.add(String.format("LOW STOCK: %s has only %d unit(s) available (threshold: %d).",
                    type, bloodBank.getAvailableUnitCount(type), LOW_STOCK_THRESHOLD));
        }
        return alerts;
    }

    /** AVAILABLE, non-expired bags expiring within {@link #EXPIRY_WARNING_DAYS} days, soonest first. */
    public List<BloodBag> getBagsNearingExpiry() {
        List<BloodBag> nearingExpiry = new ArrayList<>();
        for (BloodBag bag : bloodBank.getAllBloodBags()) {
            if (bag.getStatus() == BloodBag.Status.AVAILABLE && !bag.isExpired()
                    && bag.daysUntilExpiry() <= EXPIRY_WARNING_DAYS) {
                nearingExpiry.add(bag);
            }
        }
        nearingExpiry.sort(Comparator.comparing(BloodBag::getExpirationDate));
        return nearingExpiry;
    }

    /** Builds a human-readable warning message for each bag that's about to expire. */
    public List<String> getExpiryAlerts() {
        List<String> alerts = new ArrayList<>();
        for (BloodBag bag : getBagsNearingExpiry()) {
            alerts.add(String.format("EXPIRING SOON: Bag %s (%s) expires in %d day(s).",
                    bag.getBagId(), bag.getBloodType(), bag.daysUntilExpiry()));
        }
        return alerts;
    }

    /** All low-stock alerts followed by all expiry alerts. */
    public List<String> getAllAlerts() {
        List<String> alerts = new ArrayList<>();
        alerts.addAll(getLowStockAlerts());
        alerts.addAll(getExpiryAlerts());
        return alerts;
    }
}
