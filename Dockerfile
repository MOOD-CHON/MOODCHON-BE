FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
