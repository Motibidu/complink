# Java 17 실행 환경을 기반으로 이미지 생성
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일을 app.jar 라는 이름으로 복사
# build.gradle을 사용했다면 경로는 build/libs/*.jar 형태입니다.
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Spring Boot는 기본적으로 8080 포트를 사용
EXPOSE 8080

# 컨테이너 시작 시 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]