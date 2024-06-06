package com.axonivy.util.excel.importer;

import static com.axonivy.util.excel.importer.Constants.DEFAULT_BOOLEAN_LENGTH;
import static com.axonivy.util.excel.importer.Constants.DEFAULT_DATE_LENGTH;
import static com.axonivy.util.excel.importer.Constants.DEFAULT_DOUBLE_LENGTH;
import static com.axonivy.util.excel.importer.Constants.DEFAULT_INT_LENGTH;
import static com.axonivy.util.excel.importer.Constants.DEFAULT_STRING_LENGTH;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
    name = fieldName(name);
    if (cell == null) {
      return new Column(name, String.class, DEFAULT_STRING_LENGTH); // type not known on first row
    }
    switch (cell.getCellType()) {
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          return new Column(name, Date.class, DEFAULT_DATE_LENGTH);
        }
        if (Utils.isCellInteger(cell)) {
          return new Column(name, Integer.class, DEFAULT_INT_LENGTH);
        }
        return new Column(name, Double.class, DEFAULT_DOUBLE_LENGTH);
      case STRING:
        return new Column(name, String.class, DEFAULT_STRING_LENGTH);
      case BOOLEAN:
        return new Column(name, Boolean.class, DEFAULT_BOOLEAN_LENGTH);
      default:
        return new Column(name, String.class, DEFAULT_STRING_LENGTH);
    }
  }

  private static String fieldName(String colName) {
    colName = colName.replaceAll(" ", "");
    if (StringUtils.isAllUpperCase(colName)) {
      return colName.toLowerCase();
    }
    colName = colName.replaceAll("\\W", "");
    colName = colName.replaceAll("[^\\p{ASCII}]", "");
    return StringUtils.uncapitalize(colName);
  }

}
