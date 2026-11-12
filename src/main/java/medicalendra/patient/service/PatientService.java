package medicalendra.patient.service;

import medicalendra.common.CsvUtil;
import medicalendra.common.DataPathResolver;
import medicalendra.patient.model.Patient;
import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service handling CRUD operations for Patient entities.
 * Data is persisted as CSV in the project-level canonical data directory.
 */
public class PatientService {
    // RFC 5322-compliant simplified email pattern requiring domain name and TLD of at least 2 chars
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$"
    );

    // Sri Lankan mobile number: starts with 07XXXXXXXX (10 digits) or +947XXXXXXXX (12 chars)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(?:\\+94|0)7[0-9]{8}$"
    );

    private final Path dataFile;

    public PatientService() {
        this(DataPathResolver.resolveDataFile(null, "patients.txt"));
    }

    public PatientService(Path dataFile) {
        this.dataFile = Objects.requireNonNull(dataFile, "dataFile cannot be null");
        ensureFileExists();
    }

    /**
     * Factory method used by servlets to create a service with the deterministic data file.
     */
    public static PatientService withServletContext(ServletContext ctx) {
        Path path = DataPathResolver.resolveDataFile(ctx, "patients.txt");
        return new PatientService(path);
    }

    public Path getDataFilePath() {
        return dataFile;
    }

    private void ensureFileExists() {
        try {
            if (dataFile.getParent() != null) {
                Files.createDirectories(dataFile.getParent());
            }
            if (!Files.exists(dataFile)) {
                Files.createFile(dataFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialise patient data file at " + dataFile, e);
        }
    }

    // ---------- CSV serialization and parsing ----------
    private String toCsv(Patient p) {
        return CsvUtil.toCsvLine(p.getId(), p.getFullName(), p.getEmail(), p.getPhoneNumber());
    }

    private Patient fromCsv(String line) {
        if (CsvUtil.isCommentOrBlank(line)) {
            return null;
        }
        List<String> parts = CsvUtil.parseLine(line);
        if (parts.size() < 4) {
            System.err.println("WARN [PatientService]: Skipping malformed patient line: " + line);
            return null;
        }
        return new Patient(
                parts.get(0),
                parts.get(1),
                parts.get(2),
                parts.get(3)
        );
    }

    // ---------- Low-level CRUD ----------
    public synchronized boolean create(Patient patient) {
        if (patient == null || patient.getId() == null) return false;
        validatePatient(patient);
        if (findById(patient.getId()).isPresent()) return false; // duplicate
        try {
            ensureFileExists();
            Files.writeString(dataFile, toCsv(patient) + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("ERROR [PatientService]: Failed to append patient: " + e.getMessage());
            return false;
        }
    }

    public synchronized List<Patient> findAll() {
        List<Patient> patients = new ArrayList<>();
        try {
            if (Files.notExists(dataFile)) return patients;
            List<String> lines = Files.readAllLines(dataFile);
            for (String line : lines) {
                Patient p = fromCsv(line);
                if (p != null) {
                    patients.add(p);
                }
            }
        } catch (IOException e) {
            System.err.println("ERROR [PatientService]: Failed to read patients: " + e.getMessage());
        }
        return patients;
    }

    public synchronized Optional<Patient> findById(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return findAll().stream()
                .filter(p -> id.equals(p.getId()))
                .findFirst();
    }

    public synchronized boolean update(Patient updated) {
        if (updated == null || updated.getId() == null) return false;
        validatePatient(updated);
        List<Patient> patients = findAll();
        boolean found = false;
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getId().equals(updated.getId())) {
                patients.set(i, updated);
                found = true;
                break;
            }
        }
        if (!found) return false;
        return writeAll(patients);
    }

    public synchronized boolean delete(String id) {
        if (id == null || id.isBlank()) return false;
        List<Patient> patients = findAll();
        List<Patient> filtered = patients.stream()
                .filter(p -> !id.equals(p.getId()))
                .collect(Collectors.toList());
        if (filtered.size() == patients.size()) return false; // nothing removed
        return writeAll(filtered);
    }

    private synchronized boolean writeAll(List<Patient> patients) {
        try {
            ensureFileExists();
            List<String> preservedLines = new ArrayList<>();
            // Preserve top-level comments and recognized non-patient lines if present
            if (Files.exists(dataFile)) {
                for (String raw : Files.readAllLines(dataFile)) {
                    if (CsvUtil.isCommentOrBlank(raw)) {
                        preservedLines.add(raw);
                    }
                }
            }
            List<String> lines = new ArrayList<>(preservedLines);
            for (Patient p : patients) {
                lines.add(toCsv(p));
            }
            Files.write(dataFile, lines,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("ERROR [PatientService]: Failed to rewrite patients: " + e.getMessage());
            return false;
        }
    }

    // ---------- Validation Helpers ----------
    public void validatePatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null.");
        }
        validateFullName(patient.getFullName());
        validateEmail(patient.getEmail());
        validatePhoneNumber(patient.getPhoneNumber());
        // Normalise fields to trimmed strings
        patient.setFullName(patient.getFullName().trim());
        patient.setEmail(patient.getEmail().trim());
        patient.setPhoneNumber(patient.getPhoneNumber().trim());
    }

    public void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
    }

    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format. Please provide a valid email address (e.g., patient@example.com).");
        }
    }

    public void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        String trimmed = phoneNumber.trim();
        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid phone number. Must be a valid Sri Lankan mobile number (e.g., 0771234567 or +94771234567).");
        }
    }

    // ---------- High-level API used by servlet ----------
    public synchronized Patient createPatient(String fullName, String email, String phoneNumber) {
        validateFullName(fullName);
        validateEmail(email);
        validatePhoneNumber(phoneNumber);
        String id = "P" + System.currentTimeMillis();
        while (findById(id).isPresent()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}
            id = "P" + System.currentTimeMillis();
        }
        Patient p = new Patient(id, fullName.trim(), email.trim(), phoneNumber.trim());
        if (create(p)) return p;
        throw new IllegalStateException("Unable to create patient: duplicate ID or file write error");
    }

    public List<Patient> getAllPatients() {
        return findAll();
    }

    public Optional<Patient> getPatientById(String id) {
        return findById(id);
    }

    public boolean updatePatient(Patient patient) {
        if (patient == null) return false;
        validatePatient(patient);
        return update(patient);
    }

    public boolean deletePatient(String id) {
        return delete(id);
    }

    public List<Patient> searchPatients(String query) {
        if (query == null || query.isBlank()) return getAllPatients();
        String q = query.toLowerCase();
        return findAll().stream()
                .filter(p -> (p.getId() != null && p.getId().toLowerCase().contains(q))
                        || (p.getFullName() != null && p.getFullName().toLowerCase().contains(q))
                        || (p.getEmail() != null && p.getEmail().toLowerCase().contains(q))
                        || (p.getPhoneNumber() != null && p.getPhoneNumber().toLowerCase().contains(q)))
                .collect(Collectors.toList());
    }
}
