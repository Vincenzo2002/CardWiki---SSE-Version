FROM maven:3.9.9-eclipse-temurin-11 AS build
WORKDIR /build
COPY pom.xml ./pom.xml
COPY backend/pom.xml ./backend/pom.xml
COPY backend/src ./backend/src
RUN mvn --batch-mode --no-transfer-progress -f backend/pom.xml -DskipTests package

FROM eclipse-temurin:11-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --uid 10001 --create-home cardwiki
WORKDIR /app
RUN mkdir database images \
    && chown -R cardwiki:cardwiki /app
COPY --from=build --chown=cardwiki:cardwiki /build/backend/target/backend-0.0.1-SNAPSHOT.jar ./app.jar
USER cardwiki
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
