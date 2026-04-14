# syntax=docker/dockerfile:1

# ==============================
# STAGE 1: Build ứng dụng (Maven + JDK)
# ==============================
FROM eclipse-temurin:22-jdk AS builder

# Thư mục làm việc bên trong container build
WORKDIR /app

# Copy các file cần thiết để Maven tải dependency trước (tối ưu cache)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Cấp quyền chạy cho Maven Wrapper
RUN chmod +x mvnw

# Tải dependency trước để các lần build sau nhanh hơn
RUN ./mvnw dependency:go-offline

# Copy source code backend
COPY src ./src

# Build file jar (bỏ test cho nhanh khi build image)
RUN ./mvnw clean package -DskipTests


# ==============================
# STAGE 2: Runtime nhẹ để chạy app
# ==============================
# Runtime image tối giản, giảm bề mặt tấn công và số lượng CVE
FROM gcr.io/distroless/java22-debian12:nonroot

WORKDIR /app

# Copy file jar đã build từ stage builder
COPY --from=builder /app/target/*.jar app.jar

# App Spring Boot chạy cổng 8080
EXPOSE 8080

# Distroless nonroot đã chạy bằng user không phải root
# Lệnh khởi động container
ENTRYPOINT ["java", "-Xmx384m", "-Xms256m", "-jar", "app.jar"]
