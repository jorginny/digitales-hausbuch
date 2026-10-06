# Digitales Hausbuch

Webanwendung zur zentralen Verwaltung von Immobilien, Räumen, Haushaltsgegenständen, technischen Anlagen sowie zugehörigen Wartungsaufgaben und Wartungshistorien.

Das Projekt wurde im Rahmen eines Studienprojekts entwickelt. Ziel ist es, wichtige Informationen zu einer Immobilie strukturiert zu erfassen und wiederkehrende Wartungs- und Instandhaltungsaufgaben nachvollziehbar zu verwalten.

## Funktionsumfang

Die Anwendung unterstützt unter anderem:

- Registrierung, Login und Logout
- Verwaltung von Immobilien
- Verwaltung von Räumen innerhalb einer Immobilie
- Verwaltung von Haushaltsgegenständen und technischen Anlagen
- Erfassung von Wartungsaufgaben
- wiederkehrende Wartungsintervalle in Monaten oder Jahren
- Markierung von Wartungsaufgaben als erledigt
- Speicherung einer Wartungshistorie mit optionalen Notizen
- zentrale Übersicht über offene Wartungsaufgaben einer Immobilie
- Anzeige des Fälligkeitsstatus von Wartungsaufgaben
- hierarchisches Löschen von Immobilien, Räumen, Objekten und Wartungsaufgaben

## Architektur

Die Anwendung ist als klassische Client-Server-Webanwendung aufgebaut.

```text
React / TypeScript Frontend
          |
          | HTTP / REST
          v
Spring Boot Backend
          |
          | Spring Data JPA
          v
PostgreSQL
```

Das Frontend wird als Single Page Application mit React umgesetzt. Die Kommunikation mit dem Backend erfolgt über REST-Endpunkte. Das Spring-Boot-Backend enthält die Geschäftslogik, Authentifizierung, Validierung und Datenzugriffe. Die persistente Speicherung erfolgt in PostgreSQL.

## Technologien

### Backend

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Bean Validation
- PostgreSQL 16
- Maven
- JUnit / MockMvc

### Frontend

- React 19
- TypeScript
- Vite
- React Router
- Material UI

### Infrastruktur

- Docker
- Docker Compose

## Projektstruktur

```text
digitales-hausbuch/
├── backend/
│   ├── src/main/java/          # Spring-Boot-Anwendung
│   ├── src/main/resources/     # Anwendungskonfiguration
│   ├── src/test/java/          # Integrations- und Anwendungstests
│   ├── src/test/resources/     # separate Testkonfiguration
│   ├── compose.test.yml        # PostgreSQL-Testdatenbank
│   └── pom.xml
├── frontend/
│   ├── src/components/
│   ├── src/pages/
│   ├── src/services/
│   └── package.json
├── docker-compose.yml          # PostgreSQL für die Anwendung
└── README.md
```

## Voraussetzungen

Zum lokalen Start werden benötigt:

- Java 21
- Maven
- Node.js / npm
- Docker mit Docker Compose

Die für Maven verwendete Java-Version kann mit folgendem Befehl geprüft werden:

```bash
mvn -version
```

Dabei sollte Java 21 angezeigt werden.

## Anwendung lokal starten

### 1. Datenbank konfigurieren

Die Hauptdatenbank wird über Umgebungsvariablen konfiguriert.

Im Projektverzeichnis kann dazu eine lokale `.env`-Datei angelegt werden:

```env
POSTGRES_DB=digitales_hausbuch
POSTGRES_USER=hausbuch
POSTGRES_PASSWORD=lokales_passwort
```

Die Datei `.env` ist in `.gitignore` eingetragen und wird nicht versioniert.

### 2. PostgreSQL starten

Im Hauptverzeichnis des Projekts:

```bash
docker compose up -d
```

Die PostgreSQL-Datenbank ist anschließend über Port `5432` erreichbar.

