# GraalVM Native Image Configuration

This project is configured to build a GraalVM Native Image executable for improved startup time and reduced memory footprint.

## Prerequisites

1. **GraalVM JDK 25** - Install GraalVM JDK 25 or higher
   
   **Option 1: Using SDKMAN (recommended)**
   ```bash
   source ~/.sdkman/bin/sdkman-init.sh
   sdk install java 25.0.1-graalce
   sdk use java 25.0.1-graalce
   gu install native-image
   ```
   
   **Option 2: Using Homebrew (macOS)**
   ```bash
   brew install --cask graalvm-jdk@25
   export JAVA_HOME=$(/usr/libexec/java_home -v 25)
   export PATH=$JAVA_HOME/bin:$PATH
   ```
   
   **Option 3: Manual Download**
   - Download from https://www.graalvm.org/downloads/
   - Extract and set JAVA_HOME to the GraalVM directory
   - Add GraalVM bin to PATH
   
   **Quick Setup Script**
   ```bash
   ./setup-graalvm.sh
   ```

2. **Native Image Component** - Install the native-image component
   ```bash
   gu install native-image
   ```

3. **Verify Installation**
   ```bash
   java -version  # Should show GraalVM
   native-image --version
   ```

## Building Native Image

### Build Native Executable

**Option 1: Direct native compile (recommended)**
```bash
cd service-java-graalvm
mvn clean native:compile
```

**Option 2: Using package goal (will also build native)**
```bash
mvn clean package
```

The native executable will be generated in the `target/` directory as `service-java-graalvm` (or `service-java-graalvm.exe` on Windows).

**Note**: The native build process takes several minutes and requires significant memory (recommended: 8GB+ RAM).

### Build Regular JAR Only

To build only the regular JAR without native image:

```bash
# Temporarily skip native plugin
mvn clean package -DskipNative
```

Or modify the pom.xml to remove the native plugin execution from the package phase.

## Running Native Executable

### Development Profile
```bash
./target/service-java-graalvm --spring.profiles.active=dev
```

### Staging Profile
```bash
./target/service-java-graalvm --spring.profiles.active=stg
```

### Production Profile
```bash
./target/service-java-graalvm --spring.profiles.active=prd
```

## Configuration Files

### Reflection Configuration
- **Location**: `src/main/resources/META-INF/native-image/reflect-config.json`
- **Purpose**: Configures classes that require reflection for Jackson serialization/deserialization
- **Includes**: All model classes (ProductOrder, ServiceOrder, ReservationOrder, AirlinesOrder, etc.)

### Native Image Properties
- **Location**: `src/main/resources/META-INF/native-image/native-image.properties`
- **Purpose**: Global native image build arguments and configuration
- **Features**:
  - No fallback mode (strict native compilation)
  - Resource inclusion for application properties
  - Runtime initialization for Redis/Lettuce
  - Exception stack trace reporting

## Build Arguments

The following native image build arguments are configured:

- `--no-fallback`: Fail if native compilation is not possible (strict mode)
- `--enable-preview`: Enable preview Java features
- `-H:+ReportExceptionStackTraces`: Include exception stack traces in native image
- `-H:+AddAllCharsets`: Include all character sets
- `-H:IncludeResources=application.*\\.properties`: Include all application property files
- `-H:ReflectionConfigurationFiles=reflect-config.json`: Use reflection configuration
- `--initialize-at-run-time=io.lettuce.core`: Initialize Lettuce (Redis client) at runtime
- `--initialize-at-run-time=org.springframework.data.redis`: Initialize Spring Data Redis at runtime

## Troubleshooting

### Error: Undefined symbol ScopedMemoryAccess_closeScope0

**Problem**: Linker error with `_Java_jdk_internal_misc_ScopedMemoryAccess_closeScope0` symbol not found.

```
Undefined symbols for architecture arm64:
  "_Java_jdk_internal_misc_ScopedMemoryAccess_closeScope0", referenced from:
      ___svm_cglobaldata_base in service-java-graalvm.o
ld: symbol(s) not found for architecture arm64
```

**Root Cause**: This is a **known compatibility issue** between GraalVM 21.0.2 and Java 21's ScopedMemoryAccess API. The native method `closeScope0` is not available in the GraalVM native image runtime for Java 21.

**Solutions**:

1. **Use Java 25** (Recommended):
   ```bash
   # Install GraalVM with Java 25
   sdk install java 25.0.1-graalce
   sdk use java 25.0.1-graalce
   
   # pom.xml is already configured for Java 25
   ```

3. **Use regular JAR instead of native image**: For now, you can use the regular Spring Boot JAR:
   ```bash
   mvn clean package
   java -jar target/service-java-graalvm-1.0.0.jar
   ```

4. **Try a newer GraalVM version**: Check if a newer GraalVM version is available:
   ```bash
   sdk list java | grep graal
   ```

**Note**: The native-maven-plugin may add `--no-fallback` by default, which prevents fallback mode. This issue requires either using Java 17 or waiting for better Java 21 support in GraalVM.

### Error: 'gu' tool was not found / Not a GraalVM distribution

**Problem**: Maven reports that the JDK is not a GraalVM distribution.

**Solution**:
1. Verify you're using GraalVM:
   ```bash
   java -version
   # Should show "GraalVM" in the output
   ```

2. If using SDKMAN and it's not working:
   ```bash
   # Try Homebrew instead
   brew install --cask graalvm-jdk@21
   export JAVA_HOME=$(/usr/libexec/java_home -v 21)
   export PATH=$JAVA_HOME/bin:$PATH
   ```

3. Verify GraalVM installation:
   ```bash
   which java
   java -version
   echo $JAVA_HOME
   # Should point to GraalVM directory
   ```

4. Install native-image component:
   ```bash
   gu install native-image
   ```

5. Re-run the build:
   ```bash
   mvn clean native:compile
   ```

### Build Fails with Reflection Errors

If you encounter reflection-related errors during build:

1. Add the missing class to `reflect-config.json`
2. Ensure all fields, methods, and constructors are included
3. Rebuild the native image

### Runtime Errors

If the native executable fails at runtime:

1. Check the error message for missing classes or resources
2. Add missing classes to reflection configuration
3. Verify all required resources are included
4. Check initialization timing (build-time vs runtime)

### Redis Connection Issues

Redis/Lettuce requires runtime initialization. If you encounter issues:

1. Verify `--initialize-at-run-time` flags are set for Lettuce and Spring Data Redis
2. Check Redis connection configuration in application properties
3. Ensure Redis server is accessible

## Performance Benefits

Native images provide:

- **Faster Startup**: Typically 10-100x faster than JVM startup
- **Lower Memory**: Reduced memory footprint
- **Better Performance**: Ahead-of-time compilation optimizations
- **Single Executable**: No need for JVM installation on target system

## Limitations

- Build time is significantly longer (several minutes)
- Some dynamic features may require additional configuration
- Debugging is more limited compared to JVM mode
- Some libraries may not be fully compatible

## Testing

Test the native executable:

```bash
# Start the application
./target/service-java-graalvm --spring.profiles.active=dev

# In another terminal, test endpoints
curl http://localhost:3001/health
curl -X POST http://localhost:3001/api/invoice/validate -H "Content-Type: application/json" -d @data/product_order.json
```

## Additional Resources

- [GraalVM Native Image Documentation](https://www.graalvm.org/latest/reference-manual/native-image/)
- [Spring Native Image Guide](https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/)
- [GraalVM Build Tools](https://graalvm.github.io/native-build-tools/latest/index.html)

