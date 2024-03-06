# Use the official Gradle image to create a build artifact.
# This stage is named 'build' for reference.
FROM --platform=linux/amd64 gradle:jdk17 as build
# Copy the Gradle project files into the container image.
COPY --chown=gradle:gradle build.gradle /home/gradle/src/build.gradle
COPY --chown=gradle:gradle settings.gradle /home/gradle/src/settings.gradle
COPY --chown=gradle:gradle src /home/gradle/src/src
# Set the working directory in the container.
WORKDIR /home/gradle/src
# Compile the application and run the Gradle wrapper to build the JAR file.
# Note: `gradle build` or `./gradlew build` might be used depending on your setup.
# Use OpenJDK for the final image.
RUN gradle build --no-daemon

FROM --platform=linux/amd64 eclipse-temurin:17.0.10_7-jdk
# Copy the JAR file from the 'build' stage to the final image.
COPY --from=build /home/gradle/src/build/libs/*.jar /usr/app/leonardo-wrapper.jar
# Set the working directory in the container for the application.
WORKDIR /usr/app
# Command to run the application.
CMD ["java", "-jar", "leonardo-wrapper.jar"]