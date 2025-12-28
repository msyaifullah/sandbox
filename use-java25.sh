#!/bin/bash

# Quick script to switch to Java 25 for building
# Usage: source ./use-java25.sh  (use 'source' to apply to current shell)

if [ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    
    echo "Switching to Java 25.0.1 (GraalVM)..."
    sdk use java 25.0.1-graalce
    
    echo ""
    echo "Current Java version:"
    java -version
    
    echo ""
    echo "Maven Java version:"
    mvn -version | grep "Java version"
    
    echo ""
    echo "✓ Java 25 is now active in this shell"
    echo ""
    echo "Note: This only affects the current shell."
    echo "Java 25 is already set as default for new shells."
else
    echo "SDKMAN not found. Please install SDKMAN first."
    echo "Run: curl -s 'https://get.sdkman.io' | bash"
    exit 1
fi

