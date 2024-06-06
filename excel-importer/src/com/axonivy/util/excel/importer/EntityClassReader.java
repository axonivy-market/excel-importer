package com.axonivy.util.excel.importer;

import java.util.List;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.java.IJavaConfigurationManager;
import ch.ivyteam.ivy.process.data.persistence.IIvyEntityManager;
import ch.ivyteam.ivy.project.IIvyProjectManager;
import ch.ivyteam.ivy.scripting.dataclass.DataClassFieldModifier;
import ch.ivyteam.ivy.scripting.dataclass.IDataClassManager;
import ch.ivyteam.ivy.scripting.dataclass.IEntityClass;
import ch.ivyteam.ivy.scripting.dataclass.IEntityClassField;
import ch.ivyteam.ivy.scripting.dataclass.IProjectDataClassManager;

public class EntityClassReader {

  private final IProjectDataClassManager projectDataClassManager;
  private final IIvyEntityManager entityManager;

  public EntityClassReader(IIvyEntityManager entityManager) {
    this.projectDataClassManager = IDataClassManager.instance()
        .getProjectDataModelFor(Ivy.wfCase().getProcessModelVersion());
    this.entityManager = entityManager;
  }
  public EntityClassReader(IProjectDataClassManager manager, IIvyEntityManager entityManager) {
    this.projectDataClassManager = manager;
    this.entityManager = entityManager;
  }
  
//  public IEntityClass createEntity(Path filePath) {
//    Workbook wb = ExcelLoader.load(filePath);
//    String dataName = StringUtils.substringBeforeLast(filePath.getFileName().toString(), ".");
//    dataName = StringUtils.capitalize(dataName);
//    return toEntity(wb.getSheetAt(0), dataName);
//  }

  public IEntityClass createEntity(String dataName, List<Column> columns) {
    String fqName = projectDataClassManager.getDefaultNamespace() + "." + dataName;
    if (projectDataClassManager.findDataClass(fqName) != null) {
      throw new RuntimeException("entity " + fqName + " already exists");
    }
    IEntityClass entityClass = projectDataClassManager.createEntityClass(fqName, null);
    withIdField(entityClass);
    columns.stream().forEachOrdered(col -> {
      var field = entityClass.addField(col.getName(), col.getType().getName());
      field.setComment(col.getName());
      field.setDatabaseFieldLength(String.valueOf(col.getDatabaseFieldLength()));
    });

    entityClass.save();
    createTable(entityClass);
    return entityClass;
  }

  private void createTable(IEntityClass entityClass) {
    entityClass.buildJavaSource(List.of(), null);
    var java = IJavaConfigurationManager.instance().getJavaConfiguration(entityClass.getResource().getProject());
    var ivy = IIvyProjectManager.instance().getIvyProject(entityClass.getResource().getProject());
    Class<?> clazz;
    try {
      ivy.build(null);
      clazz = java.getClassLoader().loadClass(entityClass.getName());
    } catch (Exception ex) {
      throw new RuntimeException("Failed to load entity class " + entityClass, ex);
    }
    entityManager.findAll(clazz); // creates the schema through 'hibernate.hbm2ddl.auto=create'
  }

  private void withIdField(IEntityClass entity) {
    IEntityClassField id = entity.addField("id", Integer.class.getSimpleName());
    id.setDatabaseFieldName("id");
    id.addModifier(DataClassFieldModifier.PERSISTENT);
    id.addModifier(DataClassFieldModifier.ID);
    id.addModifier(DataClassFieldModifier.GENERATED);
    id.setComment("Identifier");
  }

}
