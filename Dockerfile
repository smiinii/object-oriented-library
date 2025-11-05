FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY ./build/libs/*.jar app.jar
ENV TZ=Asia/Seoul
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]