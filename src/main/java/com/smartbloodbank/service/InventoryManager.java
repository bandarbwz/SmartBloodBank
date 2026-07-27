package com.smartbloodbank.service;

import com.smartbloodbank.model.BloodBag;
import com.smartbloodbank.model.BloodType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Watches the stock held in a {@link BloodBank} and raises low-stock and
 * near-expiry alerts. Kept separate from BloodBank itself so the storage
 * class stays a plain data holder and this class owns the business rules
 * (single-responsibility, easy to unit test independently).
 */
public class InventoryManager {

    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int EXPIRY_WARNING_DAYS = 7;

    private final BloodBank bloodBank;

    public InventoryManager(BloodBank bloodBank) {
        this.bloodBank = bloodBank;
    }

    public int getLowStockThreshold() {
        return LOW_STOCK_THRESHOLD;
    }

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

    public List<BloodType> getLowStockTypes() {
        List<BloodType> lowStock = new ArrayList<>();
        for (BloodType type : BloodType.values()) {
            if (bloodBank.getAvailableUnitCount(type) < LOW_STOCK_THRESHOLD) {
                lowStock.add(type);
            }
        }
        return lowStock;
    }

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
