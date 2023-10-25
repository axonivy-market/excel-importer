package com.axonivy.util.excel.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.axonivy.util.excel.importer.EntityClassReader;

import ch.ivyteam.ivy.environment.IvyTest;
import ch.ivyteam.ivy.scripting.dataclass.IEntityClassField;

@IvyTest
public class TestEntityClassCreator {

  private EntityClassReader reader;

  @Test
  void readToEntity(@TempDir Path dir) throws IOException {
    Path path = dir.resolve("customers.xlsx");
    TstRes.loadTo(path, "sample.xlsx");

    var entity = reader.getEntity(path);
    assertThat(entity).isNotNull();
  }

  @Test
  void readGermanized(@TempDir Path dir) throws Exception {
    Path path = dir.resolve("Arzneimittel.xlsx");
    TstRes.loadTo(path, "ArzneimittelLight.xlsx");

    var entity = reader.getEntity(path);
    assertThat(entity).isNotNull();
    List<String> fields = entity.getFields().stream().map(IEntityClassField::getName).toList();
    for(String field : fields) {
      assertThat(field)
        .as("no whitespaces")
        .doesNotContain(" ")
        .doesNotContain("(")
        .doesNotContain("ä");
    }
    assertThat(entity.getField("anzahlInneresBehltnis").getComment())
      .as("preserve real column names")
      .isEqualTo("Anzahl Inneres Behältnis");
  }

  @BeforeEach
  void setup() {
    this.reader = new EntityClassReader();
  }

}
