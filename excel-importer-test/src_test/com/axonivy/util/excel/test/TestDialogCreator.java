package com.axonivy.util.excel.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.axonivy.util.excel.importer.DialogCreator;
import com.axonivy.util.excel.importer.EntityClassReader;
import com.axonivy.util.excel.importer.ExcelLoader;

import ch.ivyteam.ivy.dialog.configuration.IUserDialog;
import ch.ivyteam.ivy.environment.IvyTest;
import ch.ivyteam.ivy.process.model.Process;
import ch.ivyteam.ivy.process.model.element.activity.Script;
import ch.ivyteam.ivy.process.model.element.event.start.dialog.html.HtmlDialogMethodStart;
import ch.ivyteam.ivy.scripting.dataclass.IDataClass;
import ch.ivyteam.ivy.scripting.dataclass.IDataClassField;
import ch.ivyteam.ivy.scripting.dataclass.IEntityClass;

@IvyTest
@SuppressWarnings("restriction")
public class TestDialogCreator {

  private EntityClassReader reader;

  @Test
  void createEntityDialog(@TempDir Path dir) throws IOException, CoreException {
    Path path = dir.resolve("customers.xlsx");
    TstRes.loadTo(path, "sample.xlsx");

    Workbook wb = ExcelLoader.load(path);
    Sheet customerSheet = wb.getSheetAt(0);

    IEntityClass customer = reader.toEntity(customerSheet, "customer");
    IUserDialog dialog = null;
    try {
      customer.save(new NullProgressMonitor());

      String unit = "testing";
      dialog = new DialogCreator().createDialog(customer, unit);

      assertData(dialog.getDataClass(null));
      assertProcess(customer, dialog.getProcess(null).getModel());
      assertView(read(dialog.getViewFile()));

    } finally {
      customer.getResource().delete(true, new NullProgressMonitor());
      if (dialog != null) {
        dialog.getResource().delete(true, null);
      }
    }
  }

  private void assertData(IDataClass dataClass) {
    assertThat(dataClass.getFields()).extracting(IDataClassField::getName)
      .containsOnly("entries", "edit");
  }

  private void assertProcess(IEntityClass customer, Process process) {
    Script loader = process.search().type(Script.class).findOne();
    assertThat(loader.getCode()).contains(customer.getName());
    var delete = process.search().type(HtmlDialogMethodStart.class).findOne();
    String removal = delete.getOutput().getCode();
    assertThat(removal)
      .contains("testing.remove(");
  }

  private void assertView(String view) {
    assertThat(view).contains("p:dataTable");
    assertThat(view)
      .as("visualizes properties of the entity")
      .contains("firstname")
      .doesNotContain("<!-- [entity.fields] -->");
  }

  private static String read(IFile viewFile) throws IOException, CoreException {
    try(InputStream in = viewFile.getContents()) {
      var bos = new java.io.ByteArrayOutputStream();
      in.transferTo(bos);
      return new String(bos.toByteArray());
    }
  }

  @BeforeEach
  void setup() {
    this.reader = new EntityClassReader();
  }

}
