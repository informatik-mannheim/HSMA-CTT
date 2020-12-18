FROM openjdk:11
EXPOSE 8080
ADD /target/ct-0.0.1-SNAPSHOT.jar webapp.jar
ENTRYPOINT ["java","-jar","webapp.jar"]