# --- Giai đoạn 1: Build ứng dụng ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Thiết lập encoding UTF-8
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# Copy POM và tải dependencies
COPY pom.xml .
RUN mvn -B dependency:resolve dependency:resolve-plugins

# Copy mã nguồn và build
COPY src ./src
RUN mvn clean package -DskipTests -Dproject.build.sourceEncoding=UTF-8

# --- Giai đoạn 2: Chạy ứng dụng ---
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy file jar đã build ở giai đoạn trước
COPY --from=build /app/target/*.jar app.jar

# Mở port 8080
EXPOSE 8080

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
