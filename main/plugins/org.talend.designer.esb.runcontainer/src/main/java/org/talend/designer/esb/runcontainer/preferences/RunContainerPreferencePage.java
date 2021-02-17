// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.esb.runcontainer.preferences;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.designer.esb.runcontainer.core.ESBRunContainerPlugin;
import org.talend.designer.esb.runcontainer.i18n.RunContainerMessages;
import org.talend.designer.esb.runcontainer.server.RuntimeServerController;
import org.talend.designer.esb.runcontainer.ui.dialog.InitFinishMessageDialog;
import org.talend.designer.esb.runcontainer.ui.dialog.RuntimeErrorDialog;
import org.talend.designer.esb.runcontainer.ui.progress.RuntimeClientProgress;
import org.talend.designer.esb.runcontainer.ui.progress.StartRuntimeProgress;
import org.talend.designer.esb.runcontainer.ui.progress.StopRuntimeProgress;
import org.talend.designer.esb.runcontainer.ui.wizard.AddRuntimeWizard;
import org.talend.designer.esb.runcontainer.util.FileUtil;
import org.talend.designer.esb.runcontainer.util.JMXUtil;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ui.ProcessManager;

/**
 * ESB Runtime pref page
 *
 */
public class RunContainerPreferencePage extends FieldLayoutPreferencePage implements IWorkbenchPreferencePage {

    public static final String ID = "org.talend.designer.esb.runcontainer.preferences.ESBRunContainerPreferencePage"; //$NON-NLS-1$

    private Composite compositeServerBody;

    private Composite compositeOptionBody;

    private List<FieldEditor> serverFieldEditors;

    private List<FieldEditor> optionFieldEditors;

    private Button buttonAddServer;

    private Button buttonInitalizeServer;

    private BooleanFieldEditor useOSGiEditor;

    private boolean runtimeEnable;

    private ProcessManager manager;

    /**
     * Create the preference page.
     */
    public RunContainerPreferencePage() {
        setTitle(RunContainerMessages.getString("RunContainerPreferencePage.Title")); //$NON-NLS-1$
    }

