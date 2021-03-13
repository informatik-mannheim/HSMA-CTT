# HSMA-CTT
Open Source CoronaTrackingTool der Hochschule Mannheim. Es kann frei verwendet werden um die Auflagen zur Kontaktverfolgung zu erfüllen. Wir Entwickler gehen davon aus, dass viele Kontaktdaten bereits in Moodle oder andere Datenbanken gespeichert sind, sodass diese Daten nicht jedes Mal neu erhoben werden müssen. Schüler oder Studierende müssen für das Tracking einmalig ihre Emailadresse eingeben, danach reicht das abscannen von QR Codes um in der Kontaktverfolgung drin zu sein.

Die Webapp ist selbstgehostet und kann über einen Docker Container deployed werden, oder auf Java Basis laufen.

## Setup für Administratoren:
- Setup des Docker Containers oder der Nativen Java Webapp
- Erstellen von CSV Dateien mit allen Räumen und der per Infektionsschutz zulässigen Gesamtkapazität.
- Türschilder für jeden Raum ausdrucken und aufhängen

### Benutzung der Weboberfläche
Der Standardanwendungsfall sieht vor, dass Teilnehmer einen QR Code scannen. Damit werden Sie zu einer Weboberfläche weitergeleitet, sodass sie Ihre Daten eingeben können. Alternativ kann die Homepage mit jedem Browser aufgerufen werden. In das Feld "manuelles Check-In" kann eine Raumnummer oder ein Raumbezeichner eingegeben werden. Danach ist die Funktion identisch mit der des QR Code Scanners.

Alternativ können auch Sonderveranstaltungen angelegt werden, für Treffen die im Freien stattfinden. 
Erstellen einer Veranstaltung erfordert einen Login. Die Passwörter sollten gehashed werden und in einer separaten Datei gespeichert werden. Dateipfad: /var/local/ctt-app/.env  USER_CREDENTIALS. 


### Um das Projekt Lokal laufen zu lassen, oder es zu bearbeiten:
**Das Projekt am besten in der Konsole kompilieren. Dazu das Projekt per Maven bauen:**
- Konsole im Projektordner (wo das POM-file liegt) aufrufen.
- "mvn clean install" (ohne Anführungszeichen) ausführen. 
- Eine der letzten Ausgabe des Komplierungsvorgangs ist der Name der kompilierten JAR-Datei.
- Unter Linux diese Datei als ausführbar markieren.
- Die JAR-Datei kann mit java -jar MeineFrischKompilierteDatei.jar (ersetze das "MeineFrischKompeliertenDatei") ausgeführt werden, sofern mindestens ein Java 8 auf dem Rechner installiert ist
- Leichter geht "mvn spring-boot:run"
-> Danach kann Projekt in Webbrowser aufgerufen werden mit: http://localhost:8080
- Port und Hostname können beim Aufruf mit folgendem Befehl gesetzt werden: java -Dserver.port=9092 -Dhostname= <server_name> -jar ct-0.0.1-SNAPSHOT.jar (Das <server_name> durch den echten namen ersetzen)
- Weitere Konfigurationen finden sich in der Datei application.properties, die sich im Ordner src/main/resources befindet. Alle dort enthaltenen Punkte können beim Start mit vorangestelltem -D überschrieben werden

**Projekt in IDE laufen lassen**
Da das Projekt auf Maven und Lombok aufbaut, läuft es besser in Intellij als in Eclipse. Es geht aber problemlos auch dort, s. n. Punkt.

### Eclipse

Das Projekt benutzt das Lombok-Framework, Eclipse muss mit Hilfe von lombok.jar "gepatcht" werden, damit es Getter/Setter etc. richtig anzeigen kann: https://projectlombok.org/download. Der Maven-Build über die Konsole sollte auch ohne diesen Patch funktionieren.

### IntelliJ

1. [Lombok Plugin installieren] (https://projectlombok.org/setup/intellij)
2. [Enable Annotation Processing] (https://stackoverflow.com/a/41166240) (Normalerweise wird es in einem Popup nach der Plugin Installation vorgeschlagen)

In jeder IDE "import --> as existing Maven Project" wählen. Das dauerte einen kleinen Moment. Datei "CtApp.java" öffnen. Bei Run (play Knopf) "run as --> spring boot App (bzw. Java Application in Eclipse) auswählen.

- Danach kann Projekt im Webbrowser aufgerufen werden mit: http://localhost:8080


### Docker
Neu dabei ist ein Dockerfile zur Erstellung eines Dockercontainers. Ebenfalls aus dem Projektordner wie folgt aufrufbar (erfordert natürlich einen laufenden Docker Daemon): docker build -f Dockerfile -t ct .

Danach dann kann der Container mit docker run -p 8080:8080 -t ct aus geführt werden.

Neu: Zum Testen mit nginx-Proxy reicht docker-compose up (ggf. -- build) auszuführen, das Image wird dann automatisch gebaut. Danach kommt man über localhost drauf, also über Standardports 443 und 80 (wird umgeleitet auf https -> 443).


### Räume
QRCode für einen Raum generieren: http://<server>:<port>/QRCodes/room/<Raum> ("server" ggf. durch Servernamen ersetzen, ebenso wie den Port und die Bezeichnung des Raums)

QR-Codes für alle Räume: http://<server>:<port>/printout/rooms
  (dazu muss eine Datei namens formTemplate.pdf in templates/printout vorhanden sein)

In einen Raum einchecken: http://<server>:<port>/r/<raum> (geht bei Auto-Checkin dann automatisch)

Testweise zum Deaktivieren eines Auto-Checkins ?noautosignin=1 an die vorige Raum-URL anhängen

Import einer Raumliste im European CSV-Format Raumname;Kapazität, also bspw. A008;30 (wenn A008 eine Raumnummer ist, und 30 die Kapazität) über die URL: http://<server>:<port>/r/import 

