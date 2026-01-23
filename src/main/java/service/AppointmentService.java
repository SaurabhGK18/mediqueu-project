package service;

import db.DatabaseConnection;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import java.util.ArrayList;
import java.util.List;

/**
 * Appointment service for managing appointments.
 * Equivalent to Python appointment booking functionality.
 */
public class AppointmentService {
    private DatabaseConnection db;
    private QueueService queueService;
    
    public AppointmentService() {
        this.db = DatabaseConnection.getInstance();
        this.queueService = new QueueService();
    }
    
    /**
     * Book an appointment.
     * Equivalent to Python: /appointment route POST handler.
     * 
     * @param name Patient name
     * @param username User username
     * @param number Phone number
     * @param email Email
     * @param address Address
     * @param appointmentDate Appointment date (YYYY-MM-DD)
     * @param timeSlot Time slot
     * @param speciality Medical speciality
     * @param diseaseDescription Disease description
     * @param hospitalName Hospital name
     * @param doctorName Doctor name
     * @return Appointment result with queue number or error message
     */
    public AppointmentResult bookAppointment(String name, String username, String number,
                                            String email, String address, String appointmentDate,
                                            String timeSlot, String speciality, String diseaseDescription,
                                            String hospitalName, String doctorName) {
        AppointmentResult result = new AppointmentResult();
        
        // Validate inputs
        if (name == null || name.trim().isEmpty()) {
            result.success = false;
            result.message = "Patient name is required.";
            return result;
        }
        if (username == null || username.trim().isEmpty()) {
            result.success = false;
            result.message = "Username is required.";
            return result;
        }
        if (hospitalName == null || hospitalName.trim().isEmpty()) {
            result.success = false;
            result.message = "Hospital selection is required.";
            return result;
        }
        if (speciality == null || speciality.trim().isEmpty()) {
            result.success = false;
            result.message = "Speciality selection is required.";
            return result;
        }
        if (doctorName == null || doctorName.trim().isEmpty()) {
            result.success = false;
            result.message = "Doctor selection is required.";
            return result;
        }
        if (appointmentDate == null || appointmentDate.trim().isEmpty()) {
            result.success = false;
            result.message = "Appointment date is required.";
            return result;
        }
        if (timeSlot == null || timeSlot.trim().isEmpty()) {
            result.success = false;
            result.message = "Time slot selection is required.";
            return result;
        }
        
        // Validate date format and ensure it's not in the past
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(appointmentDate);
            java.time.LocalDate today = java.time.LocalDate.now();
            if (date.isBefore(today)) {
                result.success = false;
                result.message = "Appointment date cannot be in the past.";
                return result;
            }
        } catch (Exception e) {
            result.success = false;
            result.message = "Invalid date format. Please select a valid date.";
            return result;
        }
        
        // Trim inputs
        hospitalName = hospitalName.trim();
        speciality = speciality.trim();
        doctorName = doctorName.trim();
        
        // Check if doctors are available for the hospital and speciality
        List<String> doctorNames = getDoctorsByHospitalAndSpeciality(hospitalName, speciality);
        
        if (doctorNames.isEmpty()) {
            result.success = false;
            result.message = "No doctors available for " + speciality + " in " + hospitalName + ". Please select a different hospital or speciality.";
            return result;
        }
        
        // Validate that selected doctor is actually available for this hospital/speciality
        if (!doctorNames.contains(doctorName)) {
            result.success = false;
            result.message = "Selected doctor is not available for " + speciality + " in " + hospitalName + ". Please select a valid doctor.";
            return result;
        }
        
        // Check if time slot is available
        boolean isSlotFull = queueService.checkAndAllocateTimeSlot(
            appointmentDate, timeSlot, hospitalName, speciality);
        
        if (isSlotFull) {
            result.success = false;
            result.message = "The selected time slot is full. Please choose another time or date.";
            return result;
        }
        
        // Calculate queue number
        int queueNumber = queueService.calculateQueueNumber(
            appointmentDate, timeSlot, hospitalName, speciality);
        
