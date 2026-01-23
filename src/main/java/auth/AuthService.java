package auth;

import db.DatabaseConnection;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication service for user login and session management.
 * Equivalent to Python Flask session and bcrypt password hashing.
 */
public class AuthService {
    private DatabaseConnection db;
    // In-memory session storage (equivalent to Flask session)
    private Map<String, SessionData> sessions;
    
    public AuthService() {
        this.db = DatabaseConnection.getInstance();
        this.sessions = new HashMap<>();
    }
    
    /**
     * User login authentication.
     * Equivalent to Python: user_login() function.
     * 
     * @param username Username
     * @param password Plain text password
     * @return Session ID if successful, null if failed
     */
    public String userLogin(String username, String password) {
        MongoCollection<Document> usersCollection = db.usersCollection;
        
        // Find user by username
        // Equivalent to Python: users_collection.find_one({'username': username})
        Document query = new Document("username", username);
        Document user = usersCollection.find(query).first();
        
        if (user != null) {
            // Check password (simplified - in production use BCrypt)
            // Equivalent to Python: bcrypt.check_password_hash(user['password'], password)
            String storedPassword = user.getString("password");
            
            // For now, simple comparison (should use BCrypt in production)
            // In real implementation, use BCrypt.checkpw() from jBCrypt library
            if (verifyPassword(password, storedPassword)) {
                // Create session
                String sessionId = generateSessionId();
                SessionData sessionData = new SessionData();
                sessionData.username = username;
                sessionData.role = "user";
                sessions.put(sessionId, sessionData);
                
                return sessionId;
            }
        }
        
        return null; // Login failed
    }
    
    /**
     * Admin login authentication.
     * 
     * @param username Admin username
     * @param password Admin password
     * @return Session ID if successful, null if failed
     */
    public String adminLogin(String username, String password) {
        MongoCollection<Document> adminCollection = db.adminCollection;
        
        Document query = new Document("username", username);
        Document admin = adminCollection.find(query).first();
        
        if (admin != null) {
            String storedPassword = admin.getString("password");
            if (verifyPassword(password, storedPassword)) {
                String sessionId = generateSessionId();
                SessionData sessionData = new SessionData();
                sessionData.username = username;
                sessionData.role = "admin";
                sessions.put(sessionId, sessionData);
                
                return sessionId;
            }
        }
        
        return null;
    }
    
    /**
     * Check if session is valid and user has required role.
     * Equivalent to Python: @login_required(role) decorator.
     * 
     * @param sessionId Session ID from cookie/header
     * @param requiredRole Required role ('user', 'admin', etc.)
     * @return SessionData if valid, null if invalid
     */
    public SessionData validateSession(String sessionId, String requiredRole) {
        if (sessionId == null) {
            return null;
        }
        
        SessionData sessionData = sessions.get(sessionId);
        if (sessionData != null && sessionData.role.equals(requiredRole)) {
            return sessionData;
        }
        
        return null;
    }
    
    /**
     * Logout by removing session.
     * 
     * @param sessionId Session ID to invalidate
     */
    public void logout(String sessionId) {
        if (sessionId != null) {
            sessions.remove(sessionId);
        }
    }
    
    /**
     * Register a new user.
     * Equivalent to Python: user_register() function.
     * 
     * @param name Full name
     * @param username Username
     * @param email Email
     * @param phone Phone number
     * @param password Plain text password
     * @return true if successful, false if username/email already exists
     */
    public boolean registerUser(String name, String username, String email, 
                               String phone, String password) {
        MongoCollection<Document> usersCollection = db.usersCollection;
        
        // Check if username exists
        Document usernameQuery = new Document("username", username.trim());
        Document existingUser = usersCollection.find(usernameQuery).first();
        if (existingUser != null) {
            return false; // Username already exists
        }
        
        // Check if email exists
        Document emailQuery = new Document("email", email);
        Document existingEmail = usersCollection.find(emailQuery).first();
        if (existingEmail != null) {
            return false; // Email already exists
        }
        
        // Hash password (simplified - should use BCrypt)
        String hashedPassword = hashPassword(password);
        
        // Create user document
        Document userData = new Document("name", name)
                .append("username", username.trim())
                .append("email", email)
                .append("number", phone)
                .append("password", hashedPassword);
        
        // Insert user
        usersCollection.insertOne(userData);
        
        return true;
    }
    
    /**
     * Generate a session ID.
     */
    private String generateSessionId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /**
     * Hash password (simplified - should use BCrypt library).
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verify password (simplified - should use BCrypt library).
     */
    private boolean verifyPassword(String password, String hashedPassword) {
        String hashed = hashPassword(password);
        return hashed.equals(hashedPassword);
    }
    
    /**
     * Session data class.
     */
    public static class SessionData {
        public String username;
        public String role;
    }
}
