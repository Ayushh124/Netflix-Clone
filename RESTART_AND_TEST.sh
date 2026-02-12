#!/bin/bash

echo "🔄 RESTARTING BACKEND WITH FEATURED ENDPOINT..."
echo ""

# Navigate to backend directory
cd /Users/user/NETFLIX_CLONE_FINAL/NetflixClone/js_backend

# Kill existing backend
echo "1️⃣ Stopping old backend..."
lsof -ti:3002 | xargs kill -9 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ Old backend stopped"
else
    echo "⚠️  No backend running on port 3002"
fi

sleep 1

# Start new backend
echo ""
echo "2️⃣ Starting backend with new /movies/featured endpoint..."
echo ""
node server.js &

# Wait for backend to start
sleep 3

# Test if backend is running
echo ""
echo "3️⃣ Testing backend..."
curl -s http://192.168.1.49:3002/ > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Backend is running on port 3002"
else
    echo "❌ Backend failed to start!"
    exit 1
fi

echo ""
echo "4️⃣ Testing /movies/featured endpoint..."
echo "   (Will show 401 if not logged in - that's normal)"
echo ""
curl -i http://192.168.1.49:3002/movies/featured 2>/dev/null | head -n 1

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ BACKEND RESTARTED SUCCESSFULLY!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📱 NOW DO THIS IN ANDROID STUDIO:"
echo "   1. Build → Clean Project"
echo "   2. Build → Rebuild Project"
echo "   3. Run app on your phone"
echo ""
echo "🎯 EXPECTED RESULT:"
echo "   Home screen shows:"
echo "   - 🌟 Verified Picks (4 videos)"
echo "   - Filter by Tags section"
echo "   - Movies section (filtered results)"
echo ""
echo "🚀 HAPPY TESTING!"
