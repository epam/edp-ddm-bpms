FROM adoptopenjdk/openjdk11:alpine-jre
ADD *.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
