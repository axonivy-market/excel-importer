package com.axonivy.utils.excel.importer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.ivyteam.ivy.process.IProcess;
import ch.ivyteam.ivy.process.model.Process;
import ch.ivyteam.ivy.process.model.diagram.Diagram;
import ch.ivyteam.ivy.process.model.element.activity.DialogCall;
import ch.ivyteam.ivy.process.model.element.event.end.TaskEnd;
import ch.ivyteam.ivy.process.model.element.event.start.RequestStart;
import ch.ivyteam.ivy.process.model.element.event.start.value.CallSignature;
import ch.ivyteam.ivy.process.resource.ProcessCreator;
import ch.ivyteam.ivy.scripting.dataclass.IEntityClass;

@SuppressWarnings("restriction")
public class ProcessDrawer {

  private IProject project;

  public ProcessDrawer(IProject project) {
    this.project = project;
  }

  public IProcess drawManager(IEntityClass entity) {
    String name = entity.getSimpleName();

    var rdm = ProcessCreator.create(project, "Manage"+name)
      .createDefaultContent(false)
      .toCreator()
      .createDataModel(new NullProgressMonitor());

    Process process = rdm.getModel();
    drawProcess(process);

    DialogCall call = process.search().type(DialogCall.class).findOne();
    call.setName(name+" UI");
    call.setTargetDialog(DialogCreator.dialogStartFor(entity));

    rdm.save();

    return rdm;
  }

  private void drawProcess(Process process) {
    Diagram diagram = process.getDiagram();
    var start = diagram.add().shape(RequestStart.class).at(50, 50);
    RequestStart starter = start.getElement();
    starter.setSignature(new CallSignature("start"));
    starter.setName("start");

    var dialog = diagram.add().shape(DialogCall.class).at(180, 50);
    start.edges().connectTo(dialog);

    var end = diagram.add().shape(TaskEnd.class).at(300, 50);
    dialog.edges().connectTo(end);
  }

}
