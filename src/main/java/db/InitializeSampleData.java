package db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

/**
 * Initialize sample data for hospitals, doctors, and users.
 * This ensures the application has data to work with.
 */
public class InitializeSampleData {
    private DatabaseConnection db;
    
    public InitializeSampleData() {
        this.db = DatabaseConnection.getInstance();
    }
    
    /**
     * Initialize all sample data.
     */
    public void initializeAll() {
        System.out.println("Initializing sample data...");
        initializeHospitals();
        initializeDoctors();
        // strengthen doctor coverage
        ensureMinimumDoctorsPerSpeciality(10);
        // Verify doctors were created
        verifyDoctorsExist();
        // clear existing login credentials then create admin and demo logins
        clearLoginData();
        initializeAdmin();
        addDemoCredentials();
        System.out.println("Sample data initialization complete!");
    }
    
    /**
     * Verify that doctors exist for all hospitals and specialities.
     */
    public void verifyDoctorsExist() {
        MongoCollection<Document> doctorsCollection = db.doctorsCollection;
        MongoCollection<Document> hospitalCollection = db.hospitalDataCollection;
        List<String> specialities = Arrays.asList("Cardiology", "Orthopedics", "Neurology", "Pediatrics", "General Medicine");
        
        List<Document> hospitals = hospitalCollection.find().into(new ArrayList<>());
        int missingCount = 0;
        
        for (Document hosp : hospitals) {
            String hospitalName = hosp.getString("hospital_name");
            if (hospitalName == null || hospitalName.isEmpty()) continue;
            
            for (String spec : specialities) {
                Document query = new Document("hospital_name", hospitalName).append("specialization", spec);
                long count = doctorsCollection.countDocuments(query);
                if (count == 0) {
                    System.out.println("  WARNING: Missing doctors for " + hospitalName + " - " + spec);
                    missingCount++;
                    // Auto-fix: create at least 2 doctors
                    for (int i = 1; i <= 2; i++) {
                        String docName = "Dr. " + hospitalName.split(" ")[0] + " " + spec.replaceAll(" ", "") + " " + i;
                        Document d = new Document("name", docName)
                                .append("hospital_name", hospitalName)
                                .append("specialization", spec)
                                .append("qualification", "MD");
                        doctorsCollection.insertOne(d);
                    }
                    System.out.println("    Created 2 doctors for " + hospitalName + " - " + spec);
                }
            }
        }
        
        if (missingCount > 0) {
            System.out.println("Fixed " + missingCount + " missing doctor combinations.");
        } else {
            System.out.println("All hospitals have doctors for all specialities.");
        }
    }

