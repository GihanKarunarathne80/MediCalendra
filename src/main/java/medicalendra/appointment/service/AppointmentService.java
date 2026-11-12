package medicalendra.appointment.service;

import medicalendra.appointment.model.Appointment;
import medicalendra.common.CsvUtil;
import medicalendra.common.DataPathResolver;
import medicalendra.patient.service.PatientService;
import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service class handling CRUD operations for {@link Appointment}.
 * Data is persisted in the canonical {@code data/appointments.txt} file.
 * Validation ensures that a referenced patient exists via {@link PatientService}.
 */
public class AppointmentService {
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final Pattern TIME_PATTERN = Pattern.compile("^\\d{2}:\\d{2}$");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    private final Path dataFile;
    private final PatientService patientService;

    // Default constructor using deterministic path
    public AppointmentService(PatientService patientService) {
        this(patientService, DataPathResolver.resolveDataFile(null, "appointments.txt"));
    }

    // Constructor allowing explicit data file path
    public AppointmentService(PatientService patientService, Path dataFile) {
        this.patientService = Objects.requireNonNull(patientService, "patientService cannot be null");
        this.dataFile = Objects.requireNonNull(dataFile, "dataFile cannot be null");
        ensureFileExists();
    }

    // Factory method for servlet context
    public static AppointmentService withServletContext(ServletContext ctx, PatientService patientService) {
        Path path = DataPathResolver.resolveDataFile(ctx, "appointments.txt");
        return new AppointmentService(patientService, path);
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
            throw new RuntimeException("Unable to initialise appointment data file at " + dataFile, e);
        }
    }

    // ---------- CSV helpers ----------
    private String toCsv(Appointment a) {
        return CsvUtil.toCsvLine(
                a.getId(),
                a.getPatientId(),
                a.getDate(),
                a.getTime(),
                a.getReason(),
                a.getStatus()
        );
    }

    private Appointment fromCsv(String line) {
        if (CsvUtil.isCommentOrBlank(line)) {
            return null;
        }
        List<String> parts = CsvUtil.parseLine(line);
        if (parts.size() < 6) {
            System.err.println("WARN [AppointmentService]: Skipping malformed appointment line: " + line);
            return null;
        }
        return new Appointment(
                parts.get(0),
                parts.get(1),
                parts.get(2),
                parts.get(3),
                parts.get(4),
                parts.get(5)
        );
    }

    // ---------- Validation Helpers ----------
    public LocalDate parseAndValidateDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Appointment date cannot be empty.");
        }
        String trimmed = dateStr.trim();
        if (!DATE_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid date format. Expected format is YYYY-MM-DD (e.g., 2026-12-25).");
        }
        try {
            return LocalDate.parse(trimmed, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid calendar date: " + trimmed + ". Please enter a valid date.");
        }
    }

    public LocalTime parseAndValidateTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Appointment time cannot be empty.");
        }
        String trimmed = timeStr.trim();
        if (!TIME_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid time format. Expected 24-hour format HH:mm (e.g., 14:30).");
        }
        try {
            return LocalTime.parse(trimmed, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time value: " + trimmed + ". Please enter a valid time (00:00 to 23:59).");
        }
    }

    public void validateNotPast(LocalDate date, LocalTime time) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new IllegalArgumentException("Cannot book appointment for a past date (" + date + "). Please select a future date.");
        }
        if (date.isEqual(today)) {
            LocalTime now = LocalTime.now();
            if (time.isBefore(LocalTime.of(now.getHour(), now.getMinute()))) {
                throw new IllegalArgumentException("Cannot book appointment for a past time today. Selected time (" 
                        + time.format(TIME_FORMATTER) + ") has already passed.");
            }
        }
    }

    public void validateNoConflict(String patientId, String date, String time, String excludeAppointmentId) {
        List<Appointment> existingAppointments = getAllAppointments();
        for (Appointment existing : existingAppointments) {
            if (excludeAppointmentId != null && excludeAppointmentId.equals(existing.getId())) {
                continue;
            }
            if ("Cancelled".equalsIgnoreCase(existing.getStatus())) {
                continue;
            }
            if (patientId.equals(existing.getPatientId())
                    && date.equals(existing.getDate())
                    && time.equals(existing.getTime())) {
                throw new IllegalArgumentException("Scheduling conflict: Patient already has an appointment scheduled on " 
                        + date + " at " + time + ".");
            }
        }
    }

    public void validateAppointment(String patientId, String date, String time, String reason, String status, String excludeAppointmentId) {
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID is required.");
        }
        if (patientService.getPatientById(patientId.trim()).isEmpty()) {
            throw new IllegalArgumentException("Patient ID " + patientId + " does not exist.");
        }
        LocalDate parsedDate = parseAndValidateDate(date);
        LocalTime parsedTime = parseAndValidateTime(time);

        validateNotPast(parsedDate, parsedTime);
        validateNoConflict(patientId.trim(), parsedDate.toString(), parsedTime.format(TIME_FORMATTER), excludeAppointmentId);

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason is required.");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required.");
        }
    }

    // ---------- CRUD ----------
    public synchronized Appointment createAppointment(String patientId, String date, String time, String reason) {
        return createAppointment(patientId, date, time, reason, "Scheduled");
    }

    public synchronized Appointment createAppointment(String patientId, String date, String time, String reason, String status) {
        // Newly created appointments always start with "Scheduled" status
        String initialStatus = "Scheduled";
        validateAppointment(patientId, date, time, reason, initialStatus, null);

        LocalDate parsedDate = parseAndValidateDate(date);
        LocalTime parsedTime = parseAndValidateTime(time);
        String normDate = parsedDate.toString();
        String normTime = parsedTime.format(TIME_FORMATTER);

        String id = "A" + System.currentTimeMillis();
        while (getAppointmentById(id) != null) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}
            id = "A" + System.currentTimeMillis();
        }
        Appointment a = new Appointment(id, patientId.trim(), normDate, normTime, reason.trim(), initialStatus);
        try {
            ensureFileExists();
            Files.writeString(dataFile, toCsv(a) + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            return a;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write appointment to file: " + e.getMessage(), e);
        }
    }

    public synchronized List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        try {
            if (!Files.exists(dataFile)) return list;
            List<String> lines = Files.readAllLines(dataFile);
            for (String line : lines) {
                Appointment a = fromCsv(line);
                if (a != null) {
                    list.add(a);
                }
            }
        } catch (IOException e) {
            System.err.println("ERROR [AppointmentService]: Failed to read appointments: " + e.getMessage());
        }
        return list;
    }

    public synchronized Appointment getAppointmentById(String id) {
        if (id == null || id.isBlank()) return null;
        return getAllAppointments().stream()
                .filter(a -> id.equals(a.getId()))
                .findFirst()
                .orElse(null);
    }

    public synchronized boolean updateAppointment(Appointment updated) {
        if (updated == null || updated.getId() == null) return false;
        validateAppointment(updated.getPatientId(), updated.getDate(), updated.getTime(),
                updated.getReason(), updated.getStatus(), updated.getId());

        LocalDate parsedDate = parseAndValidateDate(updated.getDate());
        LocalTime parsedTime = parseAndValidateTime(updated.getTime());
        updated.setPatientId(updated.getPatientId().trim());
        updated.setDate(parsedDate.toString());
        updated.setTime(parsedTime.format(TIME_FORMATTER));
        updated.setReason(updated.getReason().trim());
        updated.setStatus(updated.getStatus().trim());

        List<Appointment> list = getAllAppointments();
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updated.getId())) {
                list.set(i, updated);
                found = true;
                break;
            }
        }
        if (found) {
            return writeAll(list);
        }
        return false;
    }

    public synchronized boolean deleteAppointment(String id) {
        if (id == null || id.isBlank()) return false;
        List<Appointment> list = getAllAppointments();
        boolean removed = list.removeIf(a -> id.equals(a.getId()));
        if (removed) {
            return writeAll(list);
        }
        return false;
    }

    private synchronized boolean writeAll(List<Appointment> list) {
        try {
            ensureFileExists();
            List<String> preservedLines = new ArrayList<>();
            if (Files.exists(dataFile)) {
                for (String raw : Files.readAllLines(dataFile)) {
                    if (CsvUtil.isCommentOrBlank(raw)) {
                        preservedLines.add(raw);
                    }
                }
            }
            List<String> lines = new ArrayList<>(preservedLines);
            for (Appointment a : list) {
                lines.add(toCsv(a));
            }
            Files.write(dataFile, lines,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("ERROR [AppointmentService]: Failed to rewrite appointments: " + e.getMessage());
            return false;
        }
    }

    // ---------- Search ----------
    public List<Appointment> search(String query) {
        String lower = query == null ? "" : query.toLowerCase();
        return getAllAppointments().stream()
                .filter(a -> (a.getId() != null && a.getId().toLowerCase().contains(lower))
                        || (a.getPatientId() != null && a.getPatientId().toLowerCase().contains(lower))
                        || (a.getDate() != null && a.getDate().toLowerCase().contains(lower))
                        || (a.getTime() != null && a.getTime().toLowerCase().contains(lower))
                        || (a.getReason() != null && a.getReason().toLowerCase().contains(lower))
                        || (a.getStatus() != null && a.getStatus().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
    }

    // ---------- Dashboard Analytics & Filtering ----------
    /**
     * Determines whether an appointment is a valid upcoming appointment:
     * - Must not be Cancelled
     * - Must be scheduled for a future date, or for today at/after the current time
     */
    public boolean isUpcoming(Appointment appointment) {
        if (appointment == null || appointment.getStatus() == null) {
            return false;
        }
        if ("Cancelled".equalsIgnoreCase(appointment.getStatus().trim())) {
            return false;
        }
        String dateStr = appointment.getDate();
        String timeStr = appointment.getTime();
        if (dateStr == null || timeStr == null || dateStr.isBlank() || timeStr.isBlank()) {
            return false;
        }
        try {
            LocalDate apptDate = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            LocalTime apptTime = LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
            LocalDate today = LocalDate.now();
            if (apptDate.isAfter(today)) {
                return true;
            }
            if (apptDate.isEqual(today)) {
                LocalTime now = LocalTime.now();
                return !apptTime.isBefore(LocalTime.of(now.getHour(), now.getMinute()));
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns valid upcoming appointments sorted with the nearest date and time first.
     */
    public List<Appointment> getUpcomingAppointments() {
        return getAllAppointments().stream()
                .filter(this::isUpcoming)
                .sorted((a1, a2) -> {
                    try {
                        LocalDate d1 = LocalDate.parse(a1.getDate().trim(), DATE_FORMATTER);
                        LocalDate d2 = LocalDate.parse(a2.getDate().trim(), DATE_FORMATTER);
                        int dateCmp = d1.compareTo(d2);
                        if (dateCmp != 0) {
                            return dateCmp;
                        }
                        LocalTime t1 = LocalTime.parse(a1.getTime().trim(), TIME_FORMATTER);
                        LocalTime t2 = LocalTime.parse(a2.getTime().trim(), TIME_FORMATTER);
                        return t1.compareTo(t2);
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns the count of valid upcoming appointments.
     */
    public int getUpcomingAppointmentsCount() {
        return getUpcomingAppointments().size();
    }

    /**
     * Returns the total count of cancelled appointments (case-insensitive).
     */
    public int getCancelledAppointmentsCount() {
        return (int) getAllAppointments().stream()
                .filter(a -> a.getStatus() != null && "Cancelled".equalsIgnoreCase(a.getStatus().trim()))
                .count();
    }
}
