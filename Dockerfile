# ===== Stage 1: Build =====
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 先拷贝 pom.xml 让依赖缓存（改业务代码不会重新下载依赖）
COPY pom.xml .
RUN mvn dependency:go-offline -q

# 拷贝源码并打包
COPY src ./src
RUN mvn clean package -DskipTests -q

# ===== Stage 2: Runtime =====
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/iot_warehouse-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Duser.timezone=Asia/Shanghai"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
