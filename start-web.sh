#!/bin/bash

# Start Web Server Script for OPD Queueing Model
# This script compiles all Java files and starts the web server

echo "Compiling Java files..."
mkdir -p out

# Compile all Java files
javac -d out src/main/java/util/RandomGenerator.java \
            src/main/java/model/Patient.java \
            src/main/java/model/Doctor.java \
            src/main/java/service/QueueManager.java \
            src/main/java/web/SimulationResult.java \
            src/main/java/simulation/SimulationEngine.java \
            src/main/java/web/WebServer.java \
            src/main/java/Main.java \
            src/main/java/MainWeb.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo ""
    echo "Starting web server..."
    echo "======================"
    echo ""
    echo "Web server will start on: http://localhost:8080"
    echo "Open your browser and navigate to the URL above"
    echo ""
    echo "Press Ctrl+C to stop the server"
    echo ""
    java -cp out MainWeb
else
    echo "Compilation failed!"
    exit 1
fi
