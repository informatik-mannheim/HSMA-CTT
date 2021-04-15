

## Inhaltsverzeichnis

 - [Umfeld und Kontext](#Umfeld-und-Kontext)
 - [Datenhaltung](#Datenhaltung)
 - [Hosting](#Hosting)
 - [Rollen und Rechte](#Rollen-und-Rechte)
 - [Ansprechpartner](#Ansprechpartner)

## Umfeld und Kontext
### Zweck des Projekts
Das Projekt dient der Kontaktverfolgung für den Infektionsschutz durch das Gesundheitsamt. Das Corona Tracking Tool (CTT) speichert den Aufenthalt von Personen in Räumen digital ab, um ein schnelles Finden von Kontaktdaten und Kontaktpersonen im Falle einer Corona-Infektion zu ermöglichen. Ziel ist es, Papierlisten abzuschaffen, um so den Verwaltungsaufwand zu reduzieren, eine datenschutzkonforme Löschung der Personendaten zu gewährleisten sowie den Vorgaben des Gesundheitsamts zu entsprechen.

### Genutzte Technologien
Die App arbeitet hauptsächlich mit dem Java-Framework Spring Boot, dem Layout Manager Thymeleaf sowie einer PostgreSQL-Datenbank.
Die genutzten Module und Bibliotheken sind:
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

Das Spring-Framework stellt außerdem einen integrierten Tomcat-Server bereit, auf dem die App ausgeführt wird.

## Datenhaltung

### Was wird gespeichert?
Für Studierende und Hochschulmitarbeiter*innen speichert die App ausschließlich die Email-Adresse des Benutzers, die Zeit des Eincheckens und den Raum in dem sich der Email-Inhaber anmeldet. Die Speicherung der Email-Adresse auf der Datenbank erfolgt AES-verschlüsselt und kann ausschließlich von Administratoren/Tracingbeauftragten über die App ausgelesen werden.

Für Gäste speichert die App nach Corona-Verordnung neben der Dauer des Aufenthalts noch Name, Vorname, Email sowie wahlweise eine Telefonnummer und/oder Postadresse. Diese Daten müssen zwingend gespeichert werden, um dem Gesundheitsamt alle benötigten Daten zur Kontaktverfolgung bereitzustellen.

### Wann wird gelöscht?
Alle Einträge, die älter als vier Wochen alt sind, werden automatisch aus der Datenbank entfernt.

## Hosting
Aktuell wird die App in einem Docker-Container auf einem hochschulinternen Ubuntu-Server (18.04) betrieben. Der Server läuft 24/7 auf einer virtuellen Maschine, welche von der Campus IT im Rahmen des VirtuServ-Projektes bereitgestellt wird. 
Sicherheitsupdates werden automatisch installiert. Eine Firewall mit Fail2Ban ist installiert und eingerichtet. Der Server besitzt aktuell 11 GB RAM und 30 GB Festplattenspeicher.

## Rollen und Rechte
### User-Rolle

Die User-Rolle ist Standard für alle Nutzer, die die Website nutzen und erfordert keinen Login. Diese Rolle ermöglicht es ausschließlich, sich in eine Veranstaltung bzw. einen Raum einzutragen.

###  Rolle Mitarbeitende

Diese Rolle hat die Möglichkeit, Veranstaltungen anzulegen und ihre Belegung (Anzahl der eingetragenen Nutzer) anzuzeigen. Geplant ist die Möglichkeit, eine Veranstaltung bzw. einen Raum zu leeren, wenn sich nicht alle vorherigen Nutzer ordnungsgemäß ausgecheckt haben.

### Admin-Rolle

Die Admin-Rolle kann alles was die Rolle Mitarbeitende kann und kann zusätzlich die Kontaktverfolgung nutzen.

## Ansprechpartner
