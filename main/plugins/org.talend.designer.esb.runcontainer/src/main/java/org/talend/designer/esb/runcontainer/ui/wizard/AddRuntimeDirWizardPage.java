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
package org.talend.designer.esb.runcontainer.ui.wizard;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.talend.designer.esb.runcontainer.i18n.RunContainerMessages;
import org.talend.designer.esb.runcontainer.util.FileUtil;

public class AddRuntimeDirWizardPage extends WizardPage {

    private Text txtRuntimeHome;

    private String runtimeHome;

    private Text txtRuntimeArchive;

    private String runtimeArchive;

    private Text txtTargetDir;

    private String targetDir;

    private List<String> rtFiles = new ArrayList<String>();

    private Button btnUseExistingContainer;

    private Button btnInstallNewContainer;

    private Button btnTargetDefault;

    private Button btnTargetCustom;

    private List<Control> ctlsRuntimeHome;

    private List<Control> ctlsRuntimeArchive;

    private Composite compExistingContainer;

    private Composite compNewContainer;

    private Composite compTarget;

    private Button btnTargetDir;

    /**
     * Create the wizard.
     */
    public AddRuntimeDirWizardPage() {
        super(RunContainerMessages.getString("AddRuntimeDirWizardPage.Title")); //$NON-NLS-1$
        setTitle(RunContainerMessages.getString("AddRuntimeDirWizardPage.Title")); //$NON-NLS-1$
        setDescription(RunContainerMessages.getString("AddRuntimeDirWizardPage.Desc")); //$NON-NLS-1$
        rtFiles.add("/bin/trun"); //$NON-NLS-1$
        rtFiles.add("/bin/setenv"); //$NON-NLS-1$
        rtFiles.add("/etc"); //$NON-NLS-1$
        rtFiles.add("/system/org/apache/karaf"); //$NON-NLS-1$
        rtFiles.add("/system/org/talend/esb"); //$NON-NLS-1$
        rtFiles.add("/lib/boot"); //$NON-NLS-1$
        // rtFiles.add("/version.txt");
    }

