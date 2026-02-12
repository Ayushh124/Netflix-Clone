#!/bin/bash

echo "🔍 Netflix Clone Connection Test"
echo "================================"
echo ""

# Get current IP
MY_IP=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}' | head -1)
echo "✅ Your Computer IP: $MY_IP"

# Check if server is running
if lsof -ti:3002 > /dev/null; then
    echo "✅ Backend Server: Running on port 3002"
else
    echo "❌ Backend Server: NOT Running"
    echo "   Start it with: cd js_backend && node server.js"
    exit 1
fi

# Test API endpoint
echo ""
echo "🧪 Testing API Endpoint..."
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://$MY_IP:3002/)
if [ "$RESPONSE" = "404" ]; then
    echo "✅ Server is responding (404 is expected for root path)"
else
    echo "❌ Server returned: $RESPONSE"
fi

# Test registration endpoint
echo ""
echo "🧪 Testing Registration Endpoint..."
REGISTER_RESPONSE=$(curl -s -X POST http://$MY_IP:3002/auth/register \
    -H "Content-Type: application/json" \
    -d '{"email":"testuser@example.com","password":"test123"}' 2>&1)
echo "   Response: $REGISTER_RESPONSE"

# Check local.properties
echo ""
echo "📝 Checking Configuration..."
if [ -f "local.properties" ]; then
    CONFIGURED_URL=$(grep "base_url" local.properties | cut -d'=' -f2)
    echo "   Configured BASE_URL: $CONFIGURED_URL"
    
    if [[ "$CONFIGURED_URL" == *"$MY_IP"* ]]; then
        echo "✅ Configuration matches your current IP"
    else
        echo "⚠️  Configuration doesn't match current IP"
        echo "   Update local.properties to: base_url=http://$MY_IP:3002/"
    fi
else
    echo "❌ local.properties not found"
fi

echo ""
echo "📱 Next Steps:"
echo "1. Make sure your phone is on the same WiFi network"
echo "2. In Android Studio:"
echo "   - Build → Clean Project"
echo "   - Build → Rebuild Project"
echo "   - Run the app"
echo ""
echo "3. Server URL: http://$MY_IP:3002/"
