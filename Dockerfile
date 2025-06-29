FROM amazonlinux:2

# Install required packages
RUN yum update -y && \
    yum install -y wget tar gzip gcc gcc-c++ zlib-devel which && \
    yum clean all

# Download and install GraalVM
RUN cd /opt && \
    wget -q https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-21.0.1/graalvm-community-jdk-21.0.1_linux-x64_bin.tar.gz && \
    tar -xzf graalvm-community-jdk-21.0.1_linux-x64_bin.tar.gz && \
    rm graalvm-community-jdk-21.0.1_linux-x64_bin.tar.gz && \
    mv graalvm-community-openjdk-21.0.1+12.1 graalvm

# Set environment variables
ENV JAVA_HOME=/opt/graalvm
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV GRAALVM_HOME=/opt/graalvm

# Verify Java installation
RUN java -version

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the native image (GraalVM Community Edition includes native-image)
RUN ./gradlew nativeCompile

# The native executable will be in build/native/nativeCompile/