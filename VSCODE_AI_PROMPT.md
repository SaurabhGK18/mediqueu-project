# VS Code AI Prompt - Fix Doctor Availability Issue

## Copy and Paste This Prompt to VS Code AI:

```
I have a Hospital Management System Java application with a MongoDB database. When users try to book appointments, they get an error: "Doctor for the selected field is not available in [Hospital Name]. Sorry for the inconvenience."

The problem is:
1. The database doesn't have doctors for all hospitals and specialities
2. The appointment booking form needs to dynamically load doctors based on selected hospital and speciality
3. Sample data needs to be initialized when the server starts

Please fix the following:

1. **InitializeSampleData.java** - This file should create sample data:
   - Add at least 5 hospitals (including "B.J. Medical College & Hospital")
   - Add doctors for ALL hospitals with these specialities: Cardiology, Orthopedics, Neurology, Pediatrics, General Medicine
   - Each hospital should have at least 2-3 doctors per speciality
   - Make sure the data structure matches what the code expects

2. **Main.java** - Update to automatically initialize sample data on server start:
   - Import InitializeSampleData
   - Call initializeAll() before starting the web server
   - Only initialize if data doesn't exist (check first)

3. **HospitalWebServer.java** - Improve the appointment booking form:
   - Change speciality from text input to dropdown with options: Cardiology, Orthopedics, Neurology, Pediatrics, General Medicine
   - Add JavaScript to dynamically load doctors when hospital or speciality changes
   - Use the existing /get-doctors API endpoint
   - Make doctor field a dropdown that populates automatically

4. **AppointmentService.java** - Ensure getDoctorsByHospitalAndSpeciality() works correctly:
   - It should query doctors collection with hospital_name and specialization fields
   - Return list of doctor names

The MongoDB collections are:
- hospital_data (has hospital_name field)
- doctors (has name, hospital_name, specialization fields)
- appointment (has hospital_name, speciality fields)

Please ensure:
- All code compiles without errors
- Sample data is created for all hospitals
- The appointment form works with dynamic doctor loading
- The error message no longer appears when booking appointments

Fix all these issues and make sure the code is production-ready.
```

## Alternative Shorter Prompt:

```
Fix the doctor availability issue in my Hospital Management System:

1. Create InitializeSampleData.java that adds sample hospitals and doctors to MongoDB
2. Update Main.java to auto-initialize sample data on startup
3. Update HospitalWebServer.java appointment form to:
   - Use dropdown for speciality (Cardiology, Orthopedics, Neurology, Pediatrics, General Medicine)
   - Add JavaScript to dynamically load doctors based on hospital + speciality
   - Make doctor field a dropdown that auto-populates

The error "Doctor for the selected field is not available" should be fixed. All hospitals need doctors for all specialities.
```

## Step-by-Step Instructions:

1. **Open VS Code** with your project
2. **Open VS Code AI/Copilot Chat** (usually `Cmd+L` or `Ctrl+L`, or look for AI icon)
3. **Copy the prompt above** (either full or shorter version)
4. **Paste it into the AI chat**
5. **Let AI generate the fixes**
6. **Review and apply the changes**
7. **Compile and test**

## What the AI Should Do:

✅ Create/update `InitializeSampleData.java` with sample hospitals and doctors
✅ Update `Main.java` to call initialization
✅ Update `HospitalWebServer.java` to improve the appointment form
✅ Ensure all code compiles and works

## After AI Fixes:

1. **Compile**: `javac -cp "lib/*" -d out src/main/java/**/*.java`
2. **Run**: `java -cp "lib/*:out" Main`
3. **Test**: Try booking an appointment - it should work now!
