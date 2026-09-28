FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY src ./src
RUN mkdir -p out && javac -encoding UTF-8 -d out src/main/java/lab/RowByRowProgress.java

ENTRYPOINT ["java", "-Xms64m", "-Xmx384m", "-XX:+ExitOnOutOfMemoryError", "-cp", "/app/out", "lab.RowByRowProgress"]
