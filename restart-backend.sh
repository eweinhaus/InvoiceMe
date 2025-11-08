#!/bin/bash

# Script to restart the backend with OAuth changes

echo "🔄 Restarting InvoiceMe Backend..."
echo ""

# Find and kill existing backend process
BACKEND_PID=$(lsof -ti:8080)
if [ -n "$BACKEND_PID" ]; then
    echo "Stopping existing backend (PID: $BACKEND_PID)..."
    kill $BACKEND_PID
    sleep 2
    
    # Force kill if still running
    if kill -0 $BACKEND_PID 2>/dev/null; then
        echo "Force killing backend..."
        kill -9 $BACKEND_PID
    fi
    echo "✅ Backend stopped"
else
    echo "No backend process found on port 8080"
fi

echo ""
echo "🚀 Starting backend..."
cd backend
mvn spring-boot:run > ../backend.log 2>&1 &

echo "✅ Backend starting in background"
echo "📋 Logs are being written to: backend.log"
echo ""
echo "To view logs: tail -f backend.log"
echo "To stop: kill \$(lsof -ti:8080)"

