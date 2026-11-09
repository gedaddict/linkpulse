FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :shortener-service:bootJar -x test --no-daemon \
 && cp "$(ls shortener-service/build/libs/*.jar | grep -v plain | head -n 1)" /app/app.jar

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 appuser
WORKDIR /app
COPY --from=build /app/app.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
