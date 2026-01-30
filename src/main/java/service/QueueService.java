package service;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import db.DatabaseConnection;

/**
 * Queue service for appointment queue number calculation.
 * Equivalent to Python calculate_queue_number and check_and_allocate_time_slot functions.
 */
public class QueueService {
    private DatabaseConnection db;
    
    public QueueService() {
        this.db = DatabaseConnection.getInstance();
    }
    
    /**
     * Calculate queue number for an appointment.
     * Equivalent to Python: calculate_queue_number(appointment_date, time_slot, hospital_name, speciality)
     * 
     * @param appointmentDate Appointment date (YYYY-MM-DD format)
     * @param timeSlot Time slot for the appointment
     * @param hospitalName Name of the hospital
     * @param speciality Medical speciality
     * @return Queue number (count + 1)
     */
    public int calculateQueueNumber(String appointmentDate, String timeSlot,
                                    String hospitalName, String speciality) {
        // Check database connection
        if (!db.isConnected()) {
            return 1; // Return queue number 1 if database unavailable (fallback)
        }

        MongoCollection<Document> appointmentCollection = db.appointmentCollection;

        // Count how many appointments have already been booked for the same slot
        // Equivalent to Python: appointment_collection.count_documents({...})
        Document query = new Document("appointment_date", appointmentDate)
                .append("time_slot", timeSlot)
                .append("hospital_name", hospitalName)
                .append("speciality", speciality);

        long count = appointmentCollection.countDocuments(query);

        // Return count + 1 (next queue number)
        return (int) count + 1;
    }
    
    /**
     * Check if a time slot is full and allocate if available.
     * Equivalent to Python: check_and_allocate_time_slot(appointment_date, time_slot, hospital_name, speciality)
     * 
     * @param appointmentDate Appointment date (YYYY-MM-DD format)
     * @param timeSlot Time slot for the appointment
     * @param hospitalName Name of the hospital
     * @param speciality Medical speciality
     * @return true if slot is full, false if available
     */
    public boolean checkAndAllocateTimeSlot(String appointmentDate, String timeSlot,
                                           String hospitalName, String speciality) {
        // Check database connection
        if (!db.isConnected()) {
            return true; // Consider slot full if database unavailable (conservative approach)
        }

        MongoCollection<Document> doctorsCollection = db.doctorsCollection;
        MongoCollection<Document> appointmentCollection = db.appointmentCollection;
        
        // Count doctors for the hospital and speciality
        // Equivalent to Python: doctors_collection.count_documents({...})
        Document doctorQuery = new Document("hospital_name", hospitalName)
                .append("specialization", speciality);
        long doctorCount = doctorsCollection.countDocuments(doctorQuery);
        
        // Count appointments in the given time slot
        // Equivalent to Python: appointment_collection.count_documents({...})
        Document appointmentQuery = new Document("appointment_date", appointmentDate)
                .append("time_slot", timeSlot)
                .append("hospital_name", hospitalName)
                .append("speciality", speciality);
        long appointmentCount = appointmentCollection.countDocuments(appointmentQuery);
        
        // Calculate capacity: 3 appointments per doctor per slot
        // If no doctors available, slot is considered unavailable
        if (doctorCount <= 0) {
            return true; // no doctors => cannot book
        }

        int capacity = (int)doctorCount * 3;

        // Return true if slot is full (appointments reached or exceeded capacity)
        return appointmentCount >= capacity;
    }
}
