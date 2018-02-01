// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.ui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.designer.ui.wizards.actions.JavaCamelJobScriptsExportWSAction;
import org.talend.camel.designer.ui.wizards.actions.JavaCamelJobScriptsExportWithMavenAction;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.service.IESBMicroService;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ui.wizards.exportjob.ExportTreeViewer;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage;
import org.talend.repository.ui.wizards.exportjob.JobScriptsExportWizardPage;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.BuildJobFactory;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManagerFactory;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

/**
 * DOC x class global comment. Detailled comment <br/>
 * 
 */
public class JavaCamelJobScriptsExportWSWizardPage extends JobScriptsExportWizardPage {

    private static final String EXPORTTYPE_KAR = Messages.getString("JavaCamelJobScriptsExportWSWizardPage.ExportKar");//$NON-NLS-1$

    private static final String EXPORTTYPE_SPRING_BOOT = Messages
            .getString("JavaCamelJobScriptsExportWSWizardPage.ExportSpringBoot");//$NON-NLS-1$

    // dialog store id constants
    private static final String STORE_DESTINATION_NAMES_ID = "JavaJobScriptsExportWizardPage.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$

    private boolean onlyExportDefaultContext;

    private boolean exportAsZip;

    protected Button exportAsZipButton;

    protected Combo exportTypeCombo;

    protected Composite pageComposite;

    protected Composite optionsGroupComposite;

    protected Composite destinationNameFieldComposite;

    protected Composite destinationNameFieldInnerComposite;

    public JavaCamelJobScriptsExportWSWizardPage(IStructuredSelection selection) {
        super("JavaCamelJobScriptsExportWSWizardPage", selection);
    }

