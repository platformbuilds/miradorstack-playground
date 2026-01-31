FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN apt-get update && apt-get install -y maven && mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Create directory for JFR recordings
RUN mkdir -p /app/jfr-recordings

# Expose port
EXPOSE 8080

# Run the application with JFR continuous recording
CMD ["java", \
     "-XX:StartFlightRecording=name=continuous,settings=profile,disk=true,dumponexit=true,maxage=24h,filename=/app/jfr-recordings/app.jfr", \
     "-jar", "target/playground-0.0.1-SNAPSHOT.jar"]
