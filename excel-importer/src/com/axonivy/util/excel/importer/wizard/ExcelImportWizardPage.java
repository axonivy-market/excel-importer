package com.axonivy.util.excel.importer.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import ch.ivyteam.ivy.designer.ui.wizard.restricted.WizardStatus;
import ch.ivyteam.swt.dialogs.SwtCommonDialogs;

public class ExcelImportWizardPage extends WizardPage implements IWizardPage, Listener {

  static final String PAGE_ID = "ImportExcel";
  private final ExcelImportProcessor processor;
  private ExcelUi ui;

  public ExcelImportWizardPage(ExcelImportProcessor processor) {
    super(PAGE_ID);
    setMessage(processor.getWizardPageOkMessage(PAGE_ID));
    this.processor = processor;
    setPageComplete(false);
  }

  private static class ExcelUi extends Composite {

    public final ComboViewer destinationNameField;
    public final Text entity;
    public final ComboViewer sourceProjectField;
    public final Button destinationBrowseButton;
    public final ComboViewer persistence;

    public ExcelUi(Composite parent) {
      super(parent, SWT.NONE);

      GridLayout layout = new GridLayout();
      layout.numColumns = 3;
      setLayout(layout);
      setLayoutData(new GridData(272));

      Label destinationLabel = new Label(this, 0);
      destinationLabel.setText("From file");
      destinationNameField = new ComboViewer(this, 2052);
      var dataDest = new GridData(768);
      dataDest.widthHint = 250;
      destinationNameField.getCombo().setLayoutData(dataDest);
      destinationBrowseButton = new Button(this, 8);
      destinationBrowseButton.setText("Browse ...");

      Label entityLabel = new Label(this, SWT.NONE);
      entityLabel.setText("Entity");
      this.entity = new Text(this, SWT.BORDER);
      GridData entGrid = new GridData(768);
      entGrid.widthHint = 250;
      entGrid.horizontalSpan = 2;
      entity.setLayoutData(entGrid);

      Label sourceLabel = new Label(this, 0);
      sourceLabel.setText("Project");
      this.sourceProjectField = new ComboViewer(this, 2060);
      GridData data = new GridData(768);
      data.widthHint = 250;
      data.horizontalSpan = 2;
      sourceProjectField.getCombo().setLayoutData(data);

      Label unitLabel = new Label(this, SWT.NONE);
      unitLabel.setText("Persistence");
      this.persistence = new ComboViewer(this, 2060);
      GridData data3 = new GridData(768);
      data3.widthHint = 250;
      data3.horizontalSpan = 2;
      persistence.getCombo().setLayoutData(data3);
    }
  }

  @Override
  public void createControl(Composite parent) {
    this.ui = new ExcelUi(parent);

    for (String projectName : ExcelImportUtil.getIvyProjectNames()) {
      ui.sourceProjectField.add(projectName);
    }
    Combo src = ui.sourceProjectField.getCombo();
    src.setText(processor.getSelectedSourceProjectName());
    src.addListener(SWT.Modify, this);
    src.addListener(SWT.Selection, this);

    Combo dst = ui.destinationNameField.getCombo();
    dst.addListener(SWT.Modify, this);
    dst.addListener(SWT.Selection, this);
    ui.destinationBrowseButton.addListener(SWT.Selection, this);

    ui.entity.addListener(SWT.Modify, this);

    Combo persist = ui.persistence.getCombo();
    persist.addListener(SWT.Modify, this);
    persist.addListener(SWT.Selection, this);

    setButtonLayoutData(ui.destinationBrowseButton);
    setControl(ui);

    String[] destinations = getDialogSettings().getArray(ExcelImportUtil.DESTINATION_KEY);
    if (destinations != null) {
      fileSelected(destinations[0]);
      for (String destination : destinations) {
        if (destination.endsWith(ExcelImportUtil.DEFAULT_EXTENSION)) {
          ui.destinationNameField.add(destination);
        }
      }
    }
  }

  @Override
  public void handleEvent(Event event) {
    Widget source = event.widget;
    if (source.equals(ui.destinationBrowseButton)) {
      handleDestinationBrowseButtonPressed();
    } else {
      handleInputChanged();
    }
  }

