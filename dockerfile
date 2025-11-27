# 使用与项目要求一致的Java 8
FROM registry.cn-beijing.aliyuncs.com/king019/openjdk:8
WORKDIR /app
COPY backend/target/rjgcsj-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]