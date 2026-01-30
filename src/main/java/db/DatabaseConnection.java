package db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

// Note: Requires MongoDB Java Driver
// Download from: https://repo1.maven.org/maven2/org/mongodb/mongodb-driver-sync/
// Or use Maven/Gradle dependency: org.mongodb:mongodb-driver-sync:4.11.1

/**
 * Database connection class for MongoDB.
 * Equivalent to the Python MongoDB connection setup.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private MongoClient client;
    private MongoDatabase database;
    
    // Collection references (equivalent to Python collections)
    public MongoCollection<Document> patientsCollection;
    public MongoCollection<Document> doctorsCollection;
    public MongoCollection<Document> usersCollection;
    public MongoCollection<Document> adminCollection;
    public MongoCollection<Document> appointmentCollection;
    public MongoCollection<Document> contactCollection;
    public MongoCollection<Document> superadminCollection;
    public MongoCollection<Document> hospitalDataCollection;
    public MongoCollection<Document> hospitalDischargeCollection;
    public MongoCollection<Document> inventoryCollection;
    public MongoCollection<Document> stockCollection;
    
    // MongoDB connection URI (same as Python)
    private static final String URI = "mongodb+srv://manasranjanpradhan2004:root@hms.m7j9t.mongodb.net/?retryWrites=true&w=majority&appName=HMS";
    private static final String DATABASE_NAME = "HMS";
    
    private DatabaseConnection() {
        try {
            // Create MongoDB client
            client = MongoClients.create(URI);
            database = client.getDatabase(DATABASE_NAME);

            // Initialize collections (equivalent to Python db['collection_name'])
            patientsCollection = database.getCollection("patients");
            doctorsCollection = database.getCollection("doctors");
            usersCollection = database.getCollection("users");
            adminCollection = database.getCollection("admin");
            appointmentCollection = database.getCollection("appointment");
            contactCollection = database.getCollection("contact");
            superadminCollection = database.getCollection("Superadmin");
            hospitalDataCollection = database.getCollection("hospital_data");
            hospitalDischargeCollection = database.getCollection("discharged");
            inventoryCollection = database.getCollection("inventory");
            stockCollection = database.getCollection("stock");

            // Test connection (equivalent to Python client.admin.command('ping'))
            database.runCommand(new Document("ping", 1));
            System.out.println("Successfully connected to MongoDB!");

            // Clean up old appointments (equivalent to Python code)
            cleanupOldAppointments();

        } catch (Exception e) {
            System.err.println("Error connecting to MongoDB: " + e.getMessage());
            System.err.println("MongoDB connection failed. The application will continue with limited functionality.");
            System.err.println("Please check your network connection or MongoDB Atlas configuration.");
            e.printStackTrace();

            // Set collections to null to indicate connection failure
            client = null;
            database = null;
            patientsCollection = null;
            doctorsCollection = null;
            usersCollection = null;
            adminCollection = null;
            appointmentCollection = null;
            contactCollection = null;
            superadminCollection = null;
            hospitalDataCollection = null;
            hospitalDischargeCollection = null;
            inventoryCollection = null;
            stockCollection = null;
        }
    }
    
    /**
     * Get singleton instance of DatabaseConnection.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Check if database connection is available.
     */
    public boolean isConnected() {
        return client != null && database != null;
    }
    
    /**
     * Clean up appointments older than today.
     * Equivalent to Python: appointment_collection.delete_many({'appointment_date': {'$lt': today_date}})
     */
    private void cleanupOldAppointments() {
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            String todayDate = today.toString();
            
            Document query = new Document("appointment_date", new Document("$lt", todayDate));
            long deletedCount = appointmentCollection.deleteMany(query).getDeletedCount();
            System.out.println("Deleted " + deletedCount + " old appointments.");
        } catch (Exception e) {
            System.err.println("Error cleaning up appointments: " + e.getMessage());
        }
    }
    
    /**
     * Close database connection.
     */
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
