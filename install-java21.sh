#!/bin/bash

# Script to install Java 25 / GraalVM 25 for native image builds

set -e

echo "Installing GraalVM 25 for Java 25 support..."

# Check if SDKMAN is installed
if [ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    
    echo "Installing GraalVM 25.0.1..."
    sdk install java 25.0.1-graalce
    
    echo "Setting as default..."
    sdk use java 25.0.1-graalce
    sdk default java 25.0.1-graalce
    
    echo "Installing native-image component..."
    gu install native-image
    
    echo ""
    echo "✓ GraalVM 25 installed successfully!"
    echo ""
    echo "Current Java version:"
    java -version
    
    echo ""
    echo "Native image version:"
    native-image --version
    
    echo ""
    echo "To use this version, run:"
    echo "  source ~/.sdkman/bin/sdkman-init.sh"
    echo "  sdk use java 25.0.1-graalce"
    
else
    echo "SDKMAN not found. Installing SDKMAN first..."
    curl -s "https://get.sdkman.io" | bash
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    
    echo "Installing GraalVM 25.0.1..."
    sdk install java 25.0.1-graalce
    sdk use java 25.0.1-graalce
    sdk default java 25.0.1-graalce
    
    echo "Installing native-image component..."
    gu install native-image
    
    echo ""
    echo "✓ GraalVM 25 installed successfully!"
fi

