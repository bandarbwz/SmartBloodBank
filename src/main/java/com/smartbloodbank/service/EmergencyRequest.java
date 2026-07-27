package com.smartbloodbank.service;

import com.smartbloodbank.model.BloodBag;
import com.smartbloodbank.model.Patient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Queues pending patient requests and works through them in urgency order.
 * ABSTRACTION reuse: ordering comes for free from {@link Patient#compareTo},
 * so this class needs no urgency-comparison logic of its own — a
 * PriorityQueue of Patient objects sorts itself.
 */
public class EmergencyRequest {

    private final Queue<Patient> pendingRequests = new PriorityQueue<>();
    private final BloodMatcher bloodMatcher;

    public EmergencyRequest(BloodMatcher bloodMatcher) {
        this.bloodMatcher = bloodMatcher;
    }

    public void submitRequest(Patient patient) {
        if (patient.isFulfilled()) {
            return;
        }
        pendingRequests.add(patient);
    }

    public boolean removeRequest(Patient patient) {
        return pendingRequests.remove(patient);
    }

    public Patient peekNextRequest() {
        return pendingRequests.peek();
    }

    public int getPendingCount() {
        return pendingRequests.size();
    }

    /** All pending requests ordered by urgency (highest priority first), without consuming the queue. */
    public List<Patient> getPendingRequestsSorted() {
        List<Patient> sorted = new ArrayList<>(pendingRequests);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Attempts to fulfill the highest-priority pending request.
     *
     * @return the patient that was fulfilled, or {@code null} if the queue
     *         is empty or the top request cannot yet be fulfilled (stock is
     *         checked before the request is removed, so it stays queued
     *         until enough compatible stock becomes available)
     */
    public Patient processNextRequest() {
        Patient next = pendingRequests.peek();
        if (next == null || !bloodMatcher.canFulfill(next.getBloodType(), next.getUnitsRequired())) {
            return null;
        }
        pendingRequests.poll();
        List<BloodBag> reserved = bloodMatcher.matchAndReserve(next);
        for (BloodBag bag : reserved) {
            bag.markUsed();
        }
        next.markFulfilled();
        return next;
    }
}
