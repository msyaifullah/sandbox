#!/bin/bash

# Generate RSA key pairs for Service A and Service B
echo "Generating RSA key pairs for JWT signing..."

# Create service-a directory keys
echo "Generating keys for Service A..."
openssl genrsa -out service-a/serviceA_private.pem 2048
openssl rsa -in service-a/serviceA_private.pem -pubout -out service-a/serviceA_public.pem

# Create service-b directory keys  
echo "Generating keys for Service B..."
openssl genrsa -out service-b/serviceB_private.pem 2048
openssl rsa -in service-b/serviceB_private.pem -pubout -out service-b/serviceB_public.pem

# Copy public keys to the other service directories
echo "Copying public keys to cross-service directories..."
cp service-a/serviceA_public.pem service-b/
cp service-b/serviceB_public.pem service-a/

echo "Key generation complete!"
echo "Files created:"
echo "  service-a/serviceA_private.pem"
echo "  service-a/serviceA_public.pem" 
echo "  service-a/serviceB_public.pem"
echo "  service-b/serviceB_private.pem"
echo "  service-b/serviceB_public.pem"
echo "  service-b/serviceA_public.pem" 


# # running service-a
# cd service-a
# npm install
# node main.js

# # running service-b
# cd ../service-b
# go run main.go jwt.go model.go