package com.smartbloodbank.service;

import com.smartbloodbank.model.BloodBag;
import com.smartbloodbank.model.Patient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Keeps a waiting line of patients who still need blood, always ready to
 * say who should be helped next. Patients don't need to be sorted by
 * hand — the queue automatically keeps the most urgent patient at the
 * front.
 *
 * Queues pending patient requests and works through them in urgency order.
 * ABSTRACTION reuse: ordering comes for free from {@link Patient#compareTo},
 * so this class needs no urgency-comparison logic of its own — a
 * PriorityQueue of Patient objects sorts itself.
 */
public class EmergencyRequest {

    private final Queue<Patient> pendingRequests = new PriorityQueue<>();
    private final BloodMatcher bloodMatcher;

    /** Wires this queue up to the BloodMatcher it should use to check/reserve stock when fulfilling requests. */
    public EmergencyRequest(BloodMatcher bloodMatcher) {
        this.bloodMatcher = bloodMatcher;
    }

    /** Adds a patient to the waiting line, unless their request is already fulfilled. */
    public void submitRequest(Patient patient) {
        if (patient.isFulfilled()) {
            return;
        }
        pendingRequests.add(patient);
    }

    /** Takes a patient out of the waiting line (e.g. if their request is cancelled); returns true if they were in it. */
    public boolean removeRequest(Patient patient) {
        return pendingRequests.remove(patient);
    }

    /** Looks at the most urgent pending patient without removing them from the queue. */
    public Patient peekNextRequest() {
        return pendingRequests.peek();
    }

    /** How many patients are currently waiting. */
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