        // Create appointment document
        Document appointmentData = new Document("name", name.trim())
                .append("username", username.trim())
                .append("number", number != null ? number.trim() : "")
                .append("email", email != null ? email.trim() : "")
                .append("address", address != null ? address.trim() : "")
                .append("appointment_date", appointmentDate)
                .append("time_slot", timeSlot)
                .append("speciality", speciality)
                .append("disease_description", diseaseDescription != null ? diseaseDescription.trim() : "")
                .append("hospital_name", hospitalName)
                .append("queue_number", queueNumber)
                .append("appointed_doc", doctorName);
        
        // Insert appointment
        try {
            db.appointmentCollection.insertOne(appointmentData);
            result.success = true;
            result.queueNumber = queueNumber;
            result.message = "Appointment booked successfully!";
        } catch (Exception e) {
            result.success = false;
            result.message = "Error booking appointment. Please try again.";
            System.err.println("Error inserting appointment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Get doctors by hospital and speciality.
     * Equivalent to Python: /get-doctors/<hospital>/<speciality> route.
     * 
     * @param hospital Hospital name
     * @param speciality Medical speciality
     * @return List of doctor names
     */
    public List<String> getDoctorsByHospitalAndSpeciality(String hospital, String speciality) {
        MongoCollection<Document> doctorsCollection = db.doctorsCollection;
        
        // Trim and normalize inputs
        hospital = hospital != null ? hospital.trim() : "";
        speciality = speciality != null ? speciality.trim() : "";
        
        Document query = new Document("hospital_name", hospital)
                .append("specialization", speciality);
        
        FindIterable<Document> doctors = doctorsCollection.find(query);
        
        List<String> doctorNames = new ArrayList<>();
        for (Document doctor : doctors) {
            String docName = doctor.getString("name");
            if (docName != null && !docName.isEmpty()) {
                doctorNames.add(docName);
            }
        }
        
        // Debug: Log if no doctors found
        if (doctorNames.isEmpty()) {
            System.out.println("WARNING: No doctors found for hospital='" + hospital + "', speciality='" + speciality + "'");
            // Try case-insensitive search as fallback
            Document caseInsensitiveQuery = new Document("hospital_name", new Document("$regex", "^" + hospital + "$").append("$options", "i"))
                    .append("specialization", new Document("$regex", "^" + speciality + "$").append("$options", "i"));
            FindIterable<Document> fallbackDoctors = doctorsCollection.find(caseInsensitiveQuery);
            for (Document doctor : fallbackDoctors) {
                String docName = doctor.getString("name");
                if (docName != null && !docName.isEmpty()) {
                    doctorNames.add(docName);
                }
            }
            if (!doctorNames.isEmpty()) {
                System.out.println("Found " + doctorNames.size() + " doctors using case-insensitive search");
            }
        }
        
        return doctorNames;
    }
    
    /**
     * Get user appointments.
     * Equivalent to Python: user_app() function.
     * 
     * @param username Username
     * @return List of appointment documents
     */
    public List<Document> getUserAppointments(String username) {
        MongoCollection<Document> appointmentCollection = db.appointmentCollection;
        
        Document query = new Document("username", username);
        FindIterable<Document> appointments = appointmentCollection.find(query);
        
        List<Document> appointmentList = new ArrayList<>();
        for (Document appointment : appointments) {
            appointmentList.add(appointment);
        }
        
        return appointmentList;
    }
    
    /**
     * Get all hospitals.
     * 
     * @return List of hospital names
     */
    public List<String> getAllHospitals() {
        MongoCollection<Document> hospitalDataCollection = db.hospitalDataCollection;
        
        FindIterable<Document> hospitals = hospitalDataCollection.find();
        
        List<String> hospitalNames = new ArrayList<>();
        for (Document hospital : hospitals) {
            String hospitalName = hospital.getString("hospital_name");
            if (hospitalName != null && !hospitalName.trim().isEmpty()) {
                hospitalNames.add(hospitalName.trim());
            }
        }
        
        // Sort alphabetically for better UX
        hospitalNames.sort(String.CASE_INSENSITIVE_ORDER);
        
        return hospitalNames;
    }
    
    /**
     * Appointment result class.
     */
    public static class AppointmentResult {
        public boolean success;
        public String message;
        public int queueNumber;
    }
}
