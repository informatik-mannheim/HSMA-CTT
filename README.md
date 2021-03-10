# HSMA-CTT
Open Source CoronaTrackingTool der Hochschule Mannheim. Kann frei verwendet werden um die Auflagen zur Kontaktvervolgung zu erfüllen. Es geht davon aus das viele Daten bereits in Moodle oder andere Datenbanken Gespeichert sind, so dass diese Daten nicht jedes mal neu erhoben werden müssen. 

Die Webapp ist selbstgehostet und kann über einen Docker container deployed werden, oder auf Java Basis laufen. 

### Um das Projekt Lokal laufen zu lassen oder zu bearbeiten:
**Das Projekt am besten in der Konsole kompilieren. Dazu das Projekt per Maven bauen:**
- Konsole im Projektordner (wo das POM-file liegt) aufrufen.
- "mvn clean install" (ohne Anführungszeichen) ausführen. 
- Eine der letzten Ausgabe des Komplierungsvorgangs ist der Name der kompilierten JAR-Datei.
- Unter Linux diese Datei als ausführbar markieren.
- Die JAR-Datei kann mit java -jar MeineFrischKompilierteDatei.jar (ersetze das "MeineFrischKompeliertenDatei") ausgeführt werden, sofern mindestens ein Java 8 auf dem Rechner installiert ist
- Leichter geht "mvn spring-boot:run"
-> Danach kann Projekt in Webbrwoser aufgerufen werden mit: http://localhost:8080
- Port und Hostname können beim Aufruf mit folgendem Befehl gesetzt werden: java -Dserver.port=9092 -Dhostname= <server_name> -jar ct-0.0.1-SNAPSHOT.jar (Das <server_name> durch den echten namen ersetzen)
- Weitere Konfigurationen finden sich in der Datei application.properties, die sich im Ordner src/main/resources befindet, alle dort enthaltenen Punkte können beim Start mit vorangestelltem -D überschrieben werden

**Projekt in IDE laufen lassen**
Da das Projekt auf Maven und Lombok aufbaut, läuft es besser in Intellij als in Eclipse. Es geht aber problemlos auch dort, s. n. Punkt.

### Eclipse

Das Projekt benutzt das Lombok-Framework, Eclipse muss mit Hilfe von lombok.jar "gepatcht" werden, damit es Getter/Setter etc. richtig anzeigen kann: https://projectlombok.org/download. Der Maven-Build über die Konsole sollte auch ohne diesen Patch funktionieren.

### IntelliJ

1. [Lombok Plugin installieren](https://projectlombok.org/setup/intellij)
2. [Enable Annotation Processing](https://stackoverflow.com/a/41166240) (Normalerweise wird es in einem Popup nach der Plugin Installation vorgeschlagen)

In jeder IDE "import --> as existing Maven Project" wählen. Das dauerte einen kleinen Moment. Datei "CtApp.java" öffnen. Bei Run (play Knopf) "run as --> spring boot App (bzw. Java Application in Eclipse) auswählen.

- Danach kann Projekt im Webbrowser aufgerufen werden mit: http://localhost:8080


### Benutzung der Weboberfläche
Über das Feld "manuelles checkin" kann eine Raumnummer oder ein Raumbezeichner eingegeben werden. Alterantiv können auch Sonderveranstaltungen angelegt werden, für Treffen die im Freien stattfinden. 

Erstellen einer Veranstaltung erfordert einen Login. Die Passworter sollten gehashed werden und ein einer Sepperaten Datei gespeichert werden. Dateipfad: /var/local/ctt-app/.env  USER_CREDENTIALS. 


### Docker
Neu dabei ist ein Dockerfile zur Erstellung eines Dockercontainers. Ebenfalls aus dem Projektordner wie folgt aufrufbar (erfordert natürlich einen laufenden Docker Daemon): docker build -f Dockerfile -t ct .

Danach dann kann der Container mit docker run -p 8080:8080 -t ct aus geführt werden.

Neu: Zum Testen mit nginx-Proxy reicht docker-compose up (ggf. -- build) auszuführen, das Image wird dann automatisch gebaut. Danach kommt man über localhost drauf, also über Standardports 443 und 80 (wird umgeleitet auf https -> 443).


### Räume
QRCode für einen Raum generieren: http://<server>:<port>/QRCodes/room/<Raum> ("server" ggf. durch Servernamen ersetzen, ebenso wie den Port und die bezeichnung des Raums)

QRCodes für alle Räume: http://<server>:<port>/printout/rooms
  (dazu muss eine Datei namens formTemplate.pdf in templates/printout vorhanden sein)

In einen Raum einchecken: http://<server>:<port>/r/<raum> (geht bei Auto-Checkin dann automatisch)

Testweise zum Deaktivieren eines Auto-Checkings ?noautosignin=1 an die vorige Raum-URL anhängen

Import einer Raumliste (im European CSV-Format Raumname;Kapazität, also bspw. A008;30) über die URL: http://<server>:<port>/r/import 

