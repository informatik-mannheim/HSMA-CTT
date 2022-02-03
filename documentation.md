

## Table of contents

 - [Purpose of the project](#Purpose-of-the-project)
 - [Data management](#Data-management)
 - [Hosting](#Hosting)
 - [Roles and rights](#Roles-and-rights)
 - [Contact](#Contact)

## Purpose of the project
### Purpose of the project
The purpose of the project is to provide contact tracking for infection control by the health department. The Corona Tracking Tool (CTT) digitally stores the whereabouts of people in rooms to enable quick finding of contact data and contact persons in case of a Corona infection. The goal is to eliminate paper lists in order to reduce the administrative burden, ensure that personal data is deleted in compliance with data protection regulations, and meet the requirements of the health department.

### Technologies used
The app works mainly with the Java framework Spring Boot, the layout manager Thymeleaf and a PostgreSQL database.
All communication between client and server is transmitted encrypted via HTTPS according to common web standards.
The modules and libraries used are:
 - Spring Data JPA
 - Spring Security
 - Spring Web
 - JitPack
 - Maven
 - JUnit
 - h2 Database (für Unit-Tests)
 - QRGen (https://github.com/kenglxn/QRGen)
 - Apache POI
 - Apache Commons
 - JAI-ImageIO (https://github.com/jai-imageio/jai-imageio-core)
 - Bootstrap
 - jQuery
 - Lombok

The Spring framework also provides an integrated Tomcat server on which the app runs.

## Data management

### What is saved?
For students and university employees, the app only stores the user's email address, the time of check-in and the room in which the email holder logs in. The storage of the email address on the database is AES encrypted and can only be read by administrators/tracing officers via the app.

For guests, the app stores, according to Corona regulation, in addition to the duration of the stay, name, first name, email and optionally a phone number and/or postal address. It is mandatory that this data is stored in order to provide the health department with all the necessary data for contact tracing.

### When will be deleted?
All entries older than four weeks are automatically removed from the database.

## Hosting
Currently, the app is run in a Docker container on a university-internal Ubuntu server (18.04). The server runs 24/7 on a virtual machine provided by Campus IT as part of the VirtuServ project.
Security updates are installed automatically. A firewall with Fail2Ban is installed and configured. The server currently has 11 GB of RAM and 30 GB of hard disk space.


## Roles and rights
### User role

The user role is standard for all users using the website and does not require a login. This role only allows users to log into an event or room.

###  Staff role
This role has the ability to create events and view their occupancy (number of registered users). Planned is the possibility to empty an event or room if not all previous users have checked out properly.


### Admin role
The Admin role can do everything the Staff role can do and can also use contact tracking.


## Contact

For questions about the project and the software, please contact [CTT Administration](mailto:ctt-admin@hs-mannheim.de).

