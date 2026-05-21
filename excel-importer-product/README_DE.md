 # Excel Importer

Der Excel Importer integriert sich in Axon Ivy, um Excel-Tabellen in deine Prozesse zu importieren, Zeilen auf Java-Entitäten abzubilden und sie in der Datenbank deiner Anwendung zu persistieren.

Er bietet dataclass-gesteuerte Zuordnungen und eine einfache Benutzeroberfläche zur Verwaltung von Entitätsdaten, mit der du importierte Datensätze überprüfen, bearbeiten und persistieren kannst.

![Excel-Symbol](../excel-importer/webContent/icon/excel-icon.png)

### Wichtigste Funktionen

- Importiere Excel-Tabellen und bilde Zeilen auf Java-Entitäten ab, sodass du Daten schnell in Axon Ivy-Prozesse laden kannst.
- Ordne Excel-Spalten persistenten Entitätsfeldern zu und persistiere Daten automatisch in deiner konfigurierten Persistence-Unit.
- Verwalte importierte Datensätze über eine integrierte Entity-Manager-Oberfläche mit Hinzufügen/Bearbeiten/Löschen-Workflows.
- Validiere und transformiere Excel-Daten vor der Persistenz, um manuellen Nachbearbeitungsaufwand zu reduzieren.
- Biete dataclass-gesteuerte Formularkomponenten für eine nahtlose Integration in Dialoge und Prozesse.
- Enthält leichte HTML-Dialogprozesse für gängige Entitätsoperationen (anzeigen, bearbeiten, löschen).

## Demo

- Für diesen Abschnitt wurden keine Informationen geliefert.

### Demo-Workflows

- Für diesen Abschnitt wurden keine Informationen geliefert.

## Einrichtung

- **Rollen:** Everybody (konfiguriert in config/roles.xml)
- **OpenAPI:** Keine öffentlichen OpenAPI-Spezifikationen werden von dieser Erweiterung bereitgestellt.

### Variablen

- Es wurden keine Variablen erkannt.

### Optionale Authentifizierungs- und Laufzeitabschnitte

- Für diesen Abschnitt wurden keine Informationen geliefert.

## Komponenten

### Connector-Prozesse

Keine Connector-Prozesse werden von dieser Erweiterung bereitgestellt.

### Formkomponenten

#### MyEntity — Persistente Entitätsdatensätze verwalten
- **Namespace:** com.axonivy.utils.excel.importer
- **Komponententyp:** Datenklasse
- **Felder:**
   - `id` (Integer) — Identifikator
   - `name` (String) — Name der Entität
- **Verwendet in:** EntityManagerProcess.p.json (Methoden: delete, edit)
- **Zweck:** Verwaltet persistente Entitätsdatensätze, die durch Excel-Importe erstellt wurden.

#### Data — Datenklasse (keine deklarierten Felder)
- **Namespace:** com.axonivy.utils.excel.importer
- **Komponententyp:** Datenklasse
- **Felder:**
   - (keine)
- **Zweck:** Platzhalter-Datenklasse, die vom Entity-Manager verwendet wird.

### OpenAPI-Ressourcen

Keine öffentlichen OpenAPI-Spezifikationen werden von dieser Erweiterung bereitgestellt.

### Maven-Artefakte

1. excel-importer

```xml
<dependency>
  <groupId>com.axonivy.utils.excel</groupId>
  <artifactId>@artifact.id@</artifactId>
  <version>@version@</version>
  <type>iar</type>
</dependency>
```
