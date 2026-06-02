#!/bin/bash

# SlideGame Desktop Test Runner
# Run this to test the game on your Mac

echo "Starting SlideGame on desktop..."
echo

export JAVA_HOME=$(/usr/libexec/java_home -v 17)

./gradlew desktop:run

echo
echo "Game closed."