    @Override
    protected void createUnzipOptionGroup(Composite parent) {
        // options group
        Group optionsGroup = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        optionsGroup.setLayout(layout);
        optionsGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        optionsGroup.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.BuildType")); //$NON-NLS-1$

        optionsGroup.setLayout(new GridLayout(2, false));

        Label label = new Label(optionsGroup, SWT.NONE);
        label.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.BuildLabel")); //$NON-NLS-1$

        exportTypeCombo = new Combo(optionsGroup, SWT.PUSH);

        // TESB-5328
        exportTypeCombo.add(EXPORTTYPE_KAR);
        if (PluginChecker.isTIS()) {
            exportTypeCombo.add(EXPORTTYPE_SPRING_BOOT);
        }
        // exportTypeCombo.setEnabled(false); // only can export kar file
        exportTypeCombo.setText(EXPORTTYPE_KAR);

        exportTypeCombo.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                Widget source = e.widget;
                if (source instanceof Combo) {
                    String destination = ((Combo) source).getText();
                    boolean isMS = destination.equals(EXPORTTYPE_SPRING_BOOT);

                    if (isMS) {
                        contextButton.setEnabled(true);
                        exportAsZipButton.setEnabled(true);
                    } else {
                        contextButton.setEnabled(false);
                        exportAsZipButton.setEnabled(false);
                    }

                    String destinationValue = getDestinationValue();

                    if (isMS) {
                        if (exportAsZip || isAddMavenScript()) {
                            destinationValue = destinationValue.substring(0, destinationValue.lastIndexOf("."))
                                    + FileConstants.ZIP_FILE_SUFFIX;
                        } else {
                            destinationValue = destinationValue.substring(0, destinationValue.lastIndexOf("."))
                                    + FileConstants.JAR_FILE_SUFFIX;
                        }
                    } else {
                        destinationValue = destinationValue.substring(0, destinationValue.lastIndexOf(".")) + getOutputSuffix();
                    }

                    setDestinationValue(destinationValue);
                }

            }
        });
    }

    @Override
    public void createOptions(final Composite optionsGroup, Font font) {
        createOptionsForKar(optionsGroup, font);
        restoreWidgetValuesForKar();
    }

    @Override
    public JobScriptsManager createJobScriptsManager() {
        String launcher = launcherCombo == null || launcherCombo.isDisposed() ? "all" : launcherCombo.getText();
        String context = contextCombo == null || contextCombo.isDisposed() ? "Default" : contextCombo.getText();
        return JobScriptsManagerFactory.createManagerInstance(getExportChoiceMap(), context, launcher, IProcessor.NO_STATISTICS,
                IProcessor.NO_TRACES, JavaJobScriptsExportWSWizardPage.JobExportType.getTypeFromString(EXPORTTYPE_KAR));
    }

    /**
     * Answer the suffix that files exported from this wizard should have. If this suffix is a file extension (which is
     * typically the case) then it must include the leading period character.
     * 
     */
    @Override
    protected String getOutputSuffix() {
        return FileConstants.KAR_FILE_SUFFIX;
    }

    /**
     * Open an appropriate destination browser so that the user can specify a source to import from.
     */
    @Override
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);

        if (isAddMavenScript()) {
            dialog.setFilterExtensions(new String[] { "*" + FileConstants.ZIP_FILE_SUFFIX, "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (EXPORTTYPE_SPRING_BOOT.equals(exportTypeCombo.getText())) {
            if (exportAsZip || isAddMavenScript()) {
                dialog.setFilterExtensions(new String[] { "*" + FileConstants.ZIP_FILE_SUFFIX, "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                dialog.setFilterExtensions(new String[] { "*.jar", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else {
            dialog.setFilterExtensions(new String[] { "*.kar", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        }
        dialog.setText(""); //$NON-NLS-1$
        // this is changed by me shenhaize
        dialog.setFileName(this.getDefaultFileName().get(0));
        String currentSourceString = getDestinationValue();
        int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
            dialog.setFilterPath(currentSourceString.substring(0, lastSeparatorIndex));
        }

        String selectedFileName = dialog.open();
        if (selectedFileName == null) {
            return;
        }
        String idealSuffix;
        if (isAddMavenScript()) {
            idealSuffix = FileConstants.ZIP_FILE_SUFFIX;
        } else if (EXPORTTYPE_SPRING_BOOT.equals(exportTypeCombo.getText())) {
            if (exportAsZip || isAddMavenScript()) {
                idealSuffix = FileConstants.ZIP_FILE_SUFFIX;
            } else {
                idealSuffix = FileConstants.JAR_FILE_SUFFIX;
            }
        } else {
            idealSuffix = getOutputSuffix();
        }
        if (!selectedFileName.endsWith(idealSuffix)) {
            selectedFileName += idealSuffix;
        }
        // when user change the name of job,will add the version auto
        if (selectedFileName != null && !selectedFileName.endsWith(this.getSelectedJobVersion() + idealSuffix)) {
            String b = selectedFileName.substring(0, (selectedFileName.length() - 4));
            File file = new File(b);

            String str = file.getName();

            String s = this.getDefaultFileName().get(0);

            if (str.equals(s)) {
                selectedFileName = b + "_" + this.getDefaultFileName().get(1) + idealSuffix; //$NON-NLS-1$
            } else {
                selectedFileName = b + idealSuffix;
            }

        }
        if (selectedFileName != null) {
            setErrorMessage(null);
            setDestinationValue(selectedFileName);

            if (getDialogSettings() != null) {
                IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
                if (section == null) {
                    section = getDialogSettings().addNewSection(DESTINATION_FILE);
                }
                section.put(DESTINATION_FILE, selectedFileName);
            }

        }
    }

    @Override
    public void handleEvent(Event e) {
        super.handleEvent(e);
        Widget source = e.widget;
        if (source instanceof Combo) {
            String destination = ((Combo) source).getText();
            if (getDialogSettings() != null) {
                IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
                if (section == null) {
                    section = getDialogSettings().addNewSection(DESTINATION_FILE);
                }
                section.put(DESTINATION_FILE, destination);
            }
        }
    }

    public boolean isAddMavenScript() {
        if (addBSButton != null) {
            return addBSButton.getSelection();
        }

        return false;
    }

    @Override
    protected Map<ExportChoice, Object> getExportChoiceMap() {
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<ExportChoice, Object>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.needJobItem, false);
        exportChoiceMap.put(ExportChoice.needSourceCode, false);

        exportChoiceMap.put(ExportChoice.needMetaInfo, true);
        exportChoiceMap.put(ExportChoice.needContext, true);
        exportChoiceMap.put(ExportChoice.needLauncher, exportAsZip);
        if (addBSButton != null) {
            exportChoiceMap.put(ExportChoice.needMavenScript, addBSButton.getSelection());

            if (isAddMavenScript()) {
                exportChoiceMap.put(ExportChoice.needAssembly, true);
                exportChoiceMap.put(ExportChoice.needLauncher, true);
            }
        }
        exportChoiceMap.put(ExportChoice.onlyDefautContext, onlyExportDefaultContext);

        return exportChoiceMap;
    }

    private void createOptionsForKar(Composite optionsGroup, Font font) {
        if (!PluginChecker.isTIS()) {
            return;
        }

        addBSButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        addBSButton.setText("Add maven script");
        addBSButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                exportTypeCombo.notifyListeners(SWT.Selection, null);
            }
        });

        // TESB-17856: ESB Microservice can be exported only with Default context
        contextButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        contextButton.setText("Only export the default context"); //$NON-NLS-1$
        contextButton.setFont(font);
        contextButton.setEnabled(false);
        contextButton.setVisible(PluginChecker.isTIS());
        contextButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onlyExportDefaultContext = contextButton.getSelection();
            }
        });

        exportAsZipButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        exportAsZipButton.setText("Export as ZIP"); //$NON-NLS-1$
        exportAsZipButton.setSelection(false);
        exportAsZipButton.setFont(getFont());
        exportAsZipButton.setEnabled(false);
        exportAsZipButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean selectContext = exportAsZipButton.getSelection();
                exportAsZip = selectContext;
                exportTypeCombo.notifyListeners(SWT.Selection, null);
            }
        });
    }

    private void restoreWidgetValuesForKar() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames != null && directoryNames.length > 0) {
                String fileName = getDefaultFileName().get(0) + '_' + getDefaultFileName().get(1) + getOutputSuffix();
                for (String directoryName : directoryNames) {
                    String destination = new Path(directoryName).append(fileName).toOSString();
                    addDestinationItem(destination);
                    setDestinationValue(destination);
                }
            } else {
                setDefaultDestination();
            }
        } else {
            setDefaultDestination();
        }

    }

    @Override
    protected ExportTreeViewer getExportTree() {
        return new ExportCamelTreeViewer(selection, this) {

            @Override
            protected void checkSelection() {
                JavaCamelJobScriptsExportWSWizardPage.this.checkExport();
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.exportjob.JobScriptsExportWizardPage#checkExport()
     */
    @Override
    public boolean checkExport() {
        boolean noError = true;
        this.setErrorMessage(null);
        this.setPageComplete(true);
        if (getCheckNodes().length == 0) {
            this.setErrorMessage(Messages.getString("JavaJobScriptsExportWSWizardPage.needOneJobSelected")); //$NON-NLS-1$
            this.setPageComplete(false);
            noError = false;
        }
        return noError;
    }

    @Override
    public boolean finish() {

        String version = getSelectedJobVersion();
        String destinationKar = getDestinationValue();
        JavaCamelJobScriptsExportWSAction action = null;
        IRunnableWithProgress actionMS = null;
        Map<ExportChoice, Object> exportChoiceMap = getExportChoiceMap();
        boolean needMavenScript = exportChoiceMap.containsKey(ExportChoice.needMavenScript)
                && exportChoiceMap.get(ExportChoice.needMavenScript) == Boolean.TRUE;

        if (needMavenScript && destinationKar.regionMatches(true, destinationKar.length() - 4, ".kar", 0, 4)) {
            destinationKar = destinationKar.substring(0, destinationKar.length() - 3) + "zip";
        }

        if (new File(destinationKar).exists()) {
            boolean yes = MessageDialog.openQuestion(getShell(),
                    Messages.getString("JavaCamelJobScriptsExportWSWizardPage.OverwriteKarTitle"),
                    Messages.getString("JavaCamelJobScriptsExportWSWizardPage.OverwriteKarMessage"));
            if (!yes) {
                return false;
            }
        }

        IESBMicroService microService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBMicroService.class)) {
            microService = (IESBMicroService) GlobalServiceRegister.getDefault().getService(IESBMicroService.class);
        }
        
        if (exportTypeCombo.getText().equals(EXPORTTYPE_SPRING_BOOT)) {

            try {
                if (microService != null) {

                    actionMS = microService.createRunnableWithProgress(exportChoiceMap, Arrays.asList(getCheckNodes()), version,
                            destinationKar, "");

                    try {
                        getContainer().run(false, true, actionMS);
                    } catch (Exception e) {
                        MessageBoxExceptionHandler.process(e.getCause(), getShell());
                        return false;
                    }
                }

            } catch (Exception e) {
                MessageBoxExceptionHandler.process(e.getCause(), getShell());
                e.printStackTrace();
            }

        } else {

            if (getProcessItem() instanceof CamelProcessItem) {
                CamelProcessItem camelProcessItem = (CamelProcessItem) getProcessItem();
                if (camelProcessItem.isExportMicroService()) {
                    camelProcessItem.setExportMicroService(false);
                }
            }

            IBuildJobHandler buildJobHandler = null;

            if (needMavenScript) {
                action = new JavaCamelJobScriptsExportWithMavenAction(exportChoiceMap, nodes[0], version, destinationKar, false);
            } else {

                buildJobHandler = BuildJobFactory.createBuildJobHandler(getProcessItem(), getContextName(), version,
                        exportChoiceMap, JobExportType.POJO);

                action = new JavaCamelJobScriptsExportWSAction(nodes[0], version, destinationKar, false);

                ProcessorUtilities.setExportAsOSGI(true);
            }

            try {
                getContainer().run(false, true, action);
                try {
                    buildJobHandler.build(new NullProgressMonitor());
                } catch (Exception e) {
                    MessageBoxExceptionHandler.process(e.getCause(), getShell());
                    return false;
                }
            } catch (InvocationTargetException e) {
                MessageBoxExceptionHandler.process(e.getCause(), getShell());
                return false;
            } catch (InterruptedException e) {
                return false;
            }
            manager = action.getManager();
            // save output directory
            manager.setDestinationPath(destinationKar);
            saveWidgetValues();
        }

        return true;
    }

    /**
     * Hook method for saving widget values for restoration by the next instance of this class.
     */
    @Override
    protected void internalSaveWidgetValues() {
        // update directory names history
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null) {
                directoryNames = new String[0];
            }

            directoryNames = addToHistory(directoryNames, getDestinationValue());
            settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);
        }
    }

    // @Override
    // protected void internalSaveWidgetValues() {
    // // update directory names history
    // IDialogSettings settings = getDialogSettings();
    // if (settings != null) {
    // String[] directoryNames = new String[1];
    // String destinationValue = manager.getDestinationPath();
    // if (destinationValue != null) {
    // IPath path = Path.fromOSString(destinationValue);
    // destinationValue = path.removeLastSegments(1).toOSString();
    // }
    // directoryNames[0] = destinationValue;
    //
    // settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);
    // }
    // }

    /**
     * Hook method for restoring widget values to the values that they held last time this wizard was used to
     * completion.
     */
    @Override
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames != null && directoryNames.length > 0) {
                for (String directoryName : directoryNames) {
                    addDestinationItem(directoryName);
                }
            }
            setDefaultDestination();
        }
    }

    @Override
    protected String getProcessType() {
        return "Route";
    }

    // TESB-17856
    public boolean isExportDefaultContext() {
        return onlyExportDefaultContext;
    }

}
