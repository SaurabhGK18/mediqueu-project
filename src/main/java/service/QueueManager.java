package service;

import model.Patient;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Manages the patient queue using FIFO (First-In-First-Out) discipline.
 * Patients are added to the queue and served in the order they arrive.
 */
public class QueueManager {
    private final Queue<Patient> patientQueue;
    private int maxQueueLength;
    private double totalQueueTime;
    private double lastQueueChangeTime;
    private int currentQueueLength;
    
    /**
     * Creates a new queue manager with an empty queue.
     */
    public QueueManager() {
        this.patientQueue = new LinkedList<>();
        this.maxQueueLength = 0;
        this.totalQueueTime = 0.0;
        this.lastQueueChangeTime = 0.0;
        this.currentQueueLength = 0;
    }
    
    /**
     * Adds a patient to the end of the queue (FIFO).
     * 
     * @param patient Patient to be added to the queue
     * @param currentTime Current simulation time
     */
    public void addPatient(Patient patient, double currentTime) {
        // Update queue time before adding
        updateQueueTime(currentTime);
        patientQueue.add(patient);
        currentQueueLength++;
        if (currentQueueLength > maxQueueLength) {
            maxQueueLength = currentQueueLength;
        }
        lastQueueChangeTime = currentTime;
    }
    
    /**
     * Removes and returns the next patient from the front of the queue (FIFO).
     * 
     * @param currentTime Current simulation time
     * @return The next patient in the queue, or null if queue is empty
     */
    public Patient getNextPatient(double currentTime) {
        // Update queue time before removing
        updateQueueTime(currentTime);
        Patient patient = patientQueue.poll();
        if (patient != null) {
            currentQueueLength--;
        }
        lastQueueChangeTime = currentTime;
        return patient;
    }
    
    /**
     * Checks if the queue is empty.
     * 
     * @return true if queue is empty, false otherwise
     */
    public boolean isEmpty() {
        return patientQueue.isEmpty();
    }
    
    /**
     * Gets the current number of patients waiting in the queue.
     * 
     * @return Current queue length
     */
    public int getCurrentQueueLength() {
        return currentQueueLength;
    }
    
    /**
     * Gets the maximum queue length observed during simulation.
     * 
     * @return Maximum queue length
     */
    public int getMaxQueueLength() {
        return maxQueueLength;
    }
    
    /**
     * Updates the total queue time by integrating queue length over time.
     * This is used to calculate average queue length.
     * 
     * @param currentTime Current simulation time
     */
    private void updateQueueTime(double currentTime) {
        double timeInterval = currentTime - lastQueueChangeTime;
        totalQueueTime += currentQueueLength * timeInterval;
    }
    
    /**
     * Finalizes queue time calculation at the end of simulation.
     * 
     * @param endTime End time of the simulation
     */
    public void finalizeQueueTime(double endTime) {
        updateQueueTime(endTime);
    }
    
    /**
     * Gets the total time-integrated queue length.
     * Used to calculate average queue length = totalQueueTime / simulationTime
     * 
     * @return Total time-integrated queue length
     */
    public double getTotalQueueTime() {
        return totalQueueTime;
    }
    
    /**
     * Gets the number of patients currently in the queue.
     * 
     * @return Queue size
     */
    public int size() {
        return patientQueue.size();
    }
}
