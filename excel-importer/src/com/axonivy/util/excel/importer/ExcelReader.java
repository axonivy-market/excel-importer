package com.axonivy.util.excel.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ExcelReader {

  public static List<Column> parseColumns(Sheet sheet) {
    Iterator<Row> rows = sheet.rowIterator();
    List<String> headers = getHeaderCellNames(rows);
    return createEntityFields(headers, rows);
  }

  private static List<String> getHeaderCellNames(Iterator<Row> rowIterator) {
    List<String> headerCells = new ArrayList<String>();
    if (rowIterator.hasNext()) {
      Row row = rowIterator.next();
      Iterator<Cell> cellIterator = row.cellIterator();
      while (cellIterator.hasNext()) {
        Cell cell = cellIterator.next();
        headerCells.add(cell.getStringCellValue());
      }
    }
    return headerCells;
  }

  private static List<Column> createEntityFields(List<String> names, Iterator<Row> rowIterator) {
    List<Column> columns = new ArrayList<>();
    if (!rowIterator.hasNext()) {
      return List.of();
    }
    Row row = rowIterator.next();
    for(int c = 0; c<row.getLastCellNum(); c++) {
      var name = names.get(c);
      var column = toColumn(name, row.getCell(c));
      columns.add(column);
    }
    return columns;
  }

  private static Column toColumn(String name, Cell cell) {
    if (cell == null) {
      return new Column(name, String.class); // type not known on first row
    }
    switch (cell.getCellType()) {
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          return new Column(name, Date.class);
        }
        if (Utils.isCellInteger(cell)) {
          return new Column(name, Integer.class);
        }
        return new Column(name, Double.class);
      case STRING:
        return new Column(name, String.class);
      case BOOLEAN:
        return new Column(name, Boolean.class);
      default:
        return new Column(name, String.class);
    }
  }

  public record Column(String name, Class<?> type) {
}

}
