 # Excel Importer

The Excel Importer integrates with Axon Ivy to import Excel spreadsheets into your processes, mapping rows to Java entities and persisting them in your application's database.

It provides dataclass-driven mappings and a simple entity management UI to review, edit, and persist imported data.

![Excel icon](../excel-importer/webContent/icon/excel-icon.png)

### Key features

- Import Excel spreadsheets and map rows to Java entities, enabling fast data ingestion into Axon Ivy processes.
- Map Excel columns to persistent entity fields and automatically persist data to your configured persistence unit.
- Manage imported records through a built-in entity manager UI with add/edit/delete workflows.
- Validate and transform Excel data before persistence, reducing manual cleanup.
- Provide dataclass-driven form components for seamless integration into dialogs and processes.
- Include lightweight HTML dialog processes for common entity operations (view, edit, delete).

## Demo

- No information was delivered for this section.

### Demo workflows

- No information was delivered for this section.

## Setup

- **Roles:** Everybody (configured in config/roles.xml)
- **OpenAPI:** No public OpenAPI specs delivered by this extension.

### Variables

- No variables were detected.

### Optional authentication and runtime sections

- No information was delivered for this section.

## Components

### Connector processes

No connector processes delivered by this extension.

### Form components

#### MyEntity — Manage persistent entity records
- **Namespace:** com.axonivy.utils.excel.importer
- **Component type:** Data Class
- **Fields:**
	- `id` (Integer) — Identifier
	- `name` (String) — Name of the entity
- **Where used:** EntityManagerProcess.p.json (methods: delete, edit)
- **Purpose:** Manage persistent entity records created from Excel imports

#### Data — Data class (no declared fields)
- **Namespace:** com.axonivy.utils.excel.importer
- **Component type:** Data Class
- **Fields:**
	- (none)
- **Purpose:** Placeholder data class used by the entity manager

### Open API resources

No public OpenAPI specs delivered by this extension.

### Maven artifacts

1. excel-importer

```xml
<dependency>
  <groupId>com.axonivy.utils.excel</groupId>
  <artifactId>@artifact.id@</artifactId>
  <version>@version@</version>
  <type>iar</type>
</dependency>
```

