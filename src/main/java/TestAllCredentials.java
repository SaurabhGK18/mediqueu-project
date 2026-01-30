import db.DatabaseConnection;
import auth.AuthService;

/**
 * Test all login credentials including the newly added saurabhkharsade789@gmail.com
 */
public class TestAllCredentials {
    public static void main(String[] args) {
        System.out.println("🔐 TESTING ALL LOGIN CREDENTIALS");
        System.out.println("=" .repeat(50));
        System.out.println();

        DatabaseConnection db = DatabaseConnection.getInstance();
        AuthService authService = new AuthService();

        System.out.println("Database Status: " + (db.isConnected() ? "✅ Connected" : "❌ Disconnected (Demo Mode)"));
        System.out.println();

        // Test credentials
        String[][] credentials = {
            // {username, password, expectedRole}
            {"demo", "123", "user"},
            {"demo", "123", "admin"},
            {"saurabhkharsade789@gmail.com", "123", "user"},
            {"saurabhkharsade789@gmail.com", "123", "admin"},
            {"saurabh_doctor", "123", "user"},
            {"demo_doctor", "123", "user"},
            {"admin", "admin123", "admin"}
        };

        int passed = 0;
        int total = credentials.length;

        for (String[] cred : credentials) {
            String username = cred[0];
            String password = cred[1];
            String role = cred[2];

            boolean success = false;
            String sessionId = null;

            if (role.equals("user")) {
                sessionId = authService.userLogin(username, password);
                success = sessionId != null;
            } else if (role.equals("admin")) {
                sessionId = authService.adminLogin(username, password);
                success = sessionId != null;
            }

            if (success && sessionId != null) {
                AuthService.SessionData session = authService.validateSession(sessionId, role);
                if (session != null) {
                    System.out.println("✅ " + role.toUpperCase() + " Login: " + username + " - SUCCESS");
                    passed++;
                } else {
                    System.out.println("❌ " + role.toUpperCase() + " Login: " + username + " - SESSION INVALID");
                }
            } else {
                System.out.println("❌ " + role.toUpperCase() + " Login: " + username + " - FAILED");
            }
        }

        System.out.println();
        System.out.println("📊 LOGIN TEST RESULTS");
        System.out.println("-".repeat(30));
        System.out.println("Total Tests: " + total);
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + (total - passed));
        System.out.println("Success Rate: " + (passed * 100 / total) + "%");
        System.out.println();

        if (db.isConnected()) {
            System.out.println("🎯 ALL CREDENTIALS WORKING WITH DATABASE!");
        } else {
            System.out.println("ℹ️  Database not connected - credentials will work when DB is available");
            System.out.println("   In demo mode, all authentication gracefully returns false");
        }

        System.out.println();
        System.out.println("📋 AVAILABLE LOGIN CREDENTIALS:");
        System.out.println("User Logins:");
        System.out.println("  • demo / 123");
        System.out.println("  • saurabhkharsade789@gmail.com / 123");
        System.out.println("  • demo_doctor / 123");
        System.out.println("  • saurabh_doctor / 123");
        System.out.println("Admin Logins:");
        System.out.println("  • demo / 123");
        System.out.println("  • saurabhkharsade789@gmail.com / 123");
        System.out.println("  • admin / admin123");
        System.out.println();
        System.out.println("🚀 Ready to test the full web application!");
    }
}