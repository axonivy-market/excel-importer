package com.axonivy.util.excel.importer;

public class Column {

  private String name;
  private Class<?> type;
  private Integer databaseFieldLength;
  private String comment;

  public Column(String name, Class<?> type, Integer databaseFieldLength, String comment) {
    super();
    this.name = name;
    this.type = type;
    this.databaseFieldLength = databaseFieldLength;
    this.comment = comment;
  }

  public Column(String name, Class<?> type, String comment) {
    super();
    this.name = name;
    this.type = type;
    this.comment = comment;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Class<?> getType() {
    return type;
  }

  public void setType(Class<?> type) {
    this.type = type;
  }

  public Integer getDatabaseFieldLength() {
    return databaseFieldLength;
  }

  public void setDatabaseFieldLength(Integer databaseFieldLength) {
    this.databaseFieldLength = databaseFieldLength;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Column column = (Column) o;

    if (!name.equals(column.name))
      return false;
    if (!type.equals(column.type))
      return false;
    return databaseFieldLength != null ? databaseFieldLength.equals(column.databaseFieldLength)
        : column.databaseFieldLength == null;
  }

  @Override
  public String toString() {
    return "Column{" + "name='" + name + '\'' + ", type=" + type + ", databaseFieldLength="
        + (databaseFieldLength != null ? databaseFieldLength : "null") + '}';
  }

}