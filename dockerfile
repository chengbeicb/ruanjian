FROM openjdk:8-jre-slim  # 使用与项目要求一致的Java 8
WORKDIR /app
COPY assets/rjgcsj-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]