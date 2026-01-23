package model;

/**
 * Represents a patient in the OPD queueing system.
 * Tracks patient arrival time, service start time, and departure time.
 */
public class Patient {
    private final int id;
    private final double arrivalTime;
    private double serviceStartTime;
    private double departureTime;
    
    /**
     * Creates a new patient with the given ID and arrival time.
     * 
     * @param id Unique identifier for the patient
     * @param arrivalTime Time when the patient arrives at the OPD
     */
    public Patient(int id, double arrivalTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceStartTime = -1.0; // Not yet started service
        this.departureTime = -1.0; // Not yet departed
    }
    
    /**
     * Gets the patient's unique identifier.
     * 
     * @return Patient ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Gets the time when the patient arrived.
     * 
     * @return Arrival time
     */
    public double getArrivalTime() {
        return arrivalTime;
    }
    
    /**
     * Gets the time when service started for this patient.
     * 
     * @return Service start time, or -1 if service hasn't started
     */
    public double getServiceStartTime() {
        return serviceStartTime;
    }
    
    /**
     * Sets the time when service started for this patient.
     * 
     * @param serviceStartTime Time when service begins
     */
    public void setServiceStartTime(double serviceStartTime) {
        this.serviceStartTime = serviceStartTime;
    }
    
    /**
     * Gets the time when the patient departs after service completion.
     * 
     * @return Departure time, or -1 if service hasn't completed
     */
    public double getDepartureTime() {
        return departureTime;
    }
    
    /**
     * Sets the time when the patient departs after service completion.
     * 
     * @param departureTime Time when service completes
     */
    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }
    
    /**
     * Calculates the waiting time for this patient.
     * Waiting time = Service start time - Arrival time
     * 
     * @return Waiting time, or 0 if service hasn't started
     */
    public double getWaitingTime() {
        if (serviceStartTime < 0) {
            return 0.0;
        }
        return serviceStartTime - arrivalTime;
    }
    
    /**
     * Calculates the service time for this patient.
     * Service time = Departure time - Service start time
     * 
     * @return Service time, or 0 if service hasn't completed
     */
    public double getServiceTime() {
        if (departureTime < 0 || serviceStartTime < 0) {
            return 0.0;
        }
        return departureTime - serviceStartTime;
    }
    
    /**
     * Checks if the patient has completed service.
     * 
     * @return true if departure time is set, false otherwise
     */
    public boolean hasCompletedService() {
        return departureTime >= 0;
    }
    
    @Override
    public String toString() {
        return String.format("Patient[ID=%d, Arrival=%.2f, ServiceStart=%.2f, Departure=%.2f]", 
                id, arrivalTime, serviceStartTime, departureTime);
    }
}
