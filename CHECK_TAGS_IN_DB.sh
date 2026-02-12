#!/bin/bash

echo "🔍 Checking if tags exist in database..."
echo ""

# Test 1: Check backend is running
echo "1️⃣ Testing if backend is running..."
curl -s http://192.168.1.49:3002/ > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Backend is running on port 3002"
else
    echo "❌ Backend is NOT running! Start it with: cd js_backend && npm start"
    exit 1
fi

echo ""
echo "2️⃣ Checking tags in MySQL database..."
mysql -u root netflix_clone -e "SELECT * FROM tags;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ Tags table queried successfully"
else
    echo "⚠️  Could not query MySQL directly (may need password)"
    echo "   Try manually: mysql -u root -p netflix_clone"
    echo "   Then run: SELECT * FROM tags;"
fi

echo ""
echo "3️⃣ Testing /tags endpoint (requires login)..."
echo "   Note: This will fail with 401 if not logged in - that's expected"
curl -i http://192.168.1.49:3002/tags

echo ""
echo "📝 WHAT TO DO:"
echo "   1. Make sure backend is running: cd js_backend && npm start"
echo "   2. Check if tags exist in database"
echo "   3. If no tags, add them with: INSERT INTO tags (name) VALUES ('Action'), ('Drama'), ('Comedy');"
