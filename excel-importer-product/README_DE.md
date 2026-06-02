# Excel-zu-Dialog-Importer

Erstelle eine funktionierende Webanwendung und den ersten Dialog direkt aus einer Excel-Tabelle.

Lies unsere [Dokumentation](excel-importer-product/README.md).

## Wichtigste Funktionen

- Erstelle eine funktionierende Webanwendung und den ersten Dialog direkt aus einer Excel-Tabelle, wodurch manuelle UI-Entwicklung entfällt.
- Generiere automatisch Entitätsklassen und Persistenzgerüst aus Tabellen-Spalten, um die Entwicklungszeit zu verkürzen.
- Prüfe und validiere importierte Daten vor dem Speichern, um Konfigurationsfehler zu vermeiden.
- Mappe Excel-Spalten einfach auf Anwendungsfelder; unterstützt benutzerdefinierte Felder und Typen.
- Verwalte importierte Daten über integrierte Dialoge: Anzeigen, Hinzufügen, Bearbeiten und Löschen.
- Integriert sich in die Axon Ivy Runtime für Persistenz und Prozessautomatisierung.

## Demo

- Für diesen Abschnitt wurden keine Informationen geliefert.

### Demo Workflows

- Für diesen Abschnitt wurden keine Informationen geliefert.

## Einrichtung

- **Rollen:** Everybody (konfiguriert in config/roles.xml)
- **OpenAPI:** Für diesen Abschnitt wurden keine Informationen geliefert.

### Variablen

```
@variables.yaml@
```

## Komponenten

### Aufrufbare Teilprozesse (Callable Subprocesses)

- Für diesen Abschnitt wurden keine Informationen geliefert.

### Dialog-Komponenten

#### Entity Manager — Verwalte importierte Einträge (anzeigen, hinzufügen, bearbeiten, löschen)
- **Namespace:** com.axonivy.utils.excel.importer.EntityManager
- **Komponententyp:** UI dialog
- **Felder:** (nicht in der Start-Signatur des Dialogs deklariert)
- **Zweck:** Zeigt eine Tabelle der importierten Einträge und ermöglicht das Hinzufügen, Bearbeiten und Löschen von Items.

#### Entity Detail — Dialog zum Anzeigen und Bearbeiten eines einzelnen importierten Eintrags
- **Namespace:** com.axonivy.utils.excel.importer.EntityManager
- **Komponententyp:** UI dialog
- **Felder:** - (keine)
- **Zweck:** Dialog zum Anzeigen und Bearbeiten der Felder eines einzelnen importierten Eintrags.

### Web Services

- Für diesen Abschnitt wurden keine Informationen geliefert.

### Maven-Artefakte

1. excel-importer

```xml
<dependency>
  <groupId>com.axonivy.utils.excel</groupId>
  <artifactId>excel-importer</artifactId>
  <type>jar</type>
</dependency>
```
