package web;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import db.DatabaseConnection;
import auth.AuthService;
import service.AppointmentService;
import service.QueueService;
import org.bson.Document;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Main web server for Hospital Management System.
 * Equivalent to Python Flask application with all routes.
 */
public class HospitalWebServer {
    private HttpServer server;
    private AuthService authService;
    private AppointmentService appointmentService;
    private QueueService queueService;
    
    public HospitalWebServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));
        
        // Initialize services
        authService = new AuthService();
        appointmentService = new AppointmentService();
        queueService = new QueueService();
        
        // Setup routes (equivalent to Flask @app.route)
        setupRoutes();
    }
    
    private void setupRoutes() {
        // Landing page
        server.createContext("/", this::handleLanding);
        
        // User authentication routes
        server.createContext("/user_login", this::handleUserLogin);
        server.createContext("/user_register", this::handleUserRegister);
        server.createContext("/user_app", this::handleUserApp);
        
        // Appointment routes
        server.createContext("/appointment", this::handleAppointment);
        server.createContext("/get-doctors", this::handleGetDoctors);
        server.createContext("/confirmation", this::handleConfirmation);
        
        // Admin routes
        server.createContext("/admin_login", this::handleAdminLogin);
        server.createContext("/admin/", this::handleAdminDashboard);
        
        // Logout route
        server.createContext("/logout", this::handleLogout);
        
        // Static file handler for CSS/JS
        server.createContext("/static", this::handleStatic);
    }
    
    /**
     * Handle landing page (GET/POST).
     * Equivalent to Python: @app.route('/', methods=['GET', 'POST'])
     */
    private void handleLanding(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("POST")) {
            // Handle contact form submission
            String requestBody = readRequestBody(exchange);
            Map<String, String> formData = parseFormData(requestBody);
            
            // Save contact data to database
            Document contactData = new Document("name", formData.getOrDefault("name", ""))
                    .append("email", formData.getOrDefault("email", ""))
                    .append("number", formData.getOrDefault("number", ""))
                    .append("comment", formData.getOrDefault("comment", ""));
            
            DatabaseConnection.getInstance().contactCollection.insertOne(contactData);
        }
        
        String html = getLandingPageHTML();
        sendResponse(exchange, 200, html, "text/html");
    }
    
    /**
     * Handle user login (GET/POST).
     * Equivalent to Python: @app.route('/user_login', methods=['POST', 'GET'])
     */
    private void handleUserLogin(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("POST")) {
            String requestBody = readRequestBody(exchange);
            Map<String, String> formData = parseFormData(requestBody);
            
            String username = formData.get("username");
            String password = formData.get("password");
            
            String sessionId = authService.userLogin(username, password);
            
            if (sessionId != null) {
                // Set session cookie and redirect
                String redirect = "/user_app";
                exchange.getResponseHeaders().add("Set-Cookie", "session_id=" + sessionId + "; Path=/");
                exchange.getResponseHeaders().add("Location", redirect);
                exchange.sendResponseHeaders(302, 0);
                exchange.close();
                return;
            } else {
                // Login failed
                String html = getUserLoginHTML("Incorrect username or password");
                sendResponse(exchange, 200, html, "text/html");
                return;
            }
        }
        
        // GET request - show login form
        String html = getUserLoginHTML(null);
        sendResponse(exchange, 200, html, "text/html");
    }
    
    /**
     * Handle user registration (GET/POST).
     * Equivalent to Python: @app.route('/user_register', methods=['GET', 'POST'])
     */
    private void handleUserRegister(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("POST")) {
            String requestBody = readRequestBody(exchange);
            Map<String, String> formData = parseFormData(requestBody);
            
            String name = formData.get("name");
            String username = formData.get("username");
            String email = formData.get("email");
            String phone = formData.get("phone");
            String password = formData.get("password");
            
            boolean success = authService.registerUser(name, username, email, phone, password);
            
            if (success) {
                // Redirect to login
                exchange.getResponseHeaders().add("Location", "/user_login");
                exchange.sendResponseHeaders(302, 0);
                exchange.close();
                return;
            } else {
                String html = getUserRegisterHTML("Username or email already exists");
                sendResponse(exchange, 200, html, "text/html");
                return;
            }
        }
        
        // GET request - show registration form
        String html = getUserRegisterHTML(null);
        sendResponse(exchange, 200, html, "text/html");
    }
    
    /**
     * Handle user appointments page.
     * Equivalent to Python: @app.route('/user_app', methods=['GET', 'POST'])
     */
    private void handleUserApp(HttpExchange exchange) throws IOException {
        String sessionId = getSessionId(exchange);
        AuthService.SessionData session = authService.validateSession(sessionId, "user");
        
        if (session == null) {
            exchange.getResponseHeaders().add("Location", "/user_login");
            exchange.sendResponseHeaders(302, 0);
            exchange.close();
            return;
        }
        
        List<Document> appointments = appointmentService.getUserAppointments(session.username);
        String html = getUserAppHTML(session.username, appointments);
        sendResponse(exchange, 200, html, "text/html");
    }
    
    /**
     * Handle appointment booking (GET/POST).
     * Equivalent to Python: @app.route('/appointment', methods=['POST', 'GET'])
     */
    private void handleAppointment(HttpExchange exchange) throws IOException {
        String sessionId = getSessionId(exchange);
        AuthService.SessionData session = authService.validateSession(sessionId, "user");
        
        if (session == null) {
            exchange.getResponseHeaders().add("Location", "/user_login");
            exchange.sendResponseHeaders(302, 0);
            exchange.close();
            return;
        }
        
        if (exchange.getRequestMethod().equals("POST")) {
            try {
                String requestBody = readRequestBody(exchange);
                Map<String, String> formData = parseFormData(requestBody);
                
                // Extract form data with null checks
                String name = formData.getOrDefault("name", "").trim();
                String number = formData.getOrDefault("number", "").trim();
                String email = formData.getOrDefault("email", "").trim();
                String address = formData.getOrDefault("Address", "").trim();
                String appointmentDate = formData.getOrDefault("dat", "").trim();
                String timeSlot = formData.getOrDefault("timeSlot", "").trim();
                String speciality = formData.getOrDefault("diseaseInput", "").trim();
                String diseaseDescription = formData.getOrDefault("diseaseDescription", "").trim();
                String hospitalName = formData.getOrDefault("hospital", "").trim();
                String doctorName = formData.getOrDefault("doctor", "").trim();
                
                // Book appointment
                AppointmentService.AppointmentResult result = appointmentService.bookAppointment(
                    name, session.username, number, email, address, appointmentDate,
                    timeSlot, speciality, diseaseDescription, hospitalName, doctorName);
            
                if (result.success) {
                    // Redirect to confirmation
                    exchange.getResponseHeaders().add("Location", "/confirmation?queue=" + result.queueNumber);
                    exchange.sendResponseHeaders(302, 0);
                    exchange.close();
                    return;
                } else {
                    // Show error
                    List<String> hospitals = appointmentService.getAllHospitals();
                    String html = getAppointmentHTML(hospitals, result.message);
                    sendResponse(exchange, 200, html, "text/html");
                    return;
                }
            } catch (Exception e) {
                // Handle any unexpected errors
                System.err.println("Error processing appointment booking: " + e.getMessage());
                e.printStackTrace();
                List<String> hospitals = appointmentService.getAllHospitals();
                String html = getAppointmentHTML(hospitals, "An error occurred while processing your request. Please try again.");
                sendResponse(exchange, 200, html, "text/html");
                return;
            }
        }
        
        // GET request - show appointment form
        List<String> hospitals = appointmentService.getAllHospitals();
        String html = getAppointmentHTML(hospitals, null);
        sendResponse(exchange, 200, html, "text/html");
    }
    
    /**
     * Handle get doctors API.
     * Equivalent to Python: @app.route('/get-doctors/<hospital>/<speciality>', methods=['GET'])
     */
    private void handleGetDoctors(HttpExchange exchange) throws IOException {
        try {
            String sessionId = getSessionId(exchange);
            AuthService.SessionData session = authService.validateSession(sessionId, "user");
            
            if (session == null) {
                sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}", "application/json");
                return;
            }
            
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryString(query);
            
            String hospital = params.get("hospital");
            String speciality = params.get("speciality");
            
            if (hospital == null || hospital.trim().isEmpty() || 
                speciality == null || speciality.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\":\"Missing required parameters\"}", "application/json");
                return;
            }
            
            List<String> doctors = appointmentService.getDoctorsByHospitalAndSpeciality(hospital.trim(), speciality.trim());
            
            // Return JSON response
            StringBuilder json = new StringBuilder("{\"doctors\":[");
            for (int i = 0; i < doctors.size(); i++) {
                if (i > 0) json.append(",");
                // Escape JSON special characters
                String doctorName = doctors.get(i).replace("\\", "\\\\").replace("\"", "\\\"");
                json.append("\"").append(doctorName).append("\"");
            }
            json.append("]}");
            
            sendResponse(exchange, 200, json.toString(), "application/json");
        } catch (Exception e) {
            System.err.println("Error in get-doctors endpoint: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}", "application/json");
        }
    }
    
    /**
     * Handle confirmation page.
     * Equivalent to Python: @app.route('/confirmation', methods=['POST', 'GET'])
     */
    private void handleConfirmation(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQueryString(query);
        String queueNumber = params.getOrDefault("queue", "N/A");
        
        String html = getConfirmationHTML(queueNumber);
        sendResponse(exchange, 200, html, "text/html");
    }
    
    /**
     * Handle admin login.
     * Equivalent to Python: @app.route('/admin_login', methods=["GET", "POST"])
     */
    private void handleAdminLogin(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("POST")) {
            String requestBody = readRequestBody(exchange);
            Map<String, String> formData = parseFormData(requestBody);
            
            String username = formData.get("username");
            String password = formData.get("password");
            
            String sessionId = authService.adminLogin(username, password);
            
            if (sessionId != null) {
                exchange.getResponseHeaders().add("Set-Cookie", "session_id=" + sessionId + "; Path=/");
                exchange.getResponseHeaders().add("Location", "/admin/");
                exchange.sendResponseHeaders(302, 0);
                exchange.close();
                return;
            } else {
                // Login failed
                String html = getAdminLoginHTML("Incorrect username or password");
                sendResponse(exchange, 200, html, "text/html");
                return;
            }
        }
        
        // GET request - show login form
        String html = getAdminLoginHTML(null);
        sendResponse(exchange, 200, html, "text/html");
    }
    
    /**
     * Handle admin dashboard.
     * Equivalent to Python: @app.route('/admin/', methods=['GET', 'POST'])
     */
    private void handleAdminDashboard(HttpExchange exchange) throws IOException {
        String sessionId = getSessionId(exchange);
        AuthService.SessionData session = authService.validateSession(sessionId, "admin");
        
        if (session == null) {
            exchange.getResponseHeaders().add("Location", "/admin_login");
            exchange.sendResponseHeaders(302, 0);
            exchange.close();
            return;
        }
        
        String html = getAdminDashboardHTML(session.username);
        sendResponse(exchange, 200, html, "text/html");
    }
    
    /**
     * Handle logout.
     */
    private void handleLogout(HttpExchange exchange) throws IOException {
        String sessionId = getSessionId(exchange);
        if (sessionId != null) {
            authService.logout(sessionId);
        }
        // Clear cookie and redirect to home
        exchange.getResponseHeaders().add("Set-Cookie", "session_id=; Path=/; Max-Age=0");
        exchange.getResponseHeaders().add("Location", "/");
        exchange.sendResponseHeaders(302, 0);
        exchange.close();
    }
    
    /**
     * Handle static files.
     */
    private void handleStatic(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 404, "Not Found", "text/plain");
    }
    
    // Helper methods
    
    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
    
    private Map<String, String> parseFormData(String formData) {
        Map<String, String> result = new HashMap<>();
        if (formData == null || formData.isEmpty()) {
            return result;
        }
        
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                try {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    result.put(key, value);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        return result;
    }
    
    private Map<String, String> parseQueryString(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return result;
        }
        return parseFormData(query);
    }
    
    private String getSessionId(HttpExchange exchange) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                String[] parts = cookie.split(";");
                for (String part : parts) {
                    String[] keyValue = part.split("=", 2);
                    if (keyValue.length == 2 && keyValue[0].trim().equals("session_id")) {
                        return keyValue[1].trim();
                    }
                }
            }
        }
        return null;
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
    
    // HTML generation methods (simplified versions)
    
    private String getLandingPageHTML() {
        return "<!DOCTYPE html><html><head><title>Hospital Management System</title>" +
               "<style>body{font-family:Arial;max-width:800px;margin:50px auto;padding:20px;}" +
               "form{margin:20px 0;}input,textarea{width:100%;padding:10px;margin:5px 0;}" +
               "button{background:#007bff;color:white;padding:10px 20px;border:none;cursor:pointer;}" +
               "</style></head><body>" +
               "<h1>Hospital Management System</h1>" +
               "<p><a href='/user_login'>User Login</a> | <a href='/user_register'>Register</a> | " +
               "<a href='/appointment'>Book Appointment</a></p>" +
               "<h2>Contact Us</h2>" +
               "<form method='POST' action='/'>" +
               "<input type='text' name='name' placeholder='Name' required><br>" +
               "<input type='email' name='email' placeholder='Email' required><br>" +
               "<input type='text' name='number' placeholder='Phone' required><br>" +
               "<textarea name='comment' placeholder='Comment' required></textarea><br>" +
               "<button type='submit'>Submit</button>" +
               "</form></body></html>";
    }
    
    private String getUserLoginHTML(String error) {
        String errorMsg = error != null ? "<p style='color:red;'>" + error + "</p>" : "";
        return "<!DOCTYPE html><html><head><title>User Login</title>" +
               "<style>body{font-family:Arial;max-width:400px;margin:50px auto;padding:20px;}" +
               "input{width:100%;padding:10px;margin:5px 0;}button{background:#007bff;color:white;padding:10px 20px;border:none;cursor:pointer;width:100%;}" +
               "</style></head><body>" +
               "<h1>User Login</h1>" + errorMsg +
               "<form method='POST' action='/user_login'>" +
               "<input type='text' name='username' placeholder='Username' required><br>" +
               "<input type='password' name='password' placeholder='Password' required><br>" +
               "<button type='submit'>Login</button>" +
               "</form><p><a href='/user_register'>Register here</a></p>" +
               "</body></html>";
    }
    
    private String getUserRegisterHTML(String error) {
        String errorMsg = error != null ? "<p style='color:red;'>" + error + "</p>" : "";
        return "<!DOCTYPE html><html><head><title>User Registration</title>" +
               "<style>body{font-family:Arial;max-width:400px;margin:50px auto;padding:20px;}" +
               "input{width:100%;padding:10px;margin:5px 0;}button{background:#28a745;color:white;padding:10px 20px;border:none;cursor:pointer;width:100%;}" +
               "</style></head><body>" +
               "<h1>User Registration</h1>" + errorMsg +
               "<form method='POST' action='/user_register'>" +
               "<input type='text' name='name' placeholder='Full Name' required><br>" +
               "<input type='text' name='username' placeholder='Username' required><br>" +
               "<input type='email' name='email' placeholder='Email' required><br>" +
               "<input type='text' name='phone' placeholder='Phone' required><br>" +
               "<input type='password' name='password' placeholder='Password' required><br>" +
               "<button type='submit'>Register</button>" +
               "</form><p><a href='/user_login'>Login here</a></p>" +
               "</body></html>";
    }
    
    private String getUserAppHTML(String username, List<Document> appointments) {
        StringBuilder html = new StringBuilder("<!DOCTYPE html><html><head><title>My Appointments</title>" +
                "<style>body{font-family:Arial;max-width:900px;margin:50px auto;padding:20px;}" +
                "table{width:100%;border-collapse:collapse;}th,td{padding:10px;border:1px solid #ddd;}" +
                "th{background:#007bff;color:white;}tr:nth-child(even){background:#f2f2f2;}" +
                ".no-appointments{text-align:center;padding:40px;color:#666;}</style></head><body>");
        html.append("<h1>Welcome, ").append(escapeHtml(username)).append("</h1>");
        html.append("<p><a href='/appointment'>Book New Appointment</a> | <a href='/'>Home</a> | <a href='/logout'>Logout</a></p>");
        html.append("<h2>My Appointments</h2>");
        
        if (appointments == null || appointments.isEmpty()) {
            html.append("<div class='no-appointments'><p>You have no appointments yet.</p><p><a href='/appointment'>Book your first appointment</a></p></div>");
        } else {
            html.append("<table><tr><th>Date</th><th>Time Slot</th><th>Hospital</th><th>Speciality</th><th>Doctor</th><th>Queue Number</th></tr>");
            
            for (Document appointment : appointments) {
                html.append("<tr>");
                html.append("<td>").append(escapeHtml(getStringOrEmpty(appointment, "appointment_date"))).append("</td>");
                html.append("<td>").append(escapeHtml(getStringOrEmpty(appointment, "time_slot"))).append("</td>");
                html.append("<td>").append(escapeHtml(getStringOrEmpty(appointment, "hospital_name"))).append("</td>");
                html.append("<td>").append(escapeHtml(getStringOrEmpty(appointment, "speciality"))).append("</td>");
                html.append("<td>").append(escapeHtml(getStringOrEmpty(appointment, "appointed_doc"))).append("</td>");
                Object queueNum = appointment.get("queue_number");
                html.append("<td>").append(queueNum != null ? queueNum.toString() : "N/A").append("</td>");
                html.append("</tr>");
            }
            
            html.append("</table>");
        }
        
        html.append("</body></html>");
        return html.toString();
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    private String getStringOrEmpty(Document doc, String key) {
        String value = doc.getString(key);
        return value != null ? value : "";
    }
    
    private String getAppointmentHTML(List<String> hospitals, String error) {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(15);
        String todayStr = today.format(DateTimeFormatter.ISO_DATE);
        String maxDateStr = maxDate.format(DateTimeFormatter.ISO_DATE);
        
        String errorMsg = error != null ? "<p style='color:red;'>" + error + "</p>" : "";
        
        StringBuilder hospitalOptions = new StringBuilder();
        for (String hospital : hospitals) {
            hospitalOptions.append("<option value='").append(hospital).append("'>").append(hospital).append("</option>");
        }
        
        // Available specialities
        String specialities = "<option value='Cardiology'>Cardiology</option>" +
                            "<option value='Orthopedics'>Orthopedics</option>" +
                            "<option value='Neurology'>Neurology</option>" +
                            "<option value='Pediatrics'>Pediatrics</option>" +
                            "<option value='General Medicine'>General Medicine</option>";
        
        return "<!DOCTYPE html><html><head><title>Book Appointment</title>" +
               "<style>body{font-family:Arial;max-width:600px;margin:50px auto;padding:20px;}" +
               "input,select,textarea{width:100%;padding:10px;margin:5px 0;box-sizing:border-box;}" +
               "button{background:#007bff;color:white;padding:10px 20px;border:none;cursor:pointer;width:100%;}" +
               "#doctorSelect{width:100%;padding:10px;margin:5px 0;box-sizing:border-box;}" +
               "</style>" +
               "<script>" +
               "function loadDoctors() {" +
               "  var hospital = document.getElementById('hospital').value;" +
               "  var speciality = document.getElementById('diseaseInput').value;" +
               "  var doctorSelect = document.getElementById('doctorSelect');" +
               "  if (!hospital || !speciality) { doctorSelect.innerHTML='<option>Select hospital and speciality first</option>'; return; }" +
               "  doctorSelect.innerHTML = '<option>Loading...</option>';" +
               "  fetch('/get-doctors?hospital=' + encodeURIComponent(hospital) + '&speciality=' + encodeURIComponent(speciality))" +
               "    .then(r => { if (!r.ok) throw new Error('Failed to load doctors'); return r.json(); })" +
               "    .then(data => {" +
               "      doctorSelect.innerHTML = '';" +
               "      if (data.doctors && data.doctors.length > 0) {" +
               "        data.doctors.forEach(doc => {" +
               "          var opt = document.createElement('option');" +
               "          opt.value = doc; opt.text = doc; doctorSelect.appendChild(opt);" +
               "        });" +
               "      } else {" +
               "        var opt = document.createElement('option');" +
               "        opt.text = 'No doctors available'; doctorSelect.appendChild(opt);" +
               "      }" +
               "    })" +
               "    .catch(err => {" +
               "      doctorSelect.innerHTML = '<option>Error loading doctors. Please refresh the page.</option>';" +
               "      console.error('Error loading doctors:', err);" +
               "    });" +
               "}" +
               "document.addEventListener('DOMContentLoaded', function() {" +
               "  document.getElementById('hospital').addEventListener('change', loadDoctors);" +
               "  document.getElementById('diseaseInput').addEventListener('change', loadDoctors);" +
               "});" +
               "</script></head><body>" +
               "<h1>Book Appointment</h1>" + errorMsg +
               "<form method='POST' action='/appointment'>" +
               "<input type='text' name='name' placeholder='Patient Name' required><br>" +
               "<input type='text' name='number' placeholder='Phone Number' required><br>" +
               "<input type='email' name='email' placeholder='Email' required><br>" +
               "<input type='text' name='Address' placeholder='Address' required><br>" +
               "<label>Hospital:</label><select name='hospital' id='hospital' required>" + hospitalOptions.toString() + "</select><br>" +
               "<label>Speciality:</label><select name='diseaseInput' id='diseaseInput' required>" + specialities + "</select><br>" +
               "<label>Doctor:</label><select name='doctor' id='doctorSelect' required><option>Select hospital and speciality first</option></select><br>" +
               "<label>Appointment Date:</label><input type='date' name='dat' min='" + todayStr + "' max='" + maxDateStr + "' required><br>" +
               "<label>Time Slot:</label><select name='timeSlot' required>" +
               "<option value='09:00-10:00'>09:00-10:00</option>" +
               "<option value='10:00-11:00'>10:00-11:00</option>" +
               "<option value='11:00-12:00'>11:00-12:00</option>" +
               "<option value='14:00-15:00'>14:00-15:00</option>" +
               "<option value='15:00-16:00'>15:00-16:00</option>" +
               "</select><br>" +
               "<label>Disease Description:</label><textarea name='diseaseDescription' placeholder='Disease Description' required></textarea><br>" +
               "<button type='submit'>Book Appointment</button>" +
               "</form><p><a href='/user_app'>Back to Appointments</a></p>" +
               "</body></html>";
    }
    
    private String getConfirmationHTML(String queueNumber) {
        String safeQueueNumber = escapeHtml(queueNumber != null ? queueNumber : "N/A");
        return "<!DOCTYPE html><html><head><title>Appointment Confirmed</title>" +
               "<style>body{font-family:Arial;max-width:600px;margin:50px auto;padding:20px;text-align:center;}" +
               ".success{color:#28a745;font-size:24px;margin:20px 0;}" +
               ".queue{font-size:48px;color:#007bff;font-weight:bold;margin:20px 0;}" +
               "a{color:#007bff;text-decoration:none;margin:0 10px;}a:hover{text-decoration:underline;}" +
               "</style></head><body>" +
               "<h1>Appointment Confirmed!</h1>" +
               "<div class='success'>Your appointment has been successfully booked.</div>" +
               "<div>Your Queue Number is:</div>" +
               "<div class='queue'>" + safeQueueNumber + "</div>" +
               "<p><a href='/user_app'>View My Appointments</a> | <a href='/appointment'>Book Another</a> | <a href='/'>Home</a></p>" +
               "</body></html>";
    }
    
    private String getAdminLoginHTML(String error) {
        String errorMsg = error != null ? "<p style='color:red;'>" + error + "</p>" : "";
        return "<!DOCTYPE html><html><head><title>Admin Login</title>" +
               "<style>body{font-family:Arial;max-width:400px;margin:50px auto;padding:20px;}" +
               "input{width:100%;padding:10px;margin:5px 0;}button{background:#dc3545;color:white;padding:10px 20px;border:none;cursor:pointer;width:100%;}" +
               "</style></head><body>" +
               "<h1>Admin Login</h1>" + errorMsg +
               "<form method='POST' action='/admin_login'>" +
               "<input type='text' name='username' placeholder='Username' required><br>" +
               "<input type='password' name='password' placeholder='Password' required><br>" +
               "<button type='submit'>Login</button>" +
               "</form></body></html>";
    }
    
    private String getAdminDashboardHTML(String username) {
        return "<!DOCTYPE html><html><head><title>Admin Dashboard</title>" +
               "<style>body{font-family:Arial;max-width:900px;margin:50px auto;padding:20px;}" +
               "</style></head><body>" +
               "<h1>Admin Dashboard</h1>" +
               "<p>Welcome, " + username + "</p>" +
               "<p><a href='/'>Home</a> | <a href='/logout'>Logout</a></p>" +
               "</body></html>";
    }
    
    public void start() {
        server.start();
        System.out.println("Hospital Management System Web Server started on http://localhost:8080");
        System.out.println("Open your browser and navigate to: http://localhost:8080");
    }
    
    public void stop() {
        server.stop(0);
    }
}
