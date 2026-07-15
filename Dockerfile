# jdk 25버전
FROM eclipse-temurin:25-jdk
ARG JAR_FILE=build/libs/springedu2.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]