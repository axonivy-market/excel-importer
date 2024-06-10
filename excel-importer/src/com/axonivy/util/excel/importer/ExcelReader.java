package com.axonivy.util.excel.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ExcelReader {

  public static Integer DEFAULT_STRING_LENGTH = 255;

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
    if (!rowIterator.hasNext()) {
      return List.of();
    }
    Map<String, Column> columnMap = new LinkedHashMap<>();
    rowIterator.forEachRemaining(row -> {
      for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
        var name = names.get(cellIndex);
        var cell = row.getCell(cellIndex);
        if (columnMap.containsKey(name)) {
          updateColumn(columnMap.get(name), cell);
        } else {
          columnMap.put(name, toColumn(name, row.getCell(cellIndex)));
        }
      }
    });
    return new ArrayList<>(columnMap.values());
  }

  private static Column toColumn(String fieldName, Cell cell) {
    if (cell == null) {
      return new Column(fieldName, String.class, DEFAULT_STRING_LENGTH); // type not known on first row
    }
    switch (cell.getCellType()) {
    case NUMERIC:
      if (DateUtil.isCellDateFormatted(cell)) {
        return new Column(fieldName, Date.class);
      }
      if (CellUtils.isInteger(cell)) {
        return new Column(fieldName, Integer.class);
      }
      return new Column(fieldName, Double.class);
    case STRING:
      var cellLength = cell.getStringCellValue().length();
      return new Column(fieldName, String.class, cellLength > DEFAULT_STRING_LENGTH ? cellLength : DEFAULT_STRING_LENGTH);
    case BOOLEAN:
      return new Column(fieldName, Boolean.class);
    default:
      return new Column(fieldName, String.class, DEFAULT_STRING_LENGTH);
    }
  }

  private static void updateColumn(Column column, Cell cell) {
    if (cell == null) {
      return;
    }
    if (cell.getCellType() == CellType.NUMERIC 
        && column.getType().equals(Integer.class)
        && !CellUtils.isInteger(cell)) {
      column.setType(Double.class);
    }
    if (column.getType().equals(String.class)) {
      var cellLength = cell.getStringCellValue().length();
      if (cellLength > column.getDatabaseFieldLength()) {
        column.setDatabaseFieldLength(cellLength);
      }
    }
  }
}
