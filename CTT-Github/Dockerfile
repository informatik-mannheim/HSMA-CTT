# First stage builds the jar and is discarded in the final image
FROM maven:3.6.3-adoptopenjdk-8 AS MAVEN_BUILD
# Copy pom and resolve dependecies for layer caching
COPY pom.xml ./pom.xml
RUN mvn dependency:go-offline

# Copy the source and build the jar
COPY src/ ./src/
RUN mvn clean package

# Build the actual runtime image
FROM openjdk:8-jre-alpine3.9
ARG UNAME=ctt
# The ids should be unique to the container
ARG UID=1586
ARG GID=1586

ENTRYPOINT ["java","-jar","/usr/share/ctt/ctt.jar"]

# Add a user and switch to it to avoid running the webserver as root
RUN adduser -D -g "" -u $UID $UNAME $UNAME
USER $UNAME

WORKDIR /usr/share/ctt/

# Add Maven dependencies
COPY --from=MAVEN_BUILD target/lib/           /usr/share/ctt/lib/

# Add the service itself
COPY --from=MAVEN_BUILD target/ctt-*-SNAPSHOT.jar /usr/share/ctt/ctt.jar
