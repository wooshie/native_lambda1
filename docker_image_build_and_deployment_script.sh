# Build the Docker image and compile
docker build -t lambda-native-build .

# Extract the compiled binary
docker create --name temp-container lambda-native-build
docker cp temp-container:/app/build/native/nativeCompile/native_lambda1 ./
docker rm temp-container

# Create deployment package
cp native_lambda1 bootstrap
zip lambda-deployment.zip bootstrap
