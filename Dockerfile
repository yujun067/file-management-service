# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ---- Run Stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /tmp/my-root

EXPOSE 8081
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.config.location=classpath:/,file:/app/config/ --spring.profiles.active=$SPRING_PROFILE"]