  boolean finish() {
    if (processor.wizardFinishInvoked() && executeImport()) {
      saveDialogSettings();
      return true;
    }
    return false;
  }

  protected void handleInputChanged() {
    var status = WizardStatus.createOkStatus();

    status.merge(processor.setImportFile(ui.destinationNameField.getCombo().getText()));
    status.merge(processor.setEntityName(ui.entity.getText()));
    String newProject = ui.sourceProjectField.getCombo().getText();
    var sameProject = Objects.equals(processor.getSelectedSourceProjectName(), newProject);
    status.merge(processor.setProject(newProject));
    if (!sameProject || ui.persistence.getCombo().getItemCount() == 0) {
      ui.persistence.getCombo().setItems(processor.units().toArray(String[]::new)); // update
    }

    status.merge(processor.setPersistence(ui.persistence.getCombo().getText()));

    setPageComplete(status.isLowerThan(WizardStatus.ERROR));
    if (status.isOk()) {
      setMessage(processor.getWizardPageOkMessage(PAGE_ID), 0);
    } else if (status.isFatal()) {
      SwtCommonDialogs.openBugDialog(getControl(), status.getFatalError());
    } else {
      setMessage(status.getMessage(), status.getSeverity());
    }
  }

  private void saveDialogSettings() {
    Combo dest = ui.destinationNameField.getCombo();
    List<String> destinations = new LinkedList<String>(Arrays.asList(dest.getItems()));
    String path = dest.getText();
    String lowerCasePath = path.toLowerCase();
    if (destinations.contains(path)) {
      destinations.remove(path);
      destinations.add(0, path);
      getDialogSettings().put(ExcelImportUtil.DESTINATION_KEY, destinations.toArray(String[]::new));
    } else if (lowerCasePath.endsWith(ExcelImportUtil.DEFAULT_EXTENSION)) {
      if (destinations.size() == 10) {
        destinations.remove(destinations.size() - 1);
      }
      destinations.add(0, path);
      getDialogSettings().put(ExcelImportUtil.DESTINATION_KEY, destinations.toArray(String[]::new));
    }
  }

  private void handleDestinationBrowseButtonPressed() {
    FileDialog dialog = new FileDialog(getContainer().getShell(), 0);
    dialog.setFilterExtensions(ExcelImportUtil.IMPORT_TYPE);
    dialog.setText("Select import file");
    dialog.setFilterPath(StringUtils.EMPTY);
    String currentSourceString = ui.destinationNameField.getCombo().getText();
    dialog.setFilterPath(currentSourceString);
    String selectedFileName = dialog.open();
    if (selectedFileName != null) {
      fileSelected(selectedFileName);
    }
  }

  private void fileSelected(String selectedFileName) {
    ui.destinationNameField.getCombo().setText(selectedFileName);
    ui.entity.setText(proposeName(selectedFileName));
  }

  private static String proposeName(String selection) {
    if (selection == null) {
      return "";
    }
    String fileName = new File(selection).getName();
    String entityName = StringUtils.substringBeforeLast(fileName, ".");
    entityName = StringUtils.capitalize(entityName);
    return entityName;
  }

  private boolean executeImport() {
    Combo dst = ui.destinationNameField.getCombo();
    if (dst.getText().lastIndexOf(File.separator) == -1) {
      dst.setText(ExcelImportUtil.DEFAULT_FILTER_PATH + File.separator + dst.getText());
      processor.setImportFile(dst.getText());
    }
    try {
      getContainer().run(true, true, processor);
    } catch (InterruptedException localInterruptedException) {
      return false;
    } catch (InvocationTargetException e) {
      SwtCommonDialogs.openBugDialog(getControl(), e.getTargetException());
      return false;
    }
    var status = processor.getStatus();
    if (status.isOK()) {
      SwtCommonDialogs.openInformationDialog(getShell(), "Express Import", "Successfully imported");
    } else {
      SwtCommonDialogs.openErrorDialog(getContainer().getShell(),
              "Problems during import of Excel as Dialog", status.getMessage(), status.getException());
      return false;
    }
    return true;
  }
}
