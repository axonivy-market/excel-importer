package com.axonivy.util.excel.importer.wizard;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.axonivy.util.excel.importer.DialogCreator;
import com.axonivy.util.excel.importer.EntityClassReader;
import com.axonivy.util.excel.importer.EntityDataLoader;
import com.axonivy.util.excel.importer.ExcelLoader;
import com.axonivy.util.excel.importer.ProcessDrawer;

import ch.ivyteam.awt.swt.SwtRunnable;
import ch.ivyteam.eclipse.util.EclipseUtil;
import ch.ivyteam.eclipse.util.MonitorUtil;
import ch.ivyteam.ivy.application.IProcessModelVersion;
import ch.ivyteam.ivy.designer.ui.wizard.restricted.IWizardSupport;
import ch.ivyteam.ivy.designer.ui.wizard.restricted.WizardStatus;
import ch.ivyteam.ivy.eclipse.util.EclipseUiUtil;
import ch.ivyteam.ivy.process.data.persistence.IPersistenceContext;
import ch.ivyteam.ivy.process.data.persistence.datamodel.IProcessDataPersistenceConfigManager;
import ch.ivyteam.ivy.process.data.persistence.model.Persistence.PersistenceUnit;
import ch.ivyteam.ivy.project.IIvyProject;
import ch.ivyteam.ivy.project.IIvyProjectManager;
import ch.ivyteam.ivy.scripting.dataclass.IDataClassManager;
import ch.ivyteam.ivy.scripting.dataclass.IProjectDataClassManager;
import ch.ivyteam.ivy.search.restricted.ProjectRelationSearchScope;
import ch.ivyteam.util.io.resource.FileResource;
import ch.ivyteam.util.io.resource.nio.NioFileSystemProvider;

public class ExcelImportProcessor implements IWizardSupport, IRunnableWithProgress {

  private IIvyProject selectedSourceProject;
  private FileResource importFile;
  private IStatus status = Status.OK_STATUS;
  private String selectedPersistence;
  private String entityName;

  public ExcelImportProcessor(IStructuredSelection selection) {
    this.selectedSourceProject = ExcelImportUtil.getFirstNonImmutableIvyProject(selection);
  }

  @Override
  public String getWizardPageTitle(String pageId) {
    return "Import Excel as Dialog";
  }

  @Override
  public String getWizardPageOkMessage(String pageId) {
    return "Please specify an Excel file to import as Dialog";
  }

  @Override
  public boolean wizardFinishInvoked() {
    var okStatus = WizardStatus.createOkStatus();
    okStatus.merge(validateImportFileExits());
    okStatus.merge(validateSource());
    return okStatus.isLowerThan(WizardStatus.ERROR);
  }

  @Override
  public boolean wizardCancelInvoked() {
    return true;
  }

  @Override
  public void run(IProgressMonitor monitor) throws InvocationTargetException {
    SubMonitor progress = MonitorUtil.begin(monitor, "Importing", 1);
    try {
      status = Status.OK_STATUS;
      ResourcesPlugin.getWorkspace().run(m -> {
        var manager = IDataClassManager.instance().getProjectDataModelFor(selectedSourceProject.getProject());
        try {
          importExcel(manager, importFile, m);
        } catch (Exception ex) {
          status = EclipseUtil.createErrorStatus(ex);
        }
      }, null, IWorkspace.AVOID_UPDATE, progress);
    } catch (Exception ex) {
      status = EclipseUtil.createErrorStatus(ex);
    } finally {
      MonitorUtil.ensureDone(monitor);
    }
  }

  private void importExcel(IProjectDataClassManager manager, FileResource excel, IProgressMonitor monitor) throws Exception {
    Workbook wb = null;
    try(InputStream is = excel.read().inputStream()) {
      wb = ExcelLoader.load(excel.name(), excel.read().inputStream());
    }
    Sheet sheet = wb.getSheetAt(0);

    var newEntity = new EntityClassReader(manager).toEntity(sheet, entityName);
    newEntity.save();
    SwtRunnable.execNowOrAsync(()->
      EclipseUiUtil.openEditor(newEntity)
    );
    monitor.setTaskName("Created EntityClass "+entityName);

    IProcessModelVersion pmv = manager.getProcessModelVersion();
    var persist = pmv.getAdapter(IPersistenceContext.class);
    var ivyEntities = persist.get(selectedPersistence);
    EntityDataLoader loader = new EntityDataLoader(ivyEntities);
    var entityType = loader.createTable(newEntity);
    loader.load(sheet, newEntity);
    List<?> loaded = ivyEntities.findAll(entityType);
    System.out.println("inserted entities "+loaded.size());
    monitor.setTaskName("Loaded Excel rows into Database "+loaded.size());

    new DialogCreator().createDialog(newEntity, selectedPersistence);

    ProcessDrawer drawer = new ProcessDrawer(manager.getProject());
    var process = drawer.drawManager(newEntity);
    SwtRunnable.execNowOrAsync(()->
      EclipseUiUtil.openEditor(process)
    );
  }

  String getSelectedSourceProjectName() {
    if (selectedSourceProject == null) {
      return StringUtils.EMPTY;
    }
    return selectedSourceProject.getName();
  }

  public WizardStatus setImportFile(String text) {
    if (text != null) {
      try {
        importFile = NioFileSystemProvider.create(Path.of("/")).root().file(text);
      } catch (Exception ex) {
        return WizardStatus.createErrorStatus("Can't create file from "+text, ex);
      }
    } else {
      importFile = null;
    }
    return validateImportFileExits();
  }

  public WizardStatus setEntityName(String name) {
    this.entityName = name;
    if (entityName.isBlank()) {
      return WizardStatus.createErrorStatus("Need a valid name for the Data to import");
    }
    return WizardStatus.createOkStatus();
  }

  public WizardStatus setSource(String projectName) {
    selectedSourceProject = null;
    if (projectName != null) {
      selectedSourceProject = IIvyProjectManager.instance().getIvyProject(projectName);
    }
    return validateSource();
  }

  public WizardStatus setPersistence(String name) {
    selectedPersistence = null;
    if (StringUtils.isNotBlank(name)) {
      selectedPersistence = name;
    }
    return validatePersistence();
  }

  public IStatus getStatus() {
    return status;
  }

  private WizardStatus validateImportFileExits() {
    if (importFile == null || !importFile.exists()) {
      return WizardStatus.createErrorStatus("Import file does not exist");
    }
    return WizardStatus.createOkStatus();
  }

  private WizardStatus validateSource() {
    if (selectedSourceProject == null) {
      return WizardStatus.createErrorStatus("Please specify an Axon Ivy project");
    }
    return WizardStatus.createOkStatus();
  }

  private WizardStatus validatePersistence() {
    if (selectedPersistence == null || !units().contains(selectedPersistence)) {
      return WizardStatus.createErrorStatus("Please specify a Persistence DB to store XLS data");
    }
    return WizardStatus.createOkStatus();
  }

  public List<String> units() {
    if (selectedSourceProject == null) {
      return List.of();
    }
    var main = IProcessDataPersistenceConfigManager.instance();
    var local = main.getProjectDataModelFor(selectedSourceProject.getProject());
    return local.getDataModels(ProjectRelationSearchScope.CURRENT_AND_ALL_REQUIRED_PROJECTS, null)
      .getModels().stream()
      .flatMap(c -> c.getPersistenceUnitConfigs().stream())
      .map(PersistenceUnit::getName)
      .toList();
  }
}
