package com.axonivy.util.excel.importer;


import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import ch.ivyteam.ivy.dialog.configuration.DialogCreationParameters;
import ch.ivyteam.ivy.dialog.configuration.IUserDialog;
import ch.ivyteam.ivy.dialog.configuration.IUserDialogManager;
import ch.ivyteam.ivy.process.IProcess;
import ch.ivyteam.ivy.process.model.Process;
import ch.ivyteam.ivy.process.model.diagram.shape.DiagramShape;
import ch.ivyteam.ivy.process.model.diagram.value.PositionDelta;
import ch.ivyteam.ivy.process.model.element.activity.Script;
import ch.ivyteam.ivy.process.model.element.activity.value.dialog.UserDialogId;
import ch.ivyteam.ivy.process.model.element.activity.value.dialog.UserDialogStart;
import ch.ivyteam.ivy.process.model.element.event.start.dialog.html.HtmlDialogStart;
import ch.ivyteam.ivy.process.model.element.event.start.value.CallSignature;
import ch.ivyteam.ivy.process.model.value.scripting.QualifiedType;
import ch.ivyteam.ivy.process.model.value.scripting.VariableDesc;
import ch.ivyteam.ivy.scripting.dataclass.IEntityClass;

@SuppressWarnings("restriction")
public class DialogCreator {

  public IUserDialog createDialog(IEntityClass entity, String unit) {
    var global = IUserDialogManager.instance();
    IProject project = entity.getResource().getProject();
    var local = global.getProjectDataModelFor(project);

    var target = dialogStartFor(entity);

    VariableDesc entries = new VariableDesc("entries", new QualifiedType(List.class.getName(), List.of(new QualifiedType(entity.getName()))));
    var params = new DialogCreationParameters.Builder(project, target.getId().getRawId())
      .signature(target.getStartMethod())
      .dataClassFields(List.of(entries))
      .toCreationParams();
    var userDialog = local.createProjectUserDialog(params, null);

    IProcess processRdm = userDialog.getProcess(null);
    Process process = processRdm.getModel();
    extendProcess(process, entity, unit);
    processRdm.save();

    extendView(userDialog.getViewFile());

    return userDialog;
  }

  private void extendProcess(Process process, IEntityClass entity, String unit) {
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

  private void extendView(IFile viewFile) {
    try(InputStream is = DialogCreator.class.getResourceAsStream("/com/axonivy/util/excel/importer/EntityManager/EntityManager.xhtml")) {
      viewFile.setContents(is, 0, null);
    } catch (Exception ex) {
      throw new RuntimeException("Failed to extend view for "+viewFile, ex);
    }
  }

  public static UserDialogStart dialogStartFor(IEntityClass entity) {
    var dialogId = UserDialogId.create(entity.getName()+"Manager");
    var target = new UserDialogStart(dialogId, new CallSignature("start"));
    return target;
  }

}
