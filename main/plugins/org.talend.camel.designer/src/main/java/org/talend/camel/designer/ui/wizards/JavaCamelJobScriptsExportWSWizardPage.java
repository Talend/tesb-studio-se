// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.osgi.framework.Bundle;
import org.talend.camel.designer.CamelDesignerPlugin;
import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.designer.ui.wizards.actions.JavaCamelJobScriptsExportWSAction;
import org.talend.camel.designer.ui.wizards.actions.JavaCamelJobScriptsExportWithMavenAction;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.PluginChecker;
import org.talend.core.repository.constants.FileConstants;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.ui.wizards.exportjob.ExportTreeViewer;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage;
import org.talend.repository.ui.wizards.exportjob.JobScriptsExportWizardPage;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManagerFactory;

public class JavaCamelJobScriptsExportWSWizardPage extends JobScriptsExportWizardPage {

    private static final String TOGGLE_MVN_ONLINE = "ALWAYS_MAVEN_ONLINE_FOR_MICROSERVICE";

    private static final String M2_OFFLINE = "eclipse.m2.offline";

    private static final String M2E_CORE = "org.eclipse.m2e.core";

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

            @Override
            public void widgetSelected(SelectionEvent e) {
                Widget source = e.widget;
                if (source instanceof Combo) {
                    String destination = ((Combo) source).getText();
                    boolean isMS = destination.equals(EXPORTTYPE_SPRING_BOOT);

                    if (isMS) {
                        addBSButton.setEnabled(false);
                        contextButton.setEnabled(true);
                        exportAsZipButton.setEnabled(true);
                    } else {
                        addBSButton.setEnabled(true);
                        contextButton.setEnabled(false);
                        exportAsZipButton.setEnabled(false);
                    }

                    String destinationValue = getDestinationValue();

                    if (isMS) {
                        if (exportAsZip) {
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
            if (exportAsZip) {
                dialog.setFilterExtensions(new String[] { "*" + FileConstants.ZIP_FILE_SUFFIX, "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                dialog.setFilterExtensions(new String[] { "*.jar", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else {
            dialog.setFilterExtensions(new String[] { "*.kar", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        }
        dialog.setText(""); //$NON-NLS-1$
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
            if (exportAsZip) {
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
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.needJobItem, false);
        exportChoiceMap.put(ExportChoice.needSourceCode, false);

        exportChoiceMap.put(ExportChoice.needMetaInfo, true);
        exportChoiceMap.put(ExportChoice.needContext, true);
        if (addBSButton != null) {
            exportChoiceMap.put(ExportChoice.needMavenScript, addBSButton.getSelection());
        }
        exportChoiceMap.put(ExportChoice.onlyDefautContext, onlyExportDefaultContext);
        exportChoiceMap.put(ExportChoice.needLauncher, exportAsZip);

        return exportChoiceMap;
    }

    private void createOptionsForKar(Composite optionsGroup, Font font) {
        if (!PluginChecker.isPluginLoaded(PluginChecker.EXPORT_ROUTE_PLUGIN_ID)) {
            return;
        }

        addBSButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        addBSButton.setText("Add maven script");
        addBSButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean show = addBSButton.getSelection();
                String destinationValue = getDestinationValue();
                if (destinationValue.endsWith(getOutputSuffix())) {
                    if (show) {
                        destinationValue = destinationValue.substring(0, destinationValue.indexOf(getOutputSuffix()))
                                + OUTPUT_FILE_SUFFIX;
                    }
                } else if (destinationValue.endsWith(OUTPUT_FILE_SUFFIX) && !show) {
                    destinationValue = destinationValue.substring(0, destinationValue.indexOf(OUTPUT_FILE_SUFFIX))
                            + getOutputSuffix();
                }
                setDestinationValue(destinationValue);
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
        if (new File(destinationKar).exists()) {
            boolean yes = MessageDialog.openQuestion(getShell(),
                    Messages.getString("JavaCamelJobScriptsExportWSWizardPage.OverwriteKarTitle"),
                    Messages.getString("JavaCamelJobScriptsExportWSWizardPage.OverwriteKarMessage"));
            if (!yes) {
                return false;
            }
        }

        JavaCamelJobScriptsExportWSAction action = null;
        IRunnableWithProgress actionMS = null;
        Map<ExportChoice, Object> exportChoiceMap = getExportChoiceMap();

        if (exportTypeCombo.getText().equals(EXPORTTYPE_SPRING_BOOT)) {
            Bundle bundle = Platform.getBundle(PluginChecker.EXPORT_ROUTE_PLUGIN_ID);
            try {
                if (bundle != null) {
                    // Get m2e preferences
                    boolean mvnOffline = Platform.getPreferencesService().getBoolean(M2E_CORE, M2_OFFLINE, false, null);

                    if (mvnOffline) {
                        // Change mvn to online ONLY if maven is offline needs run this part
                        IPreferenceStore camelStore = CamelDesignerPlugin.getDefault().getPreferenceStore();
                        // camelStore.setValue(TOGGLE_MAVEN_ONLINE, MessageDialogWithToggle.NEVER); //for bebug
                        if (!MessageDialogWithToggle.ALWAYS.equals(camelStore.getString(TOGGLE_MVN_ONLINE))) {
                            MessageDialogWithToggle dlg = MessageDialogWithToggle.openOkCancelConfirm(getShell(),
                                    Messages.getString("JavaCamelJobScriptsExportWSWizardPage.MavenConfirm"),
                                    Messages.getString("JavaCamelJobScriptsExportWSWizardPage.MavenMesssage"),
                                    Messages.getString("JavaCamelJobScriptsExportWSWizardPage.ShowItAgain"), false, camelStore,
                                    TOGGLE_MVN_ONLINE);

                            if (dlg.getReturnCode() != IDialogConstants.OK_ID)
                                return false;
                        }

                        InstanceScope.INSTANCE.getNode(M2E_CORE).putBoolean(M2_OFFLINE, false);
                    }

                    Class javaCamelJobScriptsExportMicroServiceAction = bundle
                            .loadClass("org.talend.resources.export.maven.action.JavaCamelJobScriptsExportMicroServiceAction");

                    Constructor constructor = javaCamelJobScriptsExportMicroServiceAction.getConstructor(Map.class, List.class,
                            String.class, String.class, String.class);

                    actionMS = (IRunnableWithProgress) constructor.newInstance(exportChoiceMap, Arrays.asList(getCheckNodes()),
                            version, destinationKar, "");

                    try {
                        getContainer().run(false, true, actionMS);
                    } catch (Exception e) {
                        MessageBoxExceptionHandler.process(e.getCause(), getShell());
                        return false;
                    } finally {
                        if (mvnOffline) {
                            // restore maven status
                            InstanceScope.INSTANCE.getNode(M2E_CORE).putBoolean(M2_OFFLINE, mvnOffline);
                        }
                    }
                }

            } catch (Exception e) {
                MessageBoxExceptionHandler.process(e.getCause(), getShell());
                e.printStackTrace();
            }

        } else {

            if (exportChoiceMap.containsKey(ExportChoice.needMavenScript)
                    && exportChoiceMap.get(ExportChoice.needMavenScript) == Boolean.TRUE) {
                action = new JavaCamelJobScriptsExportWithMavenAction(exportChoiceMap, nodes[0], version, destinationKar, false);
            } else {
                action = new JavaCamelJobScriptsExportWSAction(nodes[0], version, destinationKar, false);
            }

            try {
                getContainer().run(false, true, action);
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
