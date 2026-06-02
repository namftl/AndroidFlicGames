#!/bin/bash

# SlideGame Build Script
# This script helps build the Android APK with the correct Java version

echo "=== SlideGame Build Script ==="
echo

# Check if local.properties exists
if [ ! -f "local.properties" ]; then
    echo "ERROR: local.properties not found!"
    echo
    echo "Please create local.properties with your Android SDK location:"
    echo "  sdk.dir=/path/to/your/android/sdk"
    echo
    echo "Example:"
    echo "  sdk.dir=/Users/$(whoami)/Library/Android/sdk"
    echo
    exit 1
fi

# Set Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo "Using Java: $JAVA_HOME"
echo

# Build the APK
echo "Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo
    echo "=== BUILD SUCCESSFUL ==="
    echo
    echo "APK location:"
    echo "  $(pwd)/android/build/outputs/apk/debug/android-debug.apk"
    echo
    echo "File size:"
    ls -lh android/build/outputs/apk/debug/android-debug.apk | awk '{print "  " $5}'
    echo
    echo "To deploy to your tablet without USB:"
    echo "  1. Email the APK to yourself, or"
    echo "  2. Upload to cloud storage (Drive/Dropbox), or"
    echo "  3. Start HTTP server: python3 -m http.server 8000"
    echo "     Then download from tablet at: http://$(ipconfig getifaddr en0 2>/dev/null || echo YOUR_IP):8000/android/build/outputs/apk/debug/android-debug.apk"
    echo
else
    echo
    echo "=== BUILD FAILED ==="
    echo
    echo "Check the error messages above."
    echo "Common issues:"
    echo "  - Make sure Android SDK is installed"
    echo "  - Verify local.properties has correct SDK path"
    echo "  - Check that API level 33 is installed in your SDK"
    echo
    exit 1
fi
