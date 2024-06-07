package com.axonivy.util.excel.importer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class Utils {
  public static boolean isCellInteger(Cell cell) {
    if (cell == null || cell.getCellType() != CellType.NUMERIC) {
      return false;
    }
    double cellValue = cell.getNumericCellValue();
    return cellValue == (int) cellValue;
  }

}
