package com.axonivy.util.excel.importer;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import ch.ivyteam.ivy.dialog.configuration.DialogCreationParameters;
import ch.ivyteam.ivy.dialog.configuration.IUserDialog;
import ch.ivyteam.ivy.dialog.configuration.IUserDialogManager;
import ch.ivyteam.ivy.process.IProcess;
import ch.ivyteam.ivy.process.model.element.activity.value.dialog.UserDialogId;
import ch.ivyteam.ivy.process.model.element.activity.value.dialog.UserDialogStart;
import ch.ivyteam.ivy.process.model.element.event.start.value.CallSignature;
import ch.ivyteam.ivy.process.model.value.scripting.QualifiedType;
import ch.ivyteam.ivy.process.model.value.scripting.VariableDesc;
import ch.ivyteam.ivy.scripting.dataclass.IEntityClass;
import ch.ivyteam.ivy.scripting.dataclass.IEntityClassField;

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
    new DialogProcess(processRdm.getModel(), entity, unit).extendProcess();
    processRdm.save();

    extendView(userDialog.getViewFile(), entity);

    return userDialog;
  }

  private void extendView(IFile viewFile, IEntityClass entity) {
    try(InputStream is = DialogCreator.class.getResourceAsStream("/com/axonivy/util/excel/importer/EntityManager/EntityManager.xhtml")) {
      var bos = new ByteArrayOutputStream();
      is.transferTo(bos);
      var template = new String(bos.toByteArray());

      String rendered = renderFields(entity, template);

      var bis = new ByteArrayInputStream(rendered.getBytes());
      viewFile.setContents(bis, 0, null);
    } catch (Exception ex) {
      throw new RuntimeException("Failed to extend view for "+viewFile, ex);
    }
  }

  private String renderFields(IEntityClass entity, String template) {
    String fieldXhtml = entity.getFields().stream()
      .filter(fld -> !fld.getName().equals("id"))
      .map(this::htmlview)
      .collect(Collectors.joining("\n"));
    return template.replace("<!-- [entity.fields] -->", fieldXhtml);
  }

  private String htmlview(IEntityClassField field) {
    String fieldXhtml = """
        <p:column headerText="%s">
          <h:outputText value="#{entity.%s}"/>
        </p:column>
    """.formatted(field.getName(), field.getName());
    return fieldXhtml;
  }

  public static UserDialogStart dialogStartFor(IEntityClass entity) {
    var dialogId = UserDialogId.create(entity.getName()+"Manager");
    var target = new UserDialogStart(dialogId, new CallSignature("start"));
    return target;
  }

}
