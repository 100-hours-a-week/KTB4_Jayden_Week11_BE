# 1단계: Spring Boot 애플리케이션 빌드
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

# 소스가 변경되어도 의존성 레이어를 재사용한다.
RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew bootJar --no-daemon


# 2단계: JRE만 포함한 실행 이미지
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S spring \
    && adduser -S spring -G spring \
    && mkdir -p /var/lib/hobbyloop/uploads \
    && chown -R spring:spring /app /var/lib/hobbyloop

COPY --from=builder --chown=spring:spring /app/build/libs/*.jar /app/app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
