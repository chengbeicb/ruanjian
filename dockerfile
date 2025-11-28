# 使用与项目要求一致的Java 8
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY backend/target/rjgcsj-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
