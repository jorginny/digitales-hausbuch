# Tests ausführen

Voraussetzungen: Java 21 und Docker Desktop mit laufender Docker Engine.

Im Backend-Verzeichnis:

```powershell
docker compose -p hausbuch-tests -f compose.test.yml up -d --wait
.\mvnw.cmd test
```

Alternativ kann `mvn test` verwendet werden.

Die Testdatenbank heißt `digitales_hausbuch_test` und läuft in einem separaten
PostgreSQL-16-Container auf `localhost:5433`. Benutzer: `hausbuch_test`, Passwort:
`local_test_only`. Diese Zugangsdaten sind ausschließlich für die lokale
Testdatenbank vorgesehen. Der Port ist nur an die lokale Loopback-Adresse gebunden.

`src/test/resources/application.properties` wird bei Tests automatisch geladen.
Die Tests benötigen keine `POSTGRES_*`-Umgebungsvariablen. Hibernate erstellt und
entfernt die Tabellen beim Starten und Beenden des Testkontexts. Die Datenbank
speichert ihre Daten temporär; beim Stoppen des Containers gehen diese verloren.

Nach den Tests kann der Container gestoppt und entfernt werden:

```powershell
docker compose -p hausbuch-tests -f compose.test.yml down
```
