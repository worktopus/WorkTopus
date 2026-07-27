# jdk 25버전
FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY src/main/resources/wallet /app/src/main/resources/wallet
ARG JAR_FILE=build/libs/WorkTopus.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]