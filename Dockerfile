# 1단계: Gradle을 사용하여 JAR 파일 빌드
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

# 2단계: 실행 환경 구성 (JDK 25 사용)
FROM eclipse-temurin:25-jdk
WORKDIR /app

# 오라클 지갑 파일 복사
COPY src/main/resources/wallet /app/src/main/resources/wallet

# 1단계에서 빌드된 JAR 파일만 가져와서 복사
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar || COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]