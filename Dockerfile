# Stage 1: Build with Java 21 and Gradle 8.9
FROM eclipse-temurin:21-jdk AS build

ENV GRADLE_VERSION=8.9

# Install Gradle
RUN apt-get update && apt-get install -y unzip curl \
    && curl -sSL https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle.zip \
    && unzip gradle.zip -d /opt/ \
    && ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/bin/gradle \
    && rm gradle.zip

WORKDIR /app
COPY . .

# Build Quarkus app as fast-jar, skip tests
RUN gradle build -Dquarkus.package.type=fast-jar -x test

# Stage 2: Runtime using Java 21
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/quarkus-app/ /app/

EXPOSE 8080
CMD ["java", "-jar", "/app/quarkus-run.jar"]
