FROM nexus-docker-registry.apps.cicd2.mdtu-ddm.projects.epam.com/adoptopenjdk/openjdk11:alpine-jre
ADD target/*.jar app.jar
ENTRYPOINT ["/bin/sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