    /**
     * Add demo credentials to users, admin, and superadmin collections.
     * Username: demo, Password: 123
     * Also adds the requested saurabhkharsade789@gmail.com login for all roles
     */
    public void addDemoCredentials() {
        try {
            String demoUsername = "demo";
            String demoPassword = "123";
            String saurabhEmail = "saurabhkharsade789@gmail.com";
            String saurabhPassword = "123";

            // hash passwords same as AuthService.hashPassword (SHA-256 -> Base64)
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] demoHash = md.digest(demoPassword.getBytes());
            String demoHashed = java.util.Base64.getEncoder().encodeToString(demoHash);

            byte[] saurabhHash = md.digest(saurabhPassword.getBytes());
            String saurabhHashed = java.util.Base64.getEncoder().encodeToString(saurabhHash);

            // users collection
            MongoCollection<Document> users = db.usersCollection;

            // Add demo user
            Document existingUser = users.find(new Document("username", demoUsername)).first();
            if (existingUser == null) {
                Document u = new Document("name", "Demo User")
                        .append("username", demoUsername)
                        .append("email", "demo@example.com")
                        .append("number", "0000000000")
                        .append("password", demoHashed);
                users.insertOne(u);
            }

            // Add saurabh user
            Document existingSaurabhUser = users.find(new Document("email", saurabhEmail)).first();
            if (existingSaurabhUser == null) {
                Document su = new Document("name", "Saurabh Kharsade")
                        .append("username", saurabhEmail)
                        .append("email", saurabhEmail)
                        .append("number", "9876543210")
                        .append("password", saurabhHashed);
                users.insertOne(su);
            }

            // admin collection
            MongoCollection<Document> admins = db.adminCollection;

            // Add demo admin
            Document existingAdmin = admins.find(new Document("username", demoUsername)).first();
            if (existingAdmin == null) {
                Document a = new Document("username", demoUsername)
                        .append("password", demoHashed)
                        .append("name", "Demo Admin")
                        .append("email", "demo-admin@example.com");
                admins.insertOne(a);
            }

            // Add saurabh admin
            Document existingSaurabhAdmin = admins.find(new Document("username", saurabhEmail)).first();
            if (existingSaurabhAdmin == null) {
                Document sa = new Document("username", saurabhEmail)
                        .append("password", saurabhHashed)
                        .append("name", "Saurabh Kharsade (Admin)")
                        .append("email", saurabhEmail);
                admins.insertOne(sa);
            }

            // superadmin collection
            MongoCollection<Document> superadmins = db.superadminCollection;

            // Add demo superadmin
            Document existingSuper = superadmins.find(new Document("username", demoUsername)).first();
            if (existingSuper == null) {
                Document s = new Document("username", demoUsername)
                        .append("password", demoHashed)
                        .append("name", "Demo Superadmin")
                        .append("email", "demo-super@example.com");
                superadmins.insertOne(s);
            }

            // Add saurabh superadmin
            Document existingSaurabhSuper = superadmins.find(new Document("username", saurabhEmail)).first();
            if (existingSaurabhSuper == null) {
                Document ss = new Document("username", saurabhEmail)
                        .append("password", saurabhHashed)
                        .append("name", "Saurabh Kharsade (Superadmin)")
                        .append("email", saurabhEmail);
                superadmins.insertOne(ss);
            }

            // Also add a demo doctor user record in users collection for convenience
            String demoDoctorUsername = "demo_doctor";
            Document existingDocUser = users.find(new Document("username", demoDoctorUsername)).first();
            if (existingDocUser == null) {
                Document du = new Document("name", "Demo Doctor")
                        .append("username", demoDoctorUsername)
                        .append("email", "demo-doctor@example.com")
                        .append("number", "0000000000")
                        .append("password", demoHashed);
                users.insertOne(du);
            }

            // Add saurabh doctor user
            String saurabhDoctorUsername = "saurabh_doctor";
            Document existingSaurabhDocUser = users.find(new Document("username", saurabhDoctorUsername)).first();
            if (existingSaurabhDocUser == null) {
                Document sdu = new Document("name", "Dr. Saurabh Kharsade")
                        .append("username", saurabhDoctorUsername)
                        .append("email", saurabhEmail)
                        .append("number", "9876543210")
                        .append("password", saurabhHashed);
                users.insertOne(sdu);
            }

            System.out.println("Demo credentials ensured (demo / 123)");
            System.out.println("Saurabh credentials added for all roles: " + saurabhEmail + " / 123");
        } catch (Exception e) {
            System.err.println("Error adding demo credentials: " + e.getMessage());
        }
    }
    
    /**
     * Initialize sample hospitals.
     */
    public void initializeHospitals() {
        MongoCollection<Document> hospitalCollection = db.hospitalDataCollection;
        // Ensure required hospitals exist, insert only missing ones
        List<String> requiredHospitals = Arrays.asList(
            "B.J. Medical College & Hospital",
            "AIIMS Delhi",
            "Apollo Hospitals",
            "Fortis Healthcare",
            "Max Healthcare",
            "Sassoon General Hospital"
        );

        int inserted = 0;
        for (String hName : requiredHospitals) {
            Document existing = hospitalCollection.find(new Document("hospital_name", hName)).first();
            if (existing == null) {
                Document hosp = new Document("hospital_name", hName)
                        .append("address", "")
                        .append("phone", "");
                hospitalCollection.insertOne(hosp);
                inserted++;
            }
        }

        if (inserted > 0) {
            System.out.println("Inserted " + inserted + " missing hospitals");
        } else {
            System.out.println("All required hospitals already present");
        }
    }
    
    /**
     * Initialize sample doctors for all hospitals.
     */
    public void initializeDoctors() {
        MongoCollection<Document> doctorsCollection = db.doctorsCollection;
        // Ensure at least 2 doctors per speciality for every hospital present in hospital_data
        List<String> specialities = Arrays.asList("Cardiology", "Orthopedics", "Neurology", "Pediatrics", "General Medicine");

        MongoCollection<Document> hospitalCollection = db.hospitalDataCollection;
        List<Document> hospitals = hospitalCollection.find().into(new ArrayList<>());

        int totalInserted = 0;
        for (Document hosp : hospitals) {
            String hospitalName = hosp.getString("hospital_name");
            if (hospitalName == null || hospitalName.isEmpty()) continue;

            for (String spec : specialities) {
                Document query = new Document("hospital_name", hospitalName).append("specialization", spec);
                long count = doctorsCollection.countDocuments(query);
                int needed = (int)Math.max(0, 2 - count);
                if (needed > 0) {
                    System.out.println("  Adding " + needed + " doctor(s) for " + hospitalName + " - " + spec);
                }
                for (int i = 1; i <= needed; i++) {
                    String docName = "Dr. " + hospitalName.split(" ")[0] + " " + spec.replaceAll(" ", "") + " " + (count + i);
                    Document d = new Document("name", docName)
                            .append("hospital_name", hospitalName)
                            .append("specialization", spec)
                            .append("qualification", "MD");
                    doctorsCollection.insertOne(d);
                    totalInserted++;
                }
            }
        }

        if (totalInserted > 0) {
            System.out.println("Inserted " + totalInserted + " missing doctors to cover all hospitals and specialities");
        } else {
            System.out.println("All hospitals already have doctors for required specialities");
        }
    }

    /**
     * Ensure there are at least `minPerSpeciality` doctors for each speciality in every hospital.
     */
    public void ensureMinimumDoctorsPerSpeciality(int minPerSpeciality) {
        MongoCollection<Document> doctorsCollection = db.doctorsCollection;
        List<String> specialities = Arrays.asList("Cardiology", "Orthopedics", "Neurology", "Pediatrics", "General Medicine");

        MongoCollection<Document> hospitalCollection = db.hospitalDataCollection;
        List<Document> hospitals = hospitalCollection.find().into(new ArrayList<>());

        int totalInserted = 0;
        for (Document hosp : hospitals) {
            String hospitalName = hosp.getString("hospital_name");
            if (hospitalName == null || hospitalName.isEmpty()) continue;

            for (String spec : specialities) {
                Document query = new Document("hospital_name", hospitalName).append("specialization", spec);
                long count = doctorsCollection.countDocuments(query);
                int needed = (int)Math.max(0, minPerSpeciality - count);
                if (needed > 0) {
                    System.out.println("  Ensuring " + minPerSpeciality + " doctors for " + hospitalName + " - " + spec + " (adding " + needed + ")");
                }
                for (int i = 1; i <= needed; i++) {
                    String docName = "Dr. " + hospitalName.split(" ")[0] + " " + spec.replaceAll(" ", "") + " " + (count + i);
                    Document d = new Document("name", docName)
                            .append("hospital_name", hospitalName)
                            .append("specialization", spec)
                            .append("qualification", "MD");
                    doctorsCollection.insertOne(d);
                    totalInserted++;
                }
            }
        }

        if (totalInserted > 0) {
            System.out.println("ensureMinimumDoctorsPerSpeciality: Inserted " + totalInserted + " doctors to reach " + minPerSpeciality + " per speciality");
        } else {
            System.out.println("ensureMinimumDoctorsPerSpeciality: All hospitals already have at least " + minPerSpeciality + " doctors per speciality");
        }
    }

    /**
     * Remove all login-related documents from the database: users, admin, and superadmin collections.
     * Use with caution — this will delete all user/admin credentials stored in the DB.
     */
    public void clearLoginData() {
        try {
            long usersDeleted = db.usersCollection.deleteMany(new Document()).getDeletedCount();
            long adminDeleted = db.adminCollection.deleteMany(new Document()).getDeletedCount();
            long superadminDeleted = db.superadminCollection.deleteMany(new Document()).getDeletedCount();
            System.out.println("Cleared login data: users=" + usersDeleted + ", admin=" + adminDeleted + ", superadmin=" + superadminDeleted);
        } catch (Exception e) {
            System.err.println("Error clearing login data: " + e.getMessage());
        }
    }
    
    /**
     * Initialize admin user.
     */
    public void initializeAdmin() {
        MongoCollection<Document> adminCollection = db.adminCollection;
        
        // Check if admin already exists
        Document existingAdmin = adminCollection.find(new Document("username", "admin")).first();
        if (existingAdmin != null) {
            System.out.println("Admin user already exists, skipping...");
            return;
        }
        
        // Create default admin (password: admin123)
        // Hash password same as AuthService.hashPassword (SHA-256 -> Base64)
        try {
            String adminPassword = "admin123";
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(adminPassword.getBytes());
            String hashedPassword = java.util.Base64.getEncoder().encodeToString(hash);
            
            Document admin = new Document("username", "admin")
                    .append("password", hashedPassword)
                    .append("name", "System Administrator")
                    .append("email", "admin@hms.com");
            
            adminCollection.insertOne(admin);
            System.out.println("Created default admin user (username: admin, password: admin123)");
        } catch (Exception e) {
            System.err.println("Error creating admin user: " + e.getMessage());
        }
    }
}
