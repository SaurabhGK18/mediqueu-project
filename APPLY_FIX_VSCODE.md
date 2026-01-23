# How to Apply the Fix in VS Code

## Step-by-Step Instructions

### Step 1: Stop the Current Server
1. In VS Code, look at the **Terminal** panel (bottom of screen)
2. If the server is running, press `Ctrl+C` to stop it
3. Or close the terminal and open a new one

### Step 2: Recompile the Project
**Option A: Using VS Code Terminal**
1. Open terminal in VS Code: Press `` Ctrl+` `` (backtick) or `View → Terminal`
2. Run this command:
   ```bash
   javac -cp "lib/*" -d out src/main/java/**/*.java
   ```

**Option B: Using VS Code Tasks**
1. Press `Cmd+Shift+P` (Mac) or `Ctrl+Shift+P` (Windows/Linux)
2. Type: `Tasks: Run Task`
3. Select: `compile`

### Step 3: Restart the Server
**Option A: Using Run Button**
1. Open `Main.java` in VS Code
2. Click the **▶ Run** button above the `main` method
3. Or press `F5` to debug/run

**Option B: Using Terminal**
1. In VS Code terminal, run:
   ```bash
   java -cp "lib/*:out" Main
   ```

### Step 4: Verify the Fix
1. Wait for the server to start
2. You should see messages like:
   ```
   Setting up database...
   Initializing sample data...
   Inserted 5 hospitals
   Inserted 30 doctors
   Sample data initialization complete!
   Hospital Management System Web Server started on http://localhost:8080
   ```

3. Open browser: `http://localhost:8080`
4. Try booking an appointment:
   - Login/Register
   - Go to Book Appointment
   - Select "B.J. Medical College & Hospital"
   - Select a speciality (Cardiology, Orthopedics, etc.)
   - The doctor dropdown should populate automatically!

## What Was Fixed

✅ **Sample Data Initialization**: Added `InitializeSampleData.java`
- Creates 5 hospitals
- Creates 30+ doctors across all hospitals
- Covers all specialities: Cardiology, Orthopedics, Neurology, Pediatrics, General Medicine

✅ **Improved Appointment Form**: Updated `HospitalWebServer.java`
- Speciality is now a dropdown (not text input)
- Doctor dropdown loads automatically based on hospital + speciality
- JavaScript fetches doctors dynamically

✅ **Auto-Initialization**: Updated `Main.java`
- Automatically runs data initialization on server start
- Only creates data if it doesn't exist (won't duplicate)

## Troubleshooting

**Issue: "Address already in use"**
- Solution: Stop the server first (Ctrl+C), then restart

**Issue: Compilation errors**
- Solution: Make sure MongoDB driver JARs are in `lib/` folder
- Check: `ls lib/*.jar` should show 3 JAR files

**Issue: "No doctors available"**
- Solution: Check MongoDB connection
- The initialization should run automatically on first start
- Check server console for initialization messages

**Issue: Changes not reflecting**
- Solution: Make sure you recompiled after changes
- Run: `javac -cp "lib/*" -d out src/main/java/**/*.java`

## Quick Commands Reference

```bash
# Stop server
kill $(lsof -ti:8080) 2>/dev/null

# Compile
javac -cp "lib/*" -d out src/main/java/**/*.java

# Run
java -cp "lib/*:out" Main

# Or use Maven
mvn clean compile
mvn exec:java -Dexec.mainClass="Main"
```

## Files Changed

1. ✅ `src/main/java/db/InitializeSampleData.java` - NEW FILE
2. ✅ `src/main/java/Main.java` - UPDATED (added initialization)
3. ✅ `src/main/java/web/HospitalWebServer.java` - UPDATED (improved form)

All files are already saved and ready to use!
