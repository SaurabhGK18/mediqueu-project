#!/bin/bash

# Compile and Run Script for OPD Queueing Model
# This script compiles all Java files and runs the simulation

echo "Compiling Java files..."
mkdir -p out

# Compile all Java files
javac -d out src/main/java/util/RandomGenerator.java \
            src/main/java/model/Patient.java \
            src/main/java/model/Doctor.java \
            src/main/java/service/QueueManager.java \
            src/main/java/simulation/SimulationEngine.java \
            src/main/java/Main.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo ""
    echo "Running simulation..."
    echo "===================="
    java -cp out Main
else
    echo "Compilation failed!"
    exit 1
fi
