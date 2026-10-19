package medicalendra.common;

/**
 * Simple base class representing a person with a name.
 * Demonstrates inheritance (Patient will extend this class).
 */
public class Person {
    private String fullName;

    public Person() {}

    public Person(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
