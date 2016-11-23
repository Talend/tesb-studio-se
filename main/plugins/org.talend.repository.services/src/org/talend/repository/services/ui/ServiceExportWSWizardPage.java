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
package org.talend.repository.services.ui;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.talend.core.PluginChecker;
import org.talend.core.repository.constants.FileConstants;
import org.talend.repository.services.Messages;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

public class ServiceExportWSWizardPage extends WizardPage {

    private static final String OUTPUT_FILE_SUFFIX = FileConstants.ZIP_FILE_SUFFIX;

    private final String serviceName;

    private final String serviceVersion;

    private String destinationValue;

    private Text destinationText;

    private Button addBSButton;

    public ServiceExportWSWizardPage(ServiceItem serviceItem) {
        super("ServiceExportWSWizardPage"); //$NON-NLS-1$
        serviceName = serviceItem.getProperty().getLabel();
        serviceVersion = serviceItem.getProperty().getVersion();
    }

    protected void handleDestinationBrowseButtonPressed() {
        String idealSuffix;
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
        if (isAddMavenScript()) {
            idealSuffix = OUTPUT_FILE_SUFFIX;
        } else {
            idealSuffix = getOutputSuffix();
        }
        dialog.setFilterExtensions(new String[] { '*' + idealSuffix, "*.*" }); //$NON-NLS-1$
        File destination = new File(getDestinationValue());
        dialog.setFileName(destination.getName());
        dialog.setFilterPath(destination.getParent());
        String selectedFileName = dialog.open();
        if (selectedFileName == null) {
            return;
        }
        if (!selectedFileName.endsWith(idealSuffix)) {
            selectedFileName += idealSuffix;
        }
        checkDestination(selectedFileName);
        destinationValue = selectedFileName;
    }

    private void checkDestination(String fileName) {
        File destination;
        destination = new File(fileName);
        if (destination.exists()) {
            setMessage(Messages.ServiceExportWizard_WarningMessage_WillBeOverwritten);
        } else {
            setMessage(null);
        }
    }

    public String getDestinationValue() {
        if (null == destinationValue) {
            String bundleName = serviceName + "-" + serviceVersion + getOutputSuffix();
            String storedDir = getDialogSettings().get(getClass().getName());
            if (storedDir == null) {
                storedDir = System.getProperty("user.dir");
            }
            IPath path = new Path(storedDir).append(bundleName);
            destinationValue = path.toOSString();
        }
        return destinationValue;
    }

    protected String getOutputSuffix() {
        return FileConstants.KAR_FILE_SUFFIX;
    }

    @Override
    public void createControl(Composite parent) {
        setControl(parent);
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        container.setLayout(new GridLayout(1, false));

        Group destinationGroup = new Group(container, SWT.NONE);
        destinationGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        destinationGroup.setText("Destination");
        destinationGroup.setLayout(new GridLayout(2, false));

        destinationText = new Text(destinationGroup, SWT.SINGLE | SWT.BORDER);
        destinationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        destinationText.setText(getDestinationValue());
        destinationText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                destinationValue = destinationText.getText();
                checkDestination(destinationValue);
            }
        });
        Button browseButton = new Button(destinationGroup, SWT.PUSH);
        browseButton.setText("Browse");
        browseButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDestinationBrowseButtonPressed();
                destinationText.setText(destinationValue);
            }
        });
        Group optionsGroup = new Group(container, SWT.NONE);
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        optionsGroup.setLayoutData(layoutData);
        GridLayout layout = new GridLayout();
        optionsGroup.setLayout(layout);

        optionsGroup.setText(org.talend.repository.i18n.Messages.getString("IDEWorkbenchMessages.WizardExportPage_options")); //$NON-NLS-1$
        optionsGroup.setFont(parent.getFont());

        createBSGroup(optionsGroup);
    }

    private void createBSGroup(Group optionsGroup) {
        if (!PluginChecker.isPluginLoaded(PluginChecker.EXPORT_ROUTE_PLUGIN_ID)) {
            return;
        }

        Font font = optionsGroup.getFont();
        optionsGroup.setLayout(new GridLayout(2, false));

        addBSButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        addBSButton.setText("Add maven script"); //$NON-NLS-1$
        addBSButton.setFont(font);
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

    }

    public Map<ExportChoice, Object> getExportChoiceMap() {
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.needLauncher, true);
        exportChoiceMap.put(ExportChoice.needSystemRoutine, true);
        exportChoiceMap.put(ExportChoice.needUserRoutine, true);
        exportChoiceMap.put(ExportChoice.needTalendLibraries, true);
        exportChoiceMap.put(ExportChoice.needJobItem, true);
        exportChoiceMap.put(ExportChoice.needJobScript, true);
        exportChoiceMap.put(ExportChoice.needContext, true);
        exportChoiceMap.put(ExportChoice.needSourceCode, true);
        exportChoiceMap.put(ExportChoice.applyToChildren, false);
        exportChoiceMap.put(ExportChoice.doNotCompileCode, false);
        if (addBSButton != null) {
            exportChoiceMap.put(ExportChoice.needMavenScript, addBSButton.getSelection());
        }

        return exportChoiceMap;
    }

    public boolean isAddMavenScript() {
        if (addBSButton != null) {
            return addBSButton.getSelection();
        }
        return false;
    }

    private void setDestinationValue(String destinationValue) {
        destinationText.setText(destinationValue.trim());
    }

    public void finish() {
        String destination = destinationText.getText().trim();
        if (!"".equals(destination)) {
            IPath path = Path.fromOSString(destination);
            getDialogSettings().put(getClass().getName(), path.removeLastSegments(1).toOSString());
        }
    }

}
