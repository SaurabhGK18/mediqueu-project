package model;

/**
 * Represents a doctor/server in the OPD queueing system.
 * Each doctor can serve one patient at a time.
 */
public class Doctor {
    private final int id;
    private boolean available;
    private Patient currentPatient;
    private double busyTime;
    
    /**
     * Creates a new doctor with the given ID.
     * 
     * @param id Unique identifier for the doctor
     */
    public Doctor(int id) {
        this.id = id;
        this.available = true;
        this.currentPatient = null;
        this.busyTime = 0.0;
    }
    
    /**
     * Gets the doctor's unique identifier.
     * 
     * @return Doctor ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Checks if the doctor is currently available to serve a patient.
     * 
     * @return true if available, false if busy
     */
    public boolean isAvailable() {
        return available;
    }
    
    /**
     * Sets the availability status of the doctor.
     * 
     * @param available true if available, false if busy
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    /**
     * Gets the patient currently being served by this doctor.
     * 
     * @return Current patient, or null if doctor is available
     */
    public Patient getCurrentPatient() {
        return currentPatient;
    }
    
    /**
     * Assigns a patient to this doctor for service.
     * 
     * @param patient Patient to be served
     */
    public void assignPatient(Patient patient) {
        this.currentPatient = patient;
        this.available = false;
    }
    
    /**
     * Releases the current patient, making the doctor available again.
     * Updates the busy time based on the service duration.
     * 
     * @param currentTime Current simulation time
     */
    public void releasePatient(double currentTime) {
        if (currentPatient != null && currentPatient.getServiceStartTime() >= 0) {
            double serviceDuration = currentTime - currentPatient.getServiceStartTime();
            busyTime += serviceDuration;
        }
        this.currentPatient = null;
        this.available = true;
    }
    
    /**
     * Gets the total time this doctor has been busy serving patients.
     * 
     * @return Total busy time
     */
    public double getBusyTime() {
        return busyTime;
    }
    
    /**
     * Adds additional busy time (useful for final calculations).
     * 
     * @param additionalTime Additional time to add to busy time
     */
    public void addBusyTime(double additionalTime) {
        this.busyTime += additionalTime;
    }
    
    @Override
    public String toString() {
        return String.format("Doctor[ID=%d, Available=%s, CurrentPatient=%s]", 
                id, available, currentPatient != null ? String.valueOf(currentPatient.getId()) : "None");
    }
}
