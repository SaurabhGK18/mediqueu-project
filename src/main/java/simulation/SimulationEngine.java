package simulation;

import model.Doctor;
import model.Patient;
import service.QueueManager;
import util.RandomGenerator;
import java.util.ArrayList;
import java.util.List;

/**
 * Main simulation engine for the OPD queueing system.
 * Implements an M/M/c queueing model where:
 * - M: Poisson arrivals (exponential inter-arrival times)
 * - M: Exponential service times
 * - c: Multiple servers (doctors)
 * - FIFO: First-In-First-Out queue discipline
 */
public class SimulationEngine {
    private final QueueManager queueManager;
    private final List<Doctor> doctors;
    private final double simulationTime;
    private final double arrivalRate; // Lambda: patients per unit time
    private final double serviceRate; // Mu: services per unit time per doctor
    
    // Statistics
    private final List<Patient> servedPatients;
    private int totalPatientsArrived;
    private double currentTime;
    
    /**
     * Creates a new simulation engine with specified parameters.
     * 
     * @param simulationTime Total time to run the simulation
     * @param arrivalRate Rate of patient arrivals (lambda)
     * @param serviceRate Rate of service per doctor (mu)
     * @param numberOfDoctors Number of doctors/servers (c)
     */
    public SimulationEngine(double simulationTime, double arrivalRate, 
                           double serviceRate, int numberOfDoctors) {
        if (simulationTime <= 0 || arrivalRate <= 0 || serviceRate <= 0 || numberOfDoctors <= 0) {
            throw new IllegalArgumentException("All parameters must be positive");
        }
        
        this.simulationTime = simulationTime;
        this.arrivalRate = arrivalRate;
        this.serviceRate = serviceRate;
        this.queueManager = new QueueManager();
        this.doctors = new ArrayList<>();
        this.servedPatients = new ArrayList<>();
        this.totalPatientsArrived = 0;
        this.currentTime = 0.0;
        
        // Initialize doctors
        for (int i = 1; i <= numberOfDoctors; i++) {
            doctors.add(new Doctor(i));
        }
    }
    
    /**
     * Runs the complete simulation from start to finish.
     * Uses event-driven simulation approach.
     */
    public void run() {
        System.out.println("Starting OPD Queueing Simulation...");
        System.out.println("=====================================");
        System.out.printf("Simulation Time: %.2f minutes%n", simulationTime);
        System.out.printf("Arrival Rate: %.4f patients/minute%n", arrivalRate);
        System.out.printf("Service Rate: %.4f patients/minute per doctor%n", serviceRate);
        System.out.printf("Number of Doctors: %d%n", doctors.size());
        System.out.println("=====================================\n");
        
        // Event list: stores departure events (patient, doctor, departure time)
        List<DepartureEvent> departureEvents = new ArrayList<>();
        
        // Generate patient arrivals
        double nextArrivalTime = 0.0;
        int patientId = 1;
        
        // Main simulation loop
        while (currentTime < simulationTime || !departureEvents.isEmpty() || !queueManager.isEmpty()) {
            // Find next departure event
            DepartureEvent nextDeparture = findNextDeparture(departureEvents);
            
            // Process next event
            if (nextDeparture != null && 
                (currentTime >= simulationTime || nextDeparture.departureTime <= nextArrivalTime)) {
                // Process departure
                currentTime = nextDeparture.departureTime;
                processDeparture(nextDeparture, departureEvents);
            } else if (currentTime < simulationTime && nextArrivalTime <= simulationTime) {
                // Process arrival
                if (nextArrivalTime == 0.0) {
                    // First arrival
                    nextArrivalTime = RandomGenerator.generateExponential(arrivalRate);
                }
                currentTime = nextArrivalTime;
                processArrival(patientId++, departureEvents);
                
                // Schedule next arrival
                if (currentTime < simulationTime) {
                    double interArrivalTime = RandomGenerator.generateExponential(arrivalRate);
                    nextArrivalTime = currentTime + interArrivalTime;
                } else {
                    nextArrivalTime = Double.MAX_VALUE; // No more arrivals
                }
            } else {
                // Process remaining departures
                if (nextDeparture != null) {
                    currentTime = nextDeparture.departureTime;
                    processDeparture(nextDeparture, departureEvents);
                } else {
                    break; // No more events
                }
            }
        }
        
        // Finalize statistics
        finalizeStatistics();
        
        // Print results
        printResults();
    }
    
    
    /**
     * Processes a patient arrival event.
     * 
     * @param patientId ID for the new patient
     * @param departureEvents List of scheduled departure events
     */
    private void processArrival(int patientId, List<DepartureEvent> departureEvents) {
        Patient patient = new Patient(patientId, currentTime);
        totalPatientsArrived++;
        
        // Try to assign patient to an available doctor
        Doctor availableDoctor = findAvailableDoctor();
        
        if (availableDoctor != null) {
            // Doctor available, start service immediately
            patient.setServiceStartTime(currentTime);
            availableDoctor.assignPatient(patient);
            
            // Schedule departure
            double serviceTime = RandomGenerator.generateExponential(serviceRate);
            double departureTime = currentTime + serviceTime;
            departureEvents.add(new DepartureEvent(patient, availableDoctor, departureTime));
        } else {
            // All doctors busy, add to queue
            queueManager.addPatient(patient, currentTime);
        }
    }
    
