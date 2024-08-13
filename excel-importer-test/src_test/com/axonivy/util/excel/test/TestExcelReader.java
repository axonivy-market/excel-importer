package com.axonivy.util.excel.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.axonivy.util.excel.importer.Column;
import com.axonivy.util.excel.importer.ExcelLoader;
import com.axonivy.util.excel.importer.ExcelReader;

class TestExcelReader {

  @Test
  void parseColumns_xlsx(@TempDir Path dir) throws IOException {
    Path path = dir.resolve("customers.xlsx");
    TstRes.loadTo(path, "sample.xlsx");
    Workbook wb = ExcelLoader.load(path);
    List<Column> columns = ExcelReader.parseColumns(wb.getSheetAt(0));
    assertThat(columns).extracting(Column::getName)
        .contains("Firstname", "Lastname");
    assertThat(columns).contains(
        new Column("Firstname", String.class, 255), new Column("ZIP", Integer.class),
        new Column("Amount", Double.class), new Column("Birthdate", Timestamp.class), // should be a date
        new Column("Note", String.class, 811),
        new Column("Column contains texts in incorrect number format", String.class, 255),
        new Column("Column contains both text and numeric", String.class, 255)
    );
  }

}