    /**
     * Create contents of the wizard.
     * 
     * @param parent
     */
    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);

        setControl(container);
        container.setLayout(new GridLayout(1, false));

        Composite body = new Composite(container, SWT.NONE);
        body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        body.setLayout(new GridLayout(1, false));

        ctlsRuntimeHome = new ArrayList<>();
        ctlsRuntimeArchive = new ArrayList<>();

        // Runtime Home Location

        {
            // Composite compCheck = new Composite(body, SWT.NONE);
            // compCheck.setLayout(new GridLayout(1, false));

            btnUseExistingContainer = new Button(body, SWT.RADIO);
            btnUseExistingContainer.setText(RunContainerMessages.getString("AddRuntimeDirWizardPage.UseExisting"));
            btnUseExistingContainer.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    validateRuntimeHome();
                    updateWidgets();
                }
            });
            btnUseExistingContainer.setSelection(false);

            compExistingContainer = new Composite(body, SWT.NONE);
            compExistingContainer.setLayout(new GridLayout(3, false));
            compExistingContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            compExistingContainer.setBounds(0, 0, 64, 64);

            Label lblHome = new Label(compExistingContainer, SWT.NONE);
            GridData gd_lblHome = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
            gd_lblHome.widthHint = 116;
            lblHome.setLayoutData(gd_lblHome);
            lblHome.setText(RunContainerMessages.getString("AddRuntimeDirWizardPage.Home")); //$NON-NLS-1$

            txtRuntimeHome = new Text(compExistingContainer, SWT.BORDER);
            txtRuntimeHome.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            txtRuntimeHome.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    // Handle event
                    btnUseExistingContainer.setSelection(true);
                    validateRuntimeHome();
                }
            });
            Button btnNewButton = new Button(compExistingContainer, SWT.NONE);
            btnNewButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {

                    DirectoryDialog fileDialog = new DirectoryDialog(getShell(), SWT.OPEN | SWT.SHEET);
                    String rtHome = fileDialog.open();
                    if (rtHome != null) {
                        txtRuntimeHome.setText(rtHome);
                        btnUseExistingContainer.setSelection(true);
                    }
                }
            });
            btnNewButton.setText(RunContainerMessages.getString("AddRuntimeDirWizardPage.DirButton"));

            ctlsRuntimeHome.add(txtRuntimeHome);
            ctlsRuntimeHome.add(btnNewButton);
        }

        Label separator = new Label(body, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Runtime Distribution

        {
            // Composite compCheck = new Composite(body, SWT.NONE);
            // compCheck.setLayout(new GridLayout(1, false));

            btnInstallNewContainer = new Button(body, SWT.RADIO);
            btnInstallNewContainer.setText(RunContainerMessages.getString("AddRuntimeDirWizardPage.InstallNew"));
            btnInstallNewContainer.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    validateRuntimeArchive();
                    updateWidgets();
                }
            });
            btnInstallNewContainer.setSelection(true);

            compNewContainer = new Composite(body, SWT.NONE);
            compNewContainer.setLayout(new GridLayout(3, false));
            compNewContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            compNewContainer.setBounds(0, 0, 64, 64);

            Label lblHome = new Label(compNewContainer, SWT.NONE);
            GridData gd_lblHome = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
            gd_lblHome.widthHint = 116;
            lblHome.setLayoutData(gd_lblHome);
            lblHome.setText(RunContainerMessages.getString("AddRuntimeDirWizardPage.Install")); //$NON-NLS-1$

            txtRuntimeArchive = new Text(compNewContainer, SWT.BORDER);
            txtRuntimeArchive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            txtRuntimeArchive.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    // Handle event
                    btnInstallNewContainer.setSelection(true);
                    validateRuntimeArchive();
                }
            });
            Button btnNewButton = new Button(compNewContainer, SWT.NONE);
            btnNewButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN | SWT.SHEET);
                    fileDialog.setFilterExtensions(new String[] { "*.zip" });
                    String rtHome = fileDialog.open();
                    if (rtHome != null) {
                        txtRuntimeArchive.setText(rtHome);
                        btnInstallNewContainer.setSelection(true);
                    }
                }
            });
            btnNewButton.setText(RunContainerMessages.getString("AddRuntimeDirWizardPage.FileButton"));

            compTarget = new Composite(body, SWT.NONE);
            compTarget.setLayout(new GridLayout(3, false));
            compTarget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            compTarget.setBounds(0, 0, 64, 64);

            Label lblTarget = new Label(compTarget, SWT.NONE);
            lblTarget.setText("Install into:");
            GridData lblTargetGridData = new GridData();
            lblTargetGridData.horizontalSpan = 3;
            lblTarget.setLayoutData(lblTargetGridData);

            btnTargetDefault = new Button(compTarget, SWT.RADIO);
            btnTargetDefault.setText("default target folder");
            btnTargetDefault.setSelection(true);
            GridData btnTargetDefaultGridData = new GridData();
            btnTargetDefaultGridData.horizontalSpan = 0;
            btnTargetDefaultGridData.horizontalIndent = 20;
            btnTargetDefault.setLayoutData(btnTargetDefaultGridData);

            Link link = new Link(compTarget, SWT.NONE);
            link.setText("<a href=\"#\">STUDIO_HOME/esb/container</a>");
            link.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    Desktop desktop = Desktop.getDesktop();
                    String defaultLocal = System.getProperty("user.dir") + File.separator + "esb";
                    try {
                        desktop.open(new File(defaultLocal));
                    } catch (IOException e1) {
                    }
                }
            });

            Label lblBlank = new Label(compTarget, SWT.NONE);
            lblBlank.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

            btnTargetCustom = new Button(compTarget, SWT.RADIO);
            btnTargetCustom.setText("custom target folder");
            btnTargetCustom.setSelection(false);
            btnTargetCustom.setEnabled(true);
            GridData btnTargetCustomGridData = new GridData();
            btnTargetCustomGridData.horizontalIndent = 20;
            btnTargetCustom.setLayoutData(btnTargetCustomGridData);

            txtTargetDir = new Text(compTarget, SWT.BORDER);
            txtTargetDir.setEnabled(false);
            txtTargetDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            txtTargetDir.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    btnInstallNewContainer.setSelection(true);
                    btnTargetCustom.setSelection(true);
                    validateRuntimeArchive();
                }
            });

            btnTargetDir = new Button(compTarget, SWT.NONE);
            btnTargetDir.setEnabled(false);
            btnTargetDir.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    DirectoryDialog fileDialog = new DirectoryDialog(getShell(), SWT.OPEN | SWT.SHEET);
                    String rtTarget = fileDialog.open();
                    if (rtTarget != null) {
                        txtTargetDir.setText(rtTarget);
                    }
                }
            });
            btnTargetDir.setText(RunContainerMessages.getString("AddRuntimeDirWizardPage.DirButton"));

            btnTargetDefault.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    btnTargetDir.setEnabled(false);
                    txtTargetDir.setEnabled(false);
                    validateRuntimeArchive();
                }
            });

            btnTargetCustom.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    btnTargetDir.setEnabled(true);
                    txtTargetDir.setEnabled(true);
                    validateRuntimeArchive();
                }
            });

            ctlsRuntimeArchive.add(compTarget);
        }
        updateWidgets();
    }

    /*
     * private long getFolderSize(File folder) { long length = 0; File[] files = folder.listFiles();
     * 
     * int count = files.length;
     * 
     * for (int i = 0; i < count; i++) { if (files[i].isFile()) { length += files[i].length(); } else { length +=
     * getFolderSize(files[i]); } } return length; }
     */

    private void validateRuntimeArchive() {
        String arch = txtRuntimeArchive.getText();
        if (arch == null || arch.isEmpty()) {
            setPageComplete(false);
            setErrorMessage(null);
            return;
        }

        boolean isValid = arch.equals(runtimeArchive) ? true : FileUtil.isContainerArchive(arch);
        setPageComplete(isValid);

        if (isValid) {
            runtimeArchive = arch;
            setErrorMessage(null);

            validateTargetDir();
        } else {
            setErrorMessage(RunContainerMessages.getString("AddRuntimeDirWizardPage.ErrorZip"));
        }
    }

    private void validateRuntimeHome() {
        //labelVersion.setText(""); //$NON-NLS-1$
        //labelSize.setText(""); //$NON-NLS-1$
        // labelVersion.getParent().layout();
        String rtHome = txtRuntimeHome.getText();

        if (rtHome == null || rtHome.isEmpty()) {
            setPageComplete(false);
            setErrorMessage(null);
            return;
        }
        // validate, 1st version, 2nd etc
        boolean validated = true;
        String errorMsg = RunContainerMessages.getString("AddRuntimeDirWizardPage.ErrorFind"); //$NON-NLS-1$

        rtHome = rtHome.trim();
        if (rtHome.length() > 0) {
            File rtDir = new File(rtHome);
            // find version.txt, 1st in root, second check in container folder
            if (rtDir.isDirectory()) {
                File version = new File(rtHome + "/version.txt"); //$NON-NLS-1$
                String ver = ""; //$NON-NLS-1$
                try {
                    if (version.exists()) {
                        ver = Files.readAllLines(version.toPath()).get(0);
                    } else {
                        version = new File(rtHome + "/container/version.txt"); //$NON-NLS-1$
                        if (version.exists()) {
                            ver = Files.readAllLines(version.toPath()).get(0);
                            rtHome += "/container/"; //$NON-NLS-1$
                        }
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                if (ver.isEmpty()) {
                    validated = false;
                } else {
                    // labelVersion.setText(ver);
                    // long size = getFolderSize(new File(rtHome)) / 1024 / 1024;
                    //labelSize.setText(size + " MB"); //$NON-NLS-1$
                    // labelVersion.getParent().layout();

                    for (String f : rtFiles) {
                        File resFile = new File(rtHome + f);
                        if (!resFile.exists()) {
                            validated = false;
                            errorMsg = RunContainerMessages.getString("AddRuntimeDirWizardPage.ErrorDir"); //$NON-NLS-1$
                            break;
                        }
                    }
                }
            } else {
                validated = false;
            }
        } else {
            validated = false;
        }

        if (validated) {
            setErrorMessage(null);
            setPageComplete(true);
            runtimeHome = txtRuntimeHome.getText();
        } else {
            setErrorMessage(errorMsg);
            setPageComplete(false);
        }
    }

    private void validateTargetDir() {
        if (btnTargetDefault.getSelection()) {
            targetDir = null;
            return;
        }

        String target = txtTargetDir.getText();
        if (target == null || target.isEmpty()) {
            setPageComplete(false);
            setErrorMessage(null);
            return;
        }

        target = target.trim();
        File targetFile = new File(target);

        if (!targetFile.isDirectory()) {
            setPageComplete(false);
            setErrorMessage("Target doesn't point to a valid folder");
            return;
        }

        if (targetFile.list().length > 0) {
            setPageComplete(false);
            setErrorMessage("Target folder is not empty");
            return;
        }

        targetDir = target;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
     */
    @Override
    public boolean canFlipToNextPage() {
        // TODO Auto-generated method stub
        return true;
    }

    public String getRuntimeHome() {
        return runtimeHome != null ? FileUtil.getValidLocation(runtimeHome) : null;
    }

    public String getRuntimeArchive() {
        return runtimeArchive;
    }

    public String getTargetDir() {
        return targetDir;
    }

    public boolean shouldInstallNewContainer() {
        return btnInstallNewContainer.getSelection();
    }

    private static void changeControls(List<Control> ctls, boolean enable) {
        // for (Control ctl : ctls) {
        // ctl.setEnabled(enable);
        // }
    }

    private void updateWidgets() {

        for (Control ctrl : compExistingContainer.getChildren()) {
            ctrl.setEnabled(btnUseExistingContainer.getSelection());
        }
        for (Control ctrl : compNewContainer.getChildren()) {
            ctrl.setEnabled(btnInstallNewContainer.getSelection());
        }
        for (Control ctrl : compTarget.getChildren()) {
            ctrl.setEnabled(btnInstallNewContainer.getSelection());
        }

        if (btnTargetDefault.getSelection()) {
            btnTargetDir.setEnabled(false);
            txtTargetDir.setEnabled(false);
        }
    }
}
