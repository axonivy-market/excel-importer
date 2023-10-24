package com.axonivy.util.excel.importer;

import java.util.List;

import ch.ivyteam.ivy.process.model.Process;
import ch.ivyteam.ivy.process.model.diagram.shape.DiagramShape;
import ch.ivyteam.ivy.process.model.diagram.value.Position;
import ch.ivyteam.ivy.process.model.diagram.value.PositionDelta;
import ch.ivyteam.ivy.process.model.element.activity.Script;
import ch.ivyteam.ivy.process.model.element.event.start.dialog.html.HtmlDialogMethodStart;
import ch.ivyteam.ivy.process.model.element.event.start.dialog.html.HtmlDialogStart;
import ch.ivyteam.ivy.process.model.element.event.start.value.CallSignature;
import ch.ivyteam.ivy.process.model.element.value.Mappings;
import ch.ivyteam.ivy.process.model.value.MappingCode;
import ch.ivyteam.ivy.process.model.value.scripting.QualifiedType;
import ch.ivyteam.ivy.process.model.value.scripting.VariableDesc;
import ch.ivyteam.ivy.scripting.dataclass.IEntityClass;

@SuppressWarnings("restriction")
public class DialogProcess {

  private final Process process;
  private final IEntityClass entity;
  private final String unit;

  private final int x = 96;
  private int y = 248;

  public DialogProcess(Process process, IEntityClass entity, String unit) {
    this.process = process;
    this.entity = entity;
    this.unit = unit;
  }

  public void extendProcess() {
    addDbLoaderScript();
    addDeleteAction();
    addEditAction();
  }

  private void addDbLoaderScript() {
    var start = process.search().type(HtmlDialogStart.class).findOne();
    var startEnd = start.getOutgoing().get(0).getTarget();
    DiagramShape endShape = startEnd.getShape();
    endShape.move(new PositionDelta(200, 0)); // space for loader script

    Script loader = process.add().element(Script.class);
    loader.setName("load db");
    loader.setCode("""
            out.entries = ivy.persistence.%s.findAll(%s.class);
            """.formatted(unit, entity.getName()));

    DiagramShape scriptShape = loader.getShape();
    scriptShape.moveTo(start.getShape().getBounds().getCenter().shiftX(150));
    process.connections().reconnect(start.getOutgoing().get(0)).to(loader);

    scriptShape.edges().connectTo(endShape);
  }

  private void addDeleteAction() {
    var delete = addMethod();
    delete.setName("delete(" + entity.getSimpleName() + ")");
    var param = new VariableDesc("entity", new QualifiedType(entity.getName()));
    delete.setSignature(new CallSignature("delete").setInputParameters(List.of(param)));

    var template = """
      import NAME;
      SHORT loaded = ivy.persistence.UNIT.find(SHORT.class, param.entity.getId()) as SHORT;
      ivy.persistence.UNIT.remove(loaded);
      out.entries.remove(param.entity);
      """;
    var code = template
            .replaceAll("NAME", entity.getName())
            .replaceAll("SHORT", entity.getSimpleName())
            .replaceAll("UNIT", unit);

    delete.setOutput(new MappingCode(code));
  }

  private void addEditAction() {
    var edit = addMethod();
    edit.setName("edit(" + entity.getSimpleName() + ")");
    var param = new VariableDesc("entity", new QualifiedType(entity.getName()));
    edit.setSignature(new CallSignature("edit").setInputParameters(List.of(param)));

    edit.setOutput(new MappingCode(Mappings.single("out.edit", "param.entity")));
  }

  private HtmlDialogMethodStart addMethod() {
    var delete = process.add().element(HtmlDialogMethodStart.class);
    delete.getShape().moveTo(new Position(x, y));
    y += 120;
    return delete;
  }

}