#!/bin/bash

# Script to open project in both Cursor and VS Code

PROJECT_DIR="/Users/saurabh/opd-queueing-model"

echo "Opening project in both IDEs..."
echo "Project: $PROJECT_DIR"
echo ""

# Open in Cursor (if command exists)
if command -v cursor &> /dev/null; then
    echo "Opening in Cursor..."
    cursor "$PROJECT_DIR" &
else
    echo "Cursor command not found. Install it or open manually."
fi

# Open in VS Code (if command exists)
if command -v code &> /dev/null; then
    echo "Opening in VS Code..."
    code "$PROJECT_DIR" &
else
    echo "VS Code command not found. Install it or open manually."
fi

echo ""
echo "Both IDEs should open the project folder."
echo "Remember: Save files before switching between them!"