### 3. Backend starten

In das Backend-Verzeichnis wechseln:

```bash
cd backend
```

Anschließend das Backend starten:

```bash
mvn spring-boot:run
```

Das Backend läuft standardmäßig unter:

```text
http://localhost:8080
```

### 4. Frontend starten

In einem zweiten Terminal:

```bash
cd frontend
npm install
npm run dev
```

Das Frontend ist anschließend standardmäßig unter:

```text
http://localhost:5173
```

erreichbar.

## Tests

Für die Integrationstests wird bewusst eine separate PostgreSQL-Datenbank verwendet.

Die Testdatenbank ist in `backend/compose.test.yml` definiert und läuft auf Port `5433`. Die Daten werden nur temporär im Container gehalten.

### Testdatenbank starten

Im Backend-Verzeichnis:

```bash
docker compose -f compose.test.yml up -d
```

### Tests ausführen

Anschließend:

```bash
mvn test
```

Die Testkonfiguration befindet sich unter:

```text
backend/src/test/resources/application.properties
```

Sie verwendet eine eigene Datenbank mit `create-drop`, sodass das Testschema für den Testlauf neu erzeugt und anschließend wieder entfernt wird.

### Testdatenbank stoppen

Nach dem Testlauf:

```bash
docker compose -f compose.test.yml down
```

## Authentifizierung und Sicherheit

Die Anwendung verwendet eine serverseitige Session-Authentifizierung mit Spring Security. Nach erfolgreicher Anmeldung wird die Sitzung über das Cookie `JSESSIONID` verwaltet.

Passwörter werden mit BCrypt gehasht gespeichert.

Nicht öffentliche API-Endpunkte erfordern eine authentifizierte Sitzung. Zusätzlich wird auf Service-Ebene geprüft, ob angeforderte Immobilien, Räume, Objekte und Wartungsaufgaben dem angemeldeten Benutzer zugeordnet sind.

CORS ist für die lokale Entwicklung auf das React-Frontend unter `http://localhost:5173` beschränkt.

Für die Kommunikation zwischen React-Frontend und REST-Backend wurde der CSRF-Schutz im Rahmen des Prototyps für die verwendeten API-Endpunkte vereinfacht. Für einen produktiven Einsatz wäre eine vollständige CSRF-Token-Integration vorgesehen.

## Fachliches Datenmodell

Die zentralen Entitäten sind hierarchisch aufgebaut:

```text
User
└── Property
    └── Room
        └── HouseholdObject
            └── MaintenanceTask
                └── MaintenanceRecord
```

Eine Immobilie gehört einem Benutzer. Räume gehören zu einer Immobilie, Haushaltsgegenstände zu einem Raum und Wartungsaufgaben zu einem Haushaltsgegenstand. Durchgeführte Wartungen werden als Historieneinträge gespeichert.

Beim Löschen übergeordneter Elemente werden abhängige Datensätze kontrolliert von unten nach oben entfernt, um die referenzielle Integrität der Datenbank zu erhalten.

## Wartungslogik

Wartungsaufgaben können einmalig oder wiederkehrend angelegt werden.

Bei einer einmaligen Aufgabe wird die Wartung nach Abschluss als erledigt markiert.

Bei einer wiederkehrenden Aufgabe wird nach dem Abschluss ein Historieneintrag erzeugt und der nächste Fälligkeitstermin anhand des hinterlegten Intervalls berechnet. Dadurch bleibt die Aufgabe weiterhin als zukünftige Wartungsaufgabe bestehen.

## Hinweise zum Projektstatus

Die Anwendung ist als funktionsfähiger Prototyp im Rahmen eines Studienprojekts umgesetzt. Der Schwerpunkt liegt auf einer nachvollziehbaren Architektur, der Trennung von Frontend und Backend, einer persistierten relationalen Datenhaltung, Authentifizierung sowie automatisierten Integrationstests.
