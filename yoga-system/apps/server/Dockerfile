FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/yoga-server.jar app.jar
EXPOSE 8080
ENV TZ=Asia/Shanghai
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
