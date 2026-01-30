import java.io.IOException;

import db.DatabaseConnection;
import db.InitializeSampleData;
import web.HospitalWebServer;

/**
 * Main class to start the Hospital Management System web server.
 * This is the Java conversion of the Python Flask application.
 * 
 * @author Converted from Python Flask app
 * @version 1.0
 */
public class Main {
    /**
     * Main method to start the web server.
     * 
     * @param args Command-line arguments (optional port number)
     */
    public static void main(String[] args) {
        try { web.ApiServer.start(); } catch (Exception e) { e.printStackTrace(); }



        try {
            // Initialize sample data (hospitals, doctors, admin)
            System.out.println("Setting up database...");
            DatabaseConnection db = DatabaseConnection.getInstance();

            // Check if database connection is available
            if (!db.isConnected()) {
                System.err.println("Warning: MongoDB connection is not available.");
                System.err.println("The application will start with limited functionality.");
                System.err.println("Please check your network connection and MongoDB configuration.");
                System.err.println();
            } else {
                InitializeSampleData dataInit = new InitializeSampleData();

                long hospitalCount = db.hospitalDataCollection.countDocuments();
                if (hospitalCount == 0) {
                    // Full initialization if no hospitals exist
                    System.out.println("No hospitals found. Initializing all sample data...");
                    dataInit.initializeAll();
                } else {
                    // Ensure hospitals and doctors exist even if some data is present
                    System.out.println("Hospital data present (" + hospitalCount + " records). Ensuring all required data exists...");
                    dataInit.initializeHospitals();
                    dataInit.initializeDoctors();
                    dataInit.ensureMinimumDoctorsPerSpeciality(10);
                    // Verify and fix any missing doctors
                    dataInit.verifyDoctorsExist();
                    // Ensure admin credentials exist
                    dataInit.initializeAdmin();
                    dataInit.addDemoCredentials();
                    System.out.println("Data verification complete!");
                }
                System.out.println();
            }
            
            int port = 8080;
            if (args.length > 0) {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid port number, using default 8080");
                }
            }
            
            HospitalWebServer server = new HospitalWebServer(port);
            server.start();
            
            System.out.println("\nPress Ctrl+C to stop the server.");
            
            // Keep the server running
            Thread.currentThread().join();
            
        } catch (IOException e) {
            System.err.println("Error starting web server: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("\nServer stopped.");
        }
    }
}
