FROM adoptopenjdk/openjdk11:alpine-jre
ADD target/*.jar app.jar
ENTRYPOINT ["/bin/sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