    /**
     * Create contents of the preference page.
     *
     * @param parent
     */
    @Override
    public Control createPageContents(Composite parent) {
        serverFieldEditors = new ArrayList<FieldEditor>();
        optionFieldEditors = new ArrayList<FieldEditor>();
        runtimeEnable = getPreferenceStore().getBoolean(RunContainerPreferenceInitializer.P_ESB_IN_OSGI);
        GridLayout gridLayoutDefault = new GridLayout(1, false);

        Composite body = new Composite(parent, SWT.NONE);
        body.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        body.setLayout(gridLayoutDefault);
        getPreferenceStore().getBoolean(RunContainerPreferenceInitializer.P_ESB_IN_OSGI);
        useOSGiEditor = new BooleanFieldEditor(RunContainerPreferenceInitializer.P_ESB_IN_OSGI,
                "ESB Studio Runtime - Use Local Talend Runtime (OSGi Container)", body);
        addField(useOSGiEditor);
        Label lblNote = new Label(body, SWT.WRAP);
        lblNote.setText("Note: It will be only taken into account for an ESB Artifact:\n" + "\t- A Route (Any Route)\n"
                + "\t- A DataService (SOAP/REST)\n" + "\t- A Job containing tRESTClient or tESBConsumer component");

        Group groupServer = new Group(body, SWT.NONE);
        groupServer.setText(RunContainerMessages.getString("RunContainerPreferencePage.Group1")); //$NON-NLS-1$
        groupServer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        groupServer.setLayout(new GridLayout(2, false));

        compositeServerBody = new Composite(groupServer, SWT.NONE);
        compositeServerBody.setLayout(gridLayoutDefault);
        compositeServerBody.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        StringFieldEditor locationEditor = new StringFieldEditor(RunContainerPreferenceInitializer.P_ESB_RUNTIME_LOCATION,
                RunContainerMessages.getString("RunContainerPreferencePage.Location"), compositeServerBody); //$NON-NLS-1$
        addField(locationEditor);
        serverFieldEditors.add(locationEditor);

        StringFieldEditor hostFieldEditor = new StringFieldEditor(RunContainerPreferenceInitializer.P_ESB_RUNTIME_HOST,
                RunContainerMessages.getString("RunContainerPreferencePage.Host"), compositeServerBody);
        addField(hostFieldEditor);
        // only support local runtime server, if need support remote server ,enable this editor
        hostFieldEditor.setEnabled(false, compositeServerBody);

        StringFieldEditor userFieldEditor = new StringFieldEditor(RunContainerPreferenceInitializer.P_ESB_RUNTIME_USERNAME,
                RunContainerMessages.getString("RunContainerPreferencePage.Username"), compositeServerBody); //$NON-NLS-1$
        addField(userFieldEditor);
        serverFieldEditors.add(userFieldEditor);
        StringFieldEditor passwordFieldEditor = new StringFieldEditor(RunContainerPreferenceInitializer.P_ESB_RUNTIME_PASSWORD,
                RunContainerMessages.getString("RunContainerPreferencePage.Password"), compositeServerBody); //$NON-NLS-1$
        addField(passwordFieldEditor);
        serverFieldEditors.add(passwordFieldEditor);
        StringFieldEditor instanceFieldEditor = new StringFieldEditor(RunContainerPreferenceInitializer.P_ESB_RUNTIME_INSTANCE,
                RunContainerMessages.getString("RunContainerPreferencePage.Instance"), compositeServerBody); //$NON-NLS-1$
        addField(instanceFieldEditor);
        serverFieldEditors.add(instanceFieldEditor);

        IntegerFieldEditor portFieldEditor = new IntegerFieldEditor(RunContainerPreferenceInitializer.P_ESB_RUNTIME_PORT,
                RunContainerMessages.getString("RunContainerPreferencePage.Port"), compositeServerBody); //$NON-NLS-1$
        addField(portFieldEditor);
        serverFieldEditors.add(portFieldEditor);

        StringFieldEditor jmxPortFieldEditor = new StringFieldEditor(RunContainerPreferenceInitializer.P_ESB_RUNTIME_JMX_PORT,
                RunContainerMessages.getString("RunContainerPreferencePage.JMXPort"), compositeServerBody); //$NON-NLS-1$
        addField(jmxPortFieldEditor);
        serverFieldEditors.add(jmxPortFieldEditor);

        Composite compBtn = new Composite(groupServer, SWT.NONE);
        GridData gridDataBtn = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
        gridDataBtn.widthHint = 100;
        compBtn.setLayoutData(gridDataBtn);
        GridLayout layoutCompBtn = new GridLayout(1, false);
        layoutCompBtn.marginWidth = 0;
        layoutCompBtn.marginHeight = 0;

        compBtn.setLayout(layoutCompBtn);
        buttonAddServer = new Button(compBtn, SWT.NONE);
        buttonAddServer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttonAddServer.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                AddRuntimeWizard dirWizard = new AddRuntimeWizard(locationEditor.getStringValue());
                dirWizard.setNeedsProgressMonitor(true);
                WizardDialog wizardDialog = new WizardDialog(getShell(), dirWizard);
                if (wizardDialog.open() == Window.OK) {
                    locationEditor.setStringValue(dirWizard.getTarget());
                }
            }
        });
        buttonAddServer.setText(RunContainerMessages.getString("RunContainerPreferencePage.ServerButton")); //$NON-NLS-1$

        // Button btnTestConnection = new Button(compBtn, SWT.NONE);
        // btnTestConnection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        // btnTestConnection.setText("Server Info...");

        buttonInitalizeServer = new Button(compBtn, SWT.NONE);
        buttonInitalizeServer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttonInitalizeServer.setText(RunContainerMessages.getString("RunContainerPreferencePage.InitalizeButton")); //$NON-NLS-1$
        buttonInitalizeServer.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (initalizeRuntime(locationEditor.getStringValue(), hostFieldEditor.getStringValue())) {
                    try {
                        new InitFinishMessageDialog(getShell(), JMXUtil.getBundlesName()).open();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        Group groupOption = new Group(body, SWT.NONE);
        groupOption.setLayout(gridLayoutDefault);
        groupOption.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        groupOption.setText(RunContainerMessages.getString("RunContainerPreferencePage.Group2")); //$NON-NLS-1$
        
        Group deprecated = new Group(body, SWT.NONE);
        deprecated.setLayout(gridLayoutDefault);
        deprecated.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label deprecationNote = new Label(deprecated, SWT.WRAP);
        deprecationNote.setText(RunContainerMessages.getString("RunContainerPreferencePage.Deprecation"));
        
        compositeOptionBody = new Composite(groupOption, SWT.NONE);
        compositeOptionBody.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        BooleanFieldEditor filterLogEditor = new BooleanFieldEditor(RunContainerPreferenceInitializer.P_ESB_RUNTIME_SYS_LOG,
                RunContainerMessages.getString("RunContainerPreferencePage.FilterLogs"), compositeOptionBody); //$NON-NLS-1$
        addField(filterLogEditor);
        optionFieldEditors.add(filterLogEditor);

        manager = ProcessManager.getInstance();
        return body;
    }

    /**
     * Initialize the preference page.
     */
    @Override
    public void init(IWorkbench workbench) {
        // Initialize the preference page
        setPreferenceStore(ESBRunContainerPlugin.getDefault().getPreferenceStore());
    }

    private boolean initalizeRuntime(String location, String host) {
        boolean finished = true;
        performApply();

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
        try {
            dialog.run(true, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    SubMonitor totalMonitor = SubMonitor.convert(monitor, 10);
                    totalMonitor.beginTask("Initializing Runtime server", 10);
                    // 1. try to stop first
                    totalMonitor.setTaskName("Stoping Runtime server");
                    new StopRuntimeProgress().run(totalMonitor);
                    if (RuntimeServerController.getInstance().getRuntimeProcess() != null
                            && RuntimeServerController.getInstance().getRuntimeProcess().isAlive()) {
                        RuntimeServerController.getInstance().getRuntimeProcess().waitFor(20, TimeUnit.SECONDS);
                    }
                    totalMonitor.worked(2);

                    // 2. delete data(cannot use JMX to rebootCleanAll as a DLL delete failed)
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("Initalize is canceled by user");
                    }
                    totalMonitor.setTaskName("Deleting /data folder");
                    try {
                        FileUtil.deleteFolder(location + "/data");
                    } catch (IOException e) {
                        ExceptionHandler.process(e);
                        throw new InterruptedException(e.getMessage());
                    }
                    if (new File(location + "/data").exists()) {
                        throw new InterruptedException(RunContainerMessages
                                .getString("RunContainerPreferencePage.InitailzeDialog7"));
                    }
                    totalMonitor.worked(1);

                    // 3. start (again)
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("Initalize is canceled by user");
                    }

                    totalMonitor.setTaskName("Starting Runtime server");
                    new StartRuntimeProgress(false).run(totalMonitor);
                    totalMonitor.worked(2);

                    // 4. command
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("Initalize is canceled by user");
                    }
                    File launcher;
                    String os = System.getProperty("os.name");
                    if (os != null && os.toLowerCase().contains("windows")) {
                        launcher = new File(location + "/bin/client.bat");
                    } else {
                        launcher = new File(location + "/bin/client");
                    }
                    InputStream stream = RunContainerPreferencePage.class.getResourceAsStream("/resources/commands");
                    File initFile = new File(location + "/scripts/initlocal.sh");
                    if (!initFile.exists()) {
                        try {
                            Files.copy(stream, initFile.toPath());
                        } catch (IOException e) {
                            ExceptionHandler.process(e);
                            throw new InterruptedException(e.getMessage());
                        }
                    }
                    // without username and password is ok
                    // fixed by KARAF-5019
                    String command = launcher.getAbsolutePath() + " -h " + host + " -l 1 source file:scripts/initlocal.sh";
                    RuntimeClientProgress clientProgress = new RuntimeClientProgress(command);
                    clientProgress.run(totalMonitor);
                    totalMonitor.done();
                }
            });
        } catch (Exception e) {
            finished = false;
            ExceptionHandler.process(e);
            IStatus status = new Status(IStatus.ERROR, ESBRunContainerPlugin.PLUGIN_ID, e.getMessage(), e);
            if (e.getCause() != null) {
                status = new Status(IStatus.ERROR, ESBRunContainerPlugin.PLUGIN_ID, e.getCause().getMessage(), e.getCause());
            }
            RuntimeErrorDialog.openError(getShell(),
                    RunContainerMessages.getString("RunContainerPreferencePage.InitailzeDialog2"),
                    RunContainerMessages.getString("RunContainerPreferencePage.InitailzeDialog4"), status);
        }

        return finished;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.esb.runcontainer.preferences.FieldLayoutPreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        boolean performOk = super.performOk();
        if (runtimeEnable != getPreferenceStore().getBoolean(RunContainerPreferenceInitializer.P_ESB_IN_OSGI)) {
            IRunProcessService service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                    IRunProcessService.class);
            if (service != null) {
                service.refreshView();
            }

            List<IEditorReference> editorRefs = new ArrayList();
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorReference[] reference = page.getEditorReferences();
            for (IEditorReference editorRef : reference) {
                IEditorPart part = editorRef.getEditor(false);
                if (part.getEditorInput() instanceof JobEditorInput) {
                    editorRefs.add(editorRef);
                }
            }
            if (editorRefs.size() > 0
                    && MessageDialog.openConfirm(getShell(), "Running Container Changed",
                            "All editors need to be closed to apply the changes, do you want to close all opening editors now?")) {
                page.closeEditors(editorRefs.toArray(new IEditorReference[editorRefs.size()]), true);
            }
        }
        return performOk;
    }
}