    /**
     * Processes a patient departure event.
     * 
     * @param departureEvent The departure event to process
     * @param departureEvents List of scheduled departure events
     */
    private void processDeparture(DepartureEvent departureEvent, List<DepartureEvent> departureEvents) {
        Patient patient = departureEvent.patient;
        Doctor doctor = departureEvent.doctor;
        
        // Complete service
        // Cap departure time at simulationTime for accurate statistics
        double effectiveTime = Math.min(currentTime, simulationTime);
        patient.setDepartureTime(currentTime);
        doctor.releasePatient(effectiveTime);
        servedPatients.add(patient);
        
        // Remove from departure events
        departureEvents.remove(departureEvent);
        
        // Check if there are patients waiting in queue
        // Only start new services if we haven't exceeded simulation time
        if (!queueManager.isEmpty() && currentTime < simulationTime) {
            Patient nextPatient = queueManager.getNextPatient(currentTime);
            if (nextPatient != null) {
                // Start service for next patient
                nextPatient.setServiceStartTime(currentTime);
                doctor.assignPatient(nextPatient);
                
                // Schedule departure
                double serviceTime = RandomGenerator.generateExponential(serviceRate);
                double departureTime = currentTime + serviceTime;
                departureEvents.add(new DepartureEvent(nextPatient, doctor, departureTime));
            }
        }
    }
    
    /**
     * Finds an available doctor.
     * 
     * @return Available doctor, or null if all are busy
     */
    private Doctor findAvailableDoctor() {
        for (Doctor doctor : doctors) {
            if (doctor.isAvailable()) {
                return doctor;
            }
        }
        return null;
    }
    
    /**
     * Finds the next departure event in the list.
     * 
     * @param departureEvents List of departure events
     * @return Next departure event, or null if list is empty
     */
    private DepartureEvent findNextDeparture(List<DepartureEvent> departureEvents) {
        if (departureEvents.isEmpty()) {
            return null;
        }
        
        DepartureEvent next = departureEvents.get(0);
        for (DepartureEvent event : departureEvents) {
            if (event.departureTime < next.departureTime) {
                next = event;
            }
        }
        return next;
    }
    
    /**
     * Finalizes statistics at the end of simulation.
     */
    private void finalizeStatistics() {
        // Finalize queue time calculation
        queueManager.finalizeQueueTime(simulationTime);
        
        // Update busy time for doctors still serving patients at simulation end
        // Only count time up to simulationTime
        for (Doctor doctor : doctors) {
            if (!doctor.isAvailable() && doctor.getCurrentPatient() != null) {
                Patient patient = doctor.getCurrentPatient();
                if (patient.getServiceStartTime() >= 0) {
                    // Only count busy time up to simulationTime
                    double serviceStartTime = patient.getServiceStartTime();
                    if (serviceStartTime < simulationTime) {
                        double additionalBusyTime = simulationTime - serviceStartTime;
                        doctor.addBusyTime(additionalBusyTime);
                    }
                }
            }
        }
    }
    
    /**
     * Calculates and prints simulation results and metrics.
     */
    private void printResults() {
        System.out.println("\n=====================================");
        System.out.println("SIMULATION RESULTS");
        System.out.println("=====================================\n");
        
        // Calculate metrics
        double totalWaitingTime = 0.0;
        int patientsServed = servedPatients.size();
        
        for (Patient patient : servedPatients) {
            totalWaitingTime += patient.getWaitingTime();
        }
        
        double averageWaitingTime = patientsServed > 0 ? totalWaitingTime / patientsServed : 0.0;
        double averageQueueLength = queueManager.getTotalQueueTime() / simulationTime;
        
        // Calculate service utilization
        double totalBusyTime = 0.0;
        for (Doctor doctor : doctors) {
            totalBusyTime += doctor.getBusyTime();
        }
        double serviceUtilization = totalBusyTime / (simulationTime * doctors.size());
        
        // Print metrics
        System.out.printf("Total Patients Arrived: %d%n", totalPatientsArrived);
        System.out.printf("Total Patients Served: %d%n", patientsServed);
        System.out.printf("Average Waiting Time: %.2f minutes%n", averageWaitingTime);
        System.out.printf("Average Queue Length: %.2f patients%n", averageQueueLength);
        System.out.printf("Maximum Queue Length: %d patients%n", queueManager.getMaxQueueLength());
        System.out.printf("Service Utilization: %.2f%%%n", serviceUtilization * 100);
        
        // Print per-doctor statistics
        System.out.println("\nPer-Doctor Statistics:");
        System.out.println("----------------------");
        for (Doctor doctor : doctors) {
            double doctorUtilization = doctor.getBusyTime() / simulationTime;
            System.out.printf("Doctor %d Utilization: %.2f%%%n", 
                    doctor.getId(), doctorUtilization * 100);
        }
        
        System.out.println("\n=====================================");
        System.out.println("Simulation completed successfully!");
        System.out.println("=====================================");
    }
    
    /**
     * Inner class to represent a departure event.
     */
    private static class DepartureEvent {
        Patient patient;
        Doctor doctor;
        double departureTime;
        
        DepartureEvent(Patient patient, Doctor doctor, double departureTime) {
            this.patient = patient;
            this.doctor = doctor;
            this.departureTime = departureTime;
        }
    }
}
