#!/bin/bash
# Script to restart backend properly

echo "🔥 Restarting Netflix Clone Backend..."
echo ""

# Step 1: Kill any existing process on port 3002
echo "Step 1: Stopping old backend..."
PID=$(lsof -ti:3002)
if [ ! -z "$PID" ]; then
    echo "Found process: $PID"
    kill -9 $PID 2>/dev/null
    sleep 2
    echo "✅ Old backend stopped"
else
    echo "No backend running on port 3002"
fi

# Step 2: Start new backend
echo ""
echo "Step 2: Starting new backend..."
cd js_backend

if [ ! -d "node_modules" ]; then
    echo "⚠️  node_modules not found, running npm install..."
    npm install
fi

echo ""
echo "🚀 Starting backend..."
echo "Press CTRL+C to stop"
echo ""
npm start
