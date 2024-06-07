package com.axonivy.util.excel.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
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
        .contains("firstname", "lastname");
    assertThat(columns).contains(
        new Column("firstname", String.class, 255, ""), new Column("zip", Integer.class, ""),
        new Column("amount", Double.class, ""), new Column("birthdate", Date.class, ""), // should be a date
        new Column("note", String.class, 811, "")
    );
  }

}
