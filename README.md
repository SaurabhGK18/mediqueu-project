# Hospital Management System - Java Conversion

This is a **complete Java conversion** of the Python Flask Hospital Management System from the GitHub repository: [Queuing-models-in-OPDs-SIH-2024](https://github.com/LIKITH43/Queuing-models-in-OPDs-SIH-2024)

## Overview

This project is a Hospital Management System (HMS) that includes:
- User authentication (login/register)
- Appointment booking system
- Queue number calculation for appointments
- Time slot management
- Admin dashboard
- MongoDB database integration

## Key Features Converted from Python

### 1. **Queue Number Calculation**
- Calculates queue numbers based on appointments for the same date, time slot, hospital, and speciality
- Equivalent to Python: `calculate_queue_number()`

### 2. **Time Slot Allocation**
- Checks if time slots are full based on doctor capacity (3 appointments per doctor per slot)
- Equivalent to Python: `check_and_allocate_time_slot()`

### 3. **User Authentication**
- User login and registration
- Session management
- Role-based access (user/admin)
- Equivalent to Python Flask sessions

### 4. **Appointment Booking**
- Book appointments with queue number assignment
- View user appointments
- Get doctors by hospital and speciality

## Project Structure

```
opd-queueing-model/
├── src/main/java/
│   ├── db/
│   │   └── DatabaseConnection.java      # MongoDB connection (equivalent to Python MongoDB setup)
│   ├── auth/
│   │   └── AuthService.java             # Authentication service (equivalent to Flask login)
│   ├── service/
│   │   ├── QueueService.java            # Queue calculation (equivalent to calculate_queue_number)
│   │   └── AppointmentService.java     # Appointment management
│   ├── web/
│   │   └── HospitalWebServer.java       # Web server with all routes (equivalent to Flask app)
│   └── Main.java                        # Entry point
├── pom.xml                              # Maven dependencies
└── README.md
```

## Technologies Used

- **Java 8+**: Core Java programming
- **Java HTTP Server**: Built-in HTTP server (com.sun.net.httpserver)
- **MongoDB Java Driver**: For database connectivity
- **No External Web Frameworks**: Pure Java implementation

## Prerequisites

1. **Java Development Kit (JDK) 8 or higher**
2. **MongoDB Java Driver** (see setup below)
3. **MongoDB Database Access** (uses the same MongoDB instance as Python version)

## Setup Instructions

### Option 1: Using Maven (Recommended)

1. **Install Maven** (if not already installed):
   ```bash
   # On macOS
   brew install maven
   
   # On Linux
   sudo apt-get install maven
   
   # On Windows
   # Download from https://maven.apache.org/download.cgi
   ```

2. **Compile and Run**:
   ```bash
   cd opd-queueing-model
   mvn compile
   mvn exec:java -Dexec.mainClass="Main"
   ```

### Option 2: Manual Setup (Without Maven)

1. **Download MongoDB Java Driver**:
   - Download from: https://repo1.maven.org/maven2/org/mongodb/mongodb-driver-sync/4.11.1/
   - Place JAR files in a `lib/` directory

2. **Compile**:
   ```bash
   mkdir -p out lib
   # Download MongoDB driver JARs to lib/ directory
   javac -cp "lib/*" -d out src/main/java/**/*.java
   ```

3. **Run**:
   ```bash
   java -cp "lib/*:out" Main
   ```

See `MONGODB_SETUP.md` for detailed MongoDB driver setup instructions.

## Running the Application

After compilation, start the server:

```bash
java -cp "lib/*:out" Main
# Or with Maven:
mvn exec:java -Dexec.mainClass="Main"
```

The server will start on **http://localhost:8080**

## Available Routes

### User Routes
- `GET /` - Landing page
- `GET/POST /user_login` - User login
- `GET/POST /user_register` - User registration
- `GET /user_app` - View user appointments
- `GET/POST /appointment` - Book appointment
- `GET /get-doctors?hospital=...&speciality=...` - Get doctors API
- `GET /confirmation` - Appointment confirmation

### Admin Routes
- `GET/POST /admin_login` - Admin login
- `GET /admin/` - Admin dashboard

## Key Differences from Python Version

1. **Web Framework**: Uses Java's built-in HTTP server instead of Flask
2. **Session Management**: In-memory session storage instead of Flask sessions
3. **Template Rendering**: HTML strings instead of Jinja2 templates
4. **Password Hashing**: Simplified (should use BCrypt library for production)

## Queue Number Calculation Logic

The queue number is calculated as:
```java
count = appointments with same (date, time_slot, hospital, speciality)
queue_number = count + 1
```

This matches the Python implementation exactly.

## Time Slot Capacity

Time slots are considered full when:
```java
appointment_count >= 3 * (doctor_count / 3)
```

This ensures 3 appointments per doctor per time slot.

## Database Connection

The application connects to the same MongoDB instance as the Python version:
- **URI**: `mongodb+srv://manasranjanpradhan2004:root@hms.m7j9t.mongodb.net/`
- **Database**: `HMS`

## Notes

- This is a **direct conversion** of the Python Flask application
- All core functionality from the Python version is preserved
- HTML templates are simplified but functional
- MongoDB connection uses the same credentials as the original Python app

## Troubleshooting

**Issue: "Cannot find MongoDB driver classes"**
- Solution: Ensure MongoDB driver JAR is in classpath
- Use Maven for automatic dependency management

**Issue: "MongoDB connection failed"**
- Solution: Check network connectivity to MongoDB instance
- Verify MongoDB URI in `DatabaseConnection.java`

**Issue: "Port already in use"**
- Solution: Change port in `Main.java` or kill process using port 8080

## License

This is a conversion of the original Python project for educational purposes.
