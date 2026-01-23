#!/bin/bash

# Stop server script for Hospital Management System
echo "Stopping server on port 8080..."

PID=$(lsof -ti:8080)

if [ -z "$PID" ]; then
    echo "No server running on port 8080"
else
    kill $PID
    echo "Stopped server (PID: $PID)"
fi
