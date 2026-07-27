package com.smartbloodbank.service;

import com.smartbloodbank.model.BloodBag;
import com.smartbloodbank.model.BloodType;
import com.smartbloodbank.model.Donor;
import com.smartbloodbank.model.EmergencyLevel;
import com.smartbloodbank.model.Patient;
import com.smartbloodbank.model.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Saves and loads all BloodBank data to/from plain-text files so state
 * survives between runs. Each entity is written as one delimited line per
 * record; the "load" constructors already provided on the model classes
 * (which accept an existing ID and registration date) are reused to
 * reconstruct objects exactly as they were saved.
 */
public class FileManager {

    private static final String DATA_DIR = "data";
    private static final String DONORS_FILE = DATA_DIR + File.separator + "donors.txt";
    private static final String PATIENTS_FILE = DATA_DIR + File.separator + "patients.txt";
    private static final String BLOODBAGS_FILE = DATA_DIR + File.separator + "bloodbags.txt";

    private static final String FIELD_SEPARATOR = "|";
    private static final String FIELD_SEPARATOR_REGEX = "\\|";
    private static final String LIST_SEPARATOR = ",";

    public void saveAll(BloodBank bloodBank) throws IOException {
        Files.createDirectories(Paths.get(DATA_DIR));
        saveDonors(bloodBank.getAllDonors());
        savePatients(bloodBank.getAllPatients());
        saveBloodBags(bloodBank.getAllBloodBags());
    }

    public void loadAll(BloodBank bloodBank) throws IOException {
        for (Donor donor : loadDonors()) {
            bloodBank.addDonor(donor);
        }
        for (Patient patient : loadPatients()) {
            bloodBank.addPatient(patient);
        }
        for (BloodBag bag : loadBloodBags()) {
            bloodBank.addBloodBag(bag);
        }
    }

    // ---------- Donors ----------

    private void saveDonors(List<Donor> donors) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Donor donor : donors) {
            String history = donor.getDonationHistory().stream()
                    .map(LocalDate::toString)
                    .collect(Collectors.joining(LIST_SEPARATOR));
            lines.add(String.join(FIELD_SEPARATOR,
                    donor.getId(),
                    donor.getFullName(),
                    donor.getContactNumber(),
                    donor.getBloodType().name(),
                    donor.getRegistrationDate().toString(),
                    history));
        }
        Files.write(Paths.get(DONORS_FILE), lines);
    }

    private List<Donor> loadDonors() throws IOException {
        List<Donor> donors = new ArrayList<>();
        Path path = Paths.get(DONORS_FILE);
        if (!Files.exists(path)) {
            return donors;
        }
        for (String line : Files.readAllLines(path)) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(FIELD_SEPARATOR_REGEX, -1);
            Donor donor = new Donor(fields[0], fields[1], fields[2],
                    BloodType.valueOf(fields[3]), LocalDate.parse(fields[4]));
            if (fields.length > 5 && !fields[5].isBlank()) {
                for (String date : fields[5].split(LIST_SEPARATOR)) {
                    donor.recordDonation(LocalDate.parse(date));
                }
            }
            User.syncIdCounter(donor.getId());
            donors.add(donor);
        }
        return donors;
    }

    // ---------- Patients ----------

    private void savePatients(List<Patient> patients) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Patient patient : patients) {
            lines.add(String.join(FIELD_SEPARATOR,
                    patient.getId(),
                    patient.getFullName(),
                    patient.getContactNumber(),
                    patient.getBloodType().name(),
                    patient.getRegistrationDate().toString(),
                    patient.getEmergencyLevel().name(),
                    String.valueOf(patient.getUnitsRequired()),
                    patient.getWardNumber(),
                    String.valueOf(patient.isFulfilled())));
        }
        Files.write(Paths.get(PATIENTS_FILE), lines);
    }

    private List<Patient> loadPatients() throws IOException {
        List<Patient> patients = new ArrayList<>();
        Path path = Paths.get(PATIENTS_FILE);
        if (!Files.exists(path)) {
            return patients;
        }
        for (String line : Files.readAllLines(path)) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(FIELD_SEPARATOR_REGEX, -1);
            Patient patient = new Patient(fields[0], fields[1], fields[2],
                    BloodType.valueOf(fields[3]), LocalDate.parse(fields[4]),
                    EmergencyLevel.valueOf(fields[5]), Integer.parseInt(fields[6]),
                    fields[7], Boolean.parseBoolean(fields[8]));
            User.syncIdCounter(patient.getId());
            patients.add(patient);
        }
        return patients;
    }

    // ---------- Blood bags ----------

    private void saveBloodBags(List<BloodBag> bloodBags) throws IOException {
        List<String> lines = new ArrayList<>();
        for (BloodBag bag : bloodBags) {
            lines.add(String.join(FIELD_SEPARATOR,
                    bag.getBagId(),
                    bag.getBloodType().name(),
                    bag.getDonorId(),
                    bag.getDonationDate().toString(),
                    bag.getExpirationDate().toString(),
                    bag.getStatus().name()));
        }
        Files.write(Paths.get(BLOODBAGS_FILE), lines);
    }

    private List<BloodBag> loadBloodBags() throws IOException {
        List<BloodBag> bloodBags = new ArrayList<>();
        Path path = Paths.get(BLOODBAGS_FILE);
        if (!Files.exists(path)) {
            return bloodBags;
        }
        for (String line : Files.readAllLines(path)) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(FIELD_SEPARATOR_REGEX, -1);
            BloodBag bag = new BloodBag(fields[0], BloodType.valueOf(fields[1]), fields[2],
                    LocalDate.parse(fields[3]), LocalDate.parse(fields[4]),
                    BloodBag.Status.valueOf(fields[5]));
            BloodBag.syncBagIdCounter(bag.getBagId());
            bloodBags.add(bag);
        }
        return bloodBags;
    }
}
