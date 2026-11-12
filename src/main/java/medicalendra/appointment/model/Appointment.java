package medicalendra.appointment.model;

/**
 * Model representing a medical appointment.
 * All fields are stored as Strings for simplicity; date and time follow ISO‑8601 format.
 */
public class Appointment {
    private String id;
    private String patientId; // must correspond to an existing Patient id
    private String date;      // e.g., 2026-10-15
    private String time;      // e.g., 14:30
    private String reason;
    private String status;   // e.g., Scheduled, Completed, Cancelled

    public Appointment() {}

    public Appointment(String id, String patientId, String date, String time, String reason, String status) {
        this.id = id;
        this.patientId = patientId;
        this.date = date;
        this.time = time;
        this.reason = reason;
        this.status = status;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
