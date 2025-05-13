FROM gradle:8.6-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test
# 빌드 스테이지에서 uploads 디렉토리가 없을 경우를 대비하여 생성
RUN mkdir -p /app/uploads

FROM openjdk:21-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# uploads 디렉토리 복사는 그대로 유지
COPY --from=build /app/uploads /app/uploads

# 환경 변수 설정 (기본값)
ENV SERVER_PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]