package medicalendra.patient.model;

import medicalendra.common.Person;

/**
 * Patient model – inherits name from Person to demonstrate OOP inheritance.
 */
public class Patient extends Person {
    private String id;
    private String email;
    private String phoneNumber;

    public Patient() {}

    public Patient(String id, String fullName, String email, String phoneNumber) {
        super(fullName);
        this.id = id;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
