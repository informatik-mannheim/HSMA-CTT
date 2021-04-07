# Contributing to HSMA-CTT

Outside contributions and cooperation are very much welcome.

The following is a set of guidelines for contributing to HSMA-CTT, developed by the University of Applied Sciences Mannheim. These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document in a pull request.  

## Technologies

HSMA-CTT is implemented as a Spring Boot application in Java 8. [Project Lombok](https://projectlombok.org/) is used for additional source code features. In Production environments a Postgres database is used, for local deployment a embedded H2 DB is used instead.

## Local development setup

If you want to contribute code to the project, you will likely need a setup for development. We primarily use IntelliJ Idea Community Edition for our internal development, but any Java editor/IDE can be used.

### IntelliJ setup

1. Install the [Java 8 JDK](https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot).
1. If you have an older version of IntelliJ, you may have to install the [Lombok Plugin](https://projectlombok.org/setup/intellij). With a version of at least 2020.02 this is pre-installed.
1. [Enable Annotation Processing](https://stackoverflow.com/a/41166240). This should be suggested automatically when importing the project for the first time.
1. Import the project
1. Look for the `src/main/java/de.hs_mannheim.informatik.ct/CtApp.java` file. Press the green start arrow next to the `public class CtApp` line. This will start the app on port 8080.
1. Access the app via <http://localhost:8080>