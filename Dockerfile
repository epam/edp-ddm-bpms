FROM adoptopenjdk/openjdk11:alpine-jre
ENV USER_UID=1001 \
    USER_NAME=bpms
RUN addgroup --gid ${USER_UID} ${USER_NAME} \
    && adduser --disabled-password --uid ${USER_UID} --ingroup ${USER_NAME} ${USER_NAME}
ADD target/*.jar app.jar
USER bpms
ENTRYPOINT ["/bin/sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
