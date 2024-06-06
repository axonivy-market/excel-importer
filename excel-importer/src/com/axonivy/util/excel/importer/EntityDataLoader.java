package com.axonivy.util.excel.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.hibernate.internal.SessionImpl;

import ch.ivyteam.ivy.process.data.persistence.IIvyEntityManager;
import ch.ivyteam.log.Logger;

public class EntityDataLoader implements AutoCloseable {

  private static final Logger LOGGER = Logger.getLogger(EntityDataLoader.class);
  private static final String DELIMITER = ", ";
  private String tableName;
  private List<Column> columns;
  private PreparedStatement statement;
  private EntityManager entityManager;
  private Connection connection;
  private AtomicInteger rowCount = new AtomicInteger();

  public EntityDataLoader(IIvyEntityManager entityManager) throws SQLException {
    this.entityManager = entityManager.createEntityManager();
    this.connection = this.entityManager.unwrap(SessionImpl.class).getJdbcConnectionAccess().obtainConnection();
  }

  public void load(String tableName, Sheet sheet) throws SQLException {
    this.tableName = tableName;
    this.columns = ExcelReader.parseColumns(sheet);
    load(sheet, new NullProgressMonitor());
  }

  public void load(Sheet sheet, IProgressMonitor monitor) throws SQLException {
    monitor.beginTask("Importing Excel data rows", sheet.getLastRowNum());
    Iterator<Row> rows = sheet.rowIterator();
    rows.next(); // skip header
    loadRows(rows);
  }

  public void save() throws SQLException {
    Objects.requireNonNull(statement);
    connection.setAutoCommit(false);
    statement.executeBatch();
    connection.commit();
  }

  private void loadRows(Iterator<Row> rows) throws SQLException {
    Objects.requireNonNull(tableName);
    Objects.requireNonNull(columns);

    var query = buildInsertQuery();
    LOGGER.info("Prepared insert query: " + query);
    statement = connection.prepareStatement(query, Statement.NO_GENERATED_KEYS);
    rows.forEachRemaining(row -> {
      try {
        rowCount.incrementAndGet();
        insertCallValuesAsParameter(row);
        statement.addBatch();
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
    });
    LOGGER.info("Generated " + rowCount + " inserts");
  }

  private void insertCallValuesAsParameter(Row row) throws SQLException {
    int c = 0;
    for (var column : columns) {
      Cell cell = row.getCell(c);
      updateColumn(column, cell);
      Object value = getValue(cell);
      c++;
      statement.setObject(c, value);
    }
  }

  private String buildInsertQuery() {
    String colNames = columns.stream().map(Column::getName).collect(Collectors.joining(DELIMITER));
    String placeholders = columns.stream().map(i -> "?").collect(Collectors.joining(DELIMITER));
    return String.format("INSERT INTO %s (%s)\nVALUES (%s)", tableName, colNames, placeholders);
  }

  private Object getValue(Cell cell) {
    if (cell == null) {
      return null;
    }
    if (cell.getCellType() == CellType.NUMERIC) {
      if (DateUtil.isCellDateFormatted(cell)) {
        return cell.getDateCellValue();
      }
      return cell.getNumericCellValue();
    } else if (cell.getCellType() == CellType.BOOLEAN) {
      return cell.getBooleanCellValue();
    }
    return cell.getStringCellValue();
  }

  private void updateColumn(Column column, Cell cell) {
    if (cell == null) {
      return;
    }
    if (cell.getCellType() == CellType.NUMERIC && column.getType().equals(Integer.class)
        && !Utils.isCellInteger(cell)) {
      column.setType(Double.class);
    }
    if (column.getType().equals(String.class)) {
      var cellLength = cell.getStringCellValue().length();
      if (cellLength > column.getDatabaseFieldLength()) {
        column.setDatabaseFieldLength(cellLength);
      }
    }
  }

  @Override
  public void close() throws Exception {
    connection.close();
    entityManager.close();
  }

  public PreparedStatement getStatement() {
    return statement;
  }

  public void setStatement(PreparedStatement statement) {
    this.statement = statement;
  }

  public List<Column> getColumns() {
    return columns;
  }

  public AtomicInteger getRowCount() {
    return rowCount;
  }

}
