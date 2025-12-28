#!/bin/bash

# Setup script for GraalVM Native Image
# This script helps install and configure GraalVM for native image compilation

set -e

echo "=== GraalVM Native Image Setup ==="
echo ""

# Check if SDKMAN is available
if [ -d "$HOME/.sdkman" ]; then
    echo "✓ SDKMAN detected"
    
    # Source SDKMAN
    export SDKMAN_DIR="$HOME/.sdkman"
    [[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"
    
    echo ""
    echo "Installing GraalVM 25 via SDKMAN..."
    echo "If SDKMAN has connectivity issues, you can install manually:"
    echo ""
    echo "Manual installation steps:"
    echo "1. Download GraalVM from: https://www.graalvm.org/downloads/"
    echo "2. Extract to a directory (e.g., ~/graalvm)"
    echo "3. Set JAVA_HOME: export JAVA_HOME=~/graalvm"
    echo "4. Add to PATH: export PATH=\$JAVA_HOME/bin:\$PATH"
    echo "5. Install native-image: gu install native-image"
    echo ""
    
    # Try to install via SDKMAN (may fail if offline)
    if command -v sdk &> /dev/null; then
        echo "Attempting to install GraalVM 25..."
        sdk install java 25.0.1-graalce || {
            echo "⚠ SDKMAN installation failed (may be offline)"
            echo ""
            echo "Please install GraalVM manually:"
            echo "  Option 1: Download from https://www.graalvm.org/downloads/"
            echo "  Option 2: Use Homebrew: brew install --cask graalvm-jdk@25"
            exit 1
        }
        
        echo "Switching to GraalVM..."
        sdk use java 25.0.1-graalce
        
        echo ""
        echo "Installing native-image component..."
        gu install native-image || {
            echo "⚠ Failed to install native-image component"
            echo "Please run manually: gu install native-image"
            exit 1
        }
        
        echo ""
        echo "✓ GraalVM setup complete!"
    fi
else
    echo "⚠ SDKMAN not found"
    echo ""
    echo "Please install GraalVM manually:"
    echo "  Option 1: Download from https://www.graalvm.org/downloads/"
    echo "  Option 2: Use Homebrew: brew install --cask graalvm-jdk@21"
    echo ""
    echo "After installation:"
    echo "  1. Set JAVA_HOME to GraalVM installation"
    echo "  2. Add GraalVM bin to PATH"
    echo "  3. Run: gu install native-image"
    exit 1
fi

echo ""
echo "Verifying installation..."
java -version
echo ""
native-image --version || {
    echo "⚠ native-image not found. Please run: gu install native-image"
    exit 1
}

echo ""
echo "=== Setup Complete ==="
echo ""
echo "You can now build native image with:"
echo "  cd service-java-graalvm"
echo "  mvn clean native:compile"
echo ""

