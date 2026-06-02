# Excel to Dialog importer

Crafts a new web application and its first dialog from an existing Excel sheet.

Read our [documentation](excel-importer-product/README.md).

## Key features

- Create a working web application and the first dialog directly from an Excel sheet, removing manual UI coding.
- Automatically generate entity classes and persistence scaffolding from spreadsheet columns to speed development.
- Preview and validate imported data before committing to avoid configuration errors.
- Easily map Excel columns to application fields, with support for custom fields and types.
- Manage imported data using built-in dialogs to view, add, edit, and delete entries.
- Integrates with Axon Ivy runtime for persistence and process automation.

## Demo

- No information was delivered for this section.

### Demo Workflows

- No information was delivered for this section.

## Setup

- **Roles:** Everybody (configured in config/roles.xml)
- **OpenAPI:** No information was delivered for this section.

### Variables

```
@variables.yaml@
```

## Components

### Callable Subprocesses

- No information was delivered for this section.

### Dialog Components

#### Entity Manager — Manage imported entities (view, add, edit, delete)
- **Namespace:** com.axonivy.utils.excel.importer.EntityManager
- **Component type:** UI dialog
- **Fields:** - (none)
- **Purpose:** Presents a table of imported entities and allows adding, editing and deleting items.

#### Entity Detail — Dialog for viewing and editing a single imported entity
- **Namespace:** com.axonivy.utils.excel.importer.EntityManager
- **Component type:** UI dialog
- **Fields:** - (none)
- **Purpose:** Dialog to view and edit a single imported entity's fields.

### Web Services

- No information was delivered for this section.

### Maven Artifacts

1. excel-importer

```xml
<dependency>
  <groupId>com.axonivy.utils.excel</groupId>
  <artifactId>excel-importer</artifactId>
  <type>jar</type>
</dependency>
```
