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
package org.talend.camel.designer.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.designer.ui.wizards.actions.JavaCamelJobScriptsExportWSAction;
import org.talend.camel.designer.ui.wizards.actions.JavaCamelJobScriptsExportWithMavenAction;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.PluginChecker;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.repository.build.IBuildResourceParametes;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.wizards.exportjob.ExportTreeViewer;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWizardPage;
import org.talend.repository.ui.wizards.exportjob.JobScriptsExportWizardPage;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.BuildJobFactory;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManagerFactory;
import org.talend.repository.ui.wizards.exportjob.util.ExportJobUtil;

/**
 * DOC x class global comment. Detailled comment <br/>
 *
 */
public class JavaCamelJobScriptsExportWSWizardPage extends JobScriptsExportWizardPage {

    private static final String EXPORTTYPE_KAR = Messages.getString("JavaCamelJobScriptsExportWSWizardPage.ExportKar");//$NON-NLS-1$

    private static final String EXPORTTYPE_SPRING_BOOT = Messages
            .getString("JavaCamelJobScriptsExportWSWizardPage.ExportSpringBoot");//$NON-NLS-1$

    private static final String EXPORTTYPE_SPRING_BOOT_DOCKER_IMAGE = Messages
            .getString("JavaCamelJobScriptsExportWSWizardPage.ExportSpringBootDockerImage");//$NON-NLS-1$

    // dialog store id constants
    private static final String STORE_DESTINATION_NAMES_ID = "JavaJobScriptsExportWizardPage.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$

    private boolean onlyExportDefaultContext;

    private boolean exportAsZip;

    private boolean addMavenScript;

    protected Button exportAsZipButton;

    protected Combo exportTypeCombo;

    protected Composite pageComposite;

    protected Composite optionsGroupComposite;

    protected Composite optionsDockerGroupComposite;

    protected GridData optionsGroupCompositeLayout;

    protected Composite destinationNameFieldComposite;

    protected Composite destinationNameFieldInnerComposite;

    private Label destinationLabel;

    private Combo destinationNameField;

    private Button destinationBrowseButton;

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
        optionsGroup.setFont(parent.getFont());

        optionsGroup.setLayout(new GridLayout(2, false));

        Label label = new Label(optionsGroup, SWT.NONE);
        label.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.BuildLabel")); //$NON-NLS-1$

        boolean canESBMicroServiceDockerImage = PluginChecker.isDockerPluginLoaded();

        exportTypeCombo = new Combo(optionsGroup, SWT.PUSH);

        // TESB-5328
        exportTypeCombo.add(EXPORTTYPE_KAR);
        if (PluginChecker.isTIS()) {
            exportTypeCombo.add(EXPORTTYPE_SPRING_BOOT);

            if (canESBMicroServiceDockerImage) {
                exportTypeCombo.add(EXPORTTYPE_SPRING_BOOT_DOCKER_IMAGE);
            }
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
                    boolean isMS = destination.equals(EXPORTTYPE_SPRING_BOOT)
                            || destination.equals(EXPORTTYPE_SPRING_BOOT_DOCKER_IMAGE);

                    if (isMS) {
                        contextButton.dispose();
                        addBSButton.dispose();
                        exportAsZipButton.dispose();

                        optionsDockerGroupComposite.dispose();

                        if (destination.equals(EXPORTTYPE_SPRING_BOOT)) {
                            updateDestinationGroup(false);
                            createOptionsForKar(optionsGroupComposite, optionsGroupFont);
                            optionsGroupComposite.layout(true, true);
                        } else {
                            updateDestinationGroup(true);
                            createOptionsForDocker(optionsGroupComposite, optionsGroupFont);
                            createDockerOptions(parent);
                            restoreWidgetValuesForImage();
                            optionsGroupComposite.layout(true, true);
                        }

                        parent.layout();

                    } else {
                        if(contextButton != null) contextButton.setEnabled(false);
                        if(exportAsZipButton != null) exportAsZipButton.setEnabled(false);
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

    protected void updateDestinationGroup(boolean isImage) {
        destinationLabel.setEnabled(!isImage);
        destinationBrowseButton.setEnabled(!isImage);
        destinationNameField.setEnabled(!isImage);
    }

    private Button localRadio, remoteRadio;

    private Text hostText, imageText, tagText;

    private Label hostLabel, imageLabel, tagLabel;

    private Font optionsGroupFont;

    private void createDockerOptions(Composite parent) {

        optionsDockerGroupComposite = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(optionsDockerGroupComposite);

        GridLayout gdlOptionsGroupComposite = new GridLayout();
        gdlOptionsGroupComposite.marginHeight = 0;
        gdlOptionsGroupComposite.marginWidth = 0;
        optionsDockerGroupComposite.setLayout(gdlOptionsGroupComposite);

        Group optionsGroup = new Group(optionsDockerGroupComposite, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(optionsGroup);

        optionsGroup
                .setText(org.talend.repository.i18n.Messages.getString("JavaJobScriptsExportWSWizardPage.DOCKER.optionGroup")); //$NON-NLS-1$
        optionsGroup.setFont(optionsDockerGroupComposite.getFont());
        optionsGroup.setLayout(new GridLayout());

        Composite dockeOptionsComposite = new Composite(optionsGroup, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(dockeOptionsComposite);
        GridLayout dockerOptionsLayout = new GridLayout(3, false);
        dockerOptionsLayout.marginHeight = 0;
        dockerOptionsLayout.marginWidth = 0;
        dockeOptionsComposite.setLayout(dockerOptionsLayout);

        hostLabel = new Label(dockeOptionsComposite, SWT.NONE);
        hostLabel.setText(org.talend.repository.i18n.Messages.getString("JavaJobScriptsExportWSWizardPage.DOCKER.dockerHost")); //$NON-NLS-1$
        Composite hostComposite = new Composite(dockeOptionsComposite, SWT.NONE);
        hostComposite.setLayout(new GridLayout(2, false));

        localRadio = new Button(hostComposite, SWT.RADIO);
        localRadio.setText(org.talend.repository.i18n.Messages.getString("JavaJobScriptsExportWSWizardPage.DOCKER.localHost")); //$NON-NLS-1$
        remoteRadio = new Button(hostComposite, SWT.RADIO);
        remoteRadio.setText(org.talend.repository.i18n.Messages.getString("JavaJobScriptsExportWSWizardPage.DOCKER.remoteHost")); //$NON-NLS-1$
        hostText = new Text(dockeOptionsComposite, SWT.BORDER);
        GridData hostData = new GridData(GridData.FILL_HORIZONTAL);
        hostText.setLayoutData(hostData);

        imageLabel = new Label(dockeOptionsComposite, SWT.NONE);
        imageLabel.setText(org.talend.repository.i18n.Messages.getString("JavaJobScriptsExportWSWizardPage.DOCKER.imageLabel")); //$NON-NLS-1$
        imageText = new Text(dockeOptionsComposite, SWT.BORDER);
        // imageText.setText("${talend.project.name.lowercase}/${talend.job.folder}%a"); //$NON-NLS-1$
        GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(imageText);

        tagLabel = new Label(dockeOptionsComposite, SWT.NONE);
        tagLabel.setText(org.talend.repository.i18n.Messages.getString("JavaJobScriptsExportWSWizardPage.DOCKER.tagLabel")); //$NON-NLS-1$
        tagText = new Text(dockeOptionsComposite, SWT.BORDER);
        // tagText.setText("${talend.job.version}"); //$NON-NLS-1$
        GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(tagText);

        updateOptionBySelection();

        // Label additionalLabel = new Label(dockeOptionsComposite, SWT.NONE);
        // additionalLabel.setText("Additional properties");
        // Text additionalText = new Text(dockeOptionsComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        // GridData data = new GridData(GridData.FILL_HORIZONTAL);
        // data.heightHint = 60;
        // additionalText.setLayoutData(data);

        addDockerOptionsListener();

    }

    private String getDefaultImageName(ProcessItem procesItem) {
        IFile pomFile = null;

        if ("ROUTE_MICROSERVICE".equals(
                getProcessItem().getProperty().getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE))) {
            pomFile = AggregatorPomsHelper.getItemPomFolder(procesItem.getProperty()).getFile("pom-microservice.xml");
        } else {
            pomFile = AggregatorPomsHelper.getItemPomFolder(procesItem.getProperty()).getFile("pom-bundle.xml");
        }

        String projectName = PomUtil.getPomProperty(pomFile, "talend.project.name.lowercase"); //$NON-NLS-1$
        String jobFolderPath = PomUtil.getPomProperty(pomFile, "talend.job.folder"); //$NON-NLS-1$
        String jobName = PomUtil.getPomProperty(pomFile, "talend.job.name").toLowerCase(); //$NON-NLS-1$
        return projectName + "/" + jobFolderPath + jobName; //$NON-NLS-1$
    }

    private String getDefaultImageNamePattern() {
        return "${talend.project.name.lowercase}/${talend.job.folder}%a"; //$NON-NLS-1$
    }

    private String getDefaultImageTag(ProcessItem procesItem) {
        return PomIdsHelper.getJobVersion(procesItem.getProperty());
    }

    private String getDefaultImageTagPattern() {
        return "${talend.docker.tag}"; //$NON-NLS-1$
    }

    @Override
    protected void updateOptionBySelection() {
        RepositoryNode[] selectedNodes = treeViewer.getCheckNodes();
        if (selectedNodes.length > 1) {
            imageText.setText(getDefaultImageNamePattern());
            imageText.setEnabled(false);
            tagText.setText(getDefaultImageTagPattern());
            tagText.setEnabled(false);
        } else if (selectedNodes.length == 1) {
            ProcessItem selectedProcessItem = ExportJobUtil.getProcessItem(Arrays.asList(selectedNodes));
            imageText.setText(getDefaultImageName(selectedProcessItem));
            imageText.setEnabled(true);
            tagText.setText(getDefaultImageTag(selectedProcessItem));
            tagText.setEnabled(true);
        }
    }

    private void addDockerOptionsListener() {
        ModifyListener optionListener = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (remoteRadio.getSelection() && !isOptionValid(hostText, hostLabel.getText())) {
                    return;
                }
                if (!isOptionValid(imageText, imageLabel.getText())) {
                    return;
                }
                if (!isOptionValid(tagText, tagLabel.getText())) {
                    return;
                }
            }
        };

        hostText.addModifyListener(optionListener);
        imageText.addModifyListener(optionListener);
        tagText.addModifyListener(optionListener);

        remoteRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                hostText.setEnabled(remoteRadio.getSelection());
                if (remoteRadio.getSelection() && !isOptionValid(hostText, hostLabel.getText())) {
                    return;
                }
                if (!isOptionValid(imageText, imageLabel.getText())) {
                    return;
                }
                if (!isOptionValid(tagText, tagLabel.getText())) {
                    return;
                }
            }

        });
    }

    private boolean isOptionValid(Text text, String label) {
        boolean isValid = false;
        if (StringUtils.isBlank(text.getText())) {
            setErrorMessage(
                    org.talend.repository.i18n.Messages.getString("JavaJobScriptsExportWSWizardPage.DOCKER.errorMsg", label)); //$NON-NLS-1$
            setPageComplete(false);
            isValid = false;
        } else {
            setErrorMessage(null);
            setPageComplete(true);
            isValid = true;
        }
        return isValid;
    }

    private void restoreWidgetValuesForImage() {

        IDialogSettings settings = getDialogSettings();

        String remoteHost = settings.get(JavaJobScriptsExportWizardPage.STORE_DOCKER_REMOTE_HOST);
        if (StringUtils.isNotBlank(remoteHost)) {
            hostText.setText(remoteHost);
        }
        boolean isRemote = settings.getBoolean(JavaJobScriptsExportWizardPage.STORE_DOCKER_IS_REMOTE_HOST);
        localRadio.setSelection(!isRemote);
        remoteRadio.setSelection(isRemote);
        hostText.setEnabled(isRemote);
    }

    @Override
    public void createOptions(final Composite optionsGroup, Font font) {
        optionsGroupComposite = optionsGroup;
        optionsGroupFont = font;

        createOptionsForKar(optionsGroup, font);
        // createOptionForDockerImage(optionsGroup, font);

        if ("ROUTE_MICROSERVICE".equals(
                getProcessItem().getProperty().getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE))) {
            createDockerOptions(optionsGroup);
            restoreWidgetValuesForImage();
        }

        restoreWidgetValuesForKar();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1#createDestinationGroup(org.
     * eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createDestinationGroup(Composite parent) {
        Font font = parent.getFont();
        // destination specification group
        Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        destinationSelectionGroup.setLayout(layout);
        destinationSelectionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
        destinationSelectionGroup.setFont(font);

        destinationLabel = new Label(destinationSelectionGroup, SWT.NONE);
        destinationLabel.setText(getDestinationLabel());
        destinationLabel.setFont(font);

        // destination name entry field
        destinationNameField = new Combo(destinationSelectionGroup, SWT.SINGLE | SWT.BORDER);
        destinationNameField.addListener(SWT.Modify, this);
        destinationNameField.addListener(SWT.Selection, this);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        destinationNameField.setLayoutData(data);
        destinationNameField.setFont(font);
        BidiUtils.applyBidiProcessing(destinationNameField, "file"); //$NON-NLS-1$

        // destination browse button
        destinationBrowseButton = new Button(destinationSelectionGroup, SWT.PUSH);
        destinationBrowseButton.setText(DataTransferMessages.DataTransfer_browse);
        destinationBrowseButton.addListener(SWT.Selection, this);
        destinationBrowseButton.setFont(font);
        setButtonLayoutData(destinationBrowseButton);

        new Label(parent, SWT.NONE); // vertical spacer
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.wizards.exportjob.JobScriptsExportWizardPage#createControl(org.eclipse.swt.widgets.
     * Composite)
     */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        if ("ROUTE_MICROSERVICE".equals(
                getProcessItem().getProperty().getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE))) {

            exportTypeCombo.select(1);
            exportTypeCombo.notifyListeners(SWT.Selection, null);
            //exportTypeCombo.setEnabled(false);

            for (int i = 0; i < exportTypeCombo.getItems().length; i++) {
                if (exportTypeCombo.getItems()[i].equals(EXPORTTYPE_SPRING_BOOT_DOCKER_IMAGE)
                        || exportTypeCombo.getItems()[i].equals(EXPORTTYPE_SPRING_BOOT)) {
                    continue;
                } else {
                    exportTypeCombo.remove(i);
                }
            }

        } else {
            exportTypeCombo.select(0);
            exportTypeCombo.notifyListeners(SWT.Selection, null);
            exportTypeCombo.setEnabled(false);
        }
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

        if (EXPORTTYPE_SPRING_BOOT.equals(exportTypeCombo.getText())) {
            if (exportAsZip) {
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
        if (EXPORTTYPE_SPRING_BOOT.equals(exportTypeCombo.getText())) {
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

        if (source == destinationBrowseButton) {
            handleDestinationBrowseButtonPressed();
        }

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
        return addMavenScript;
    }

    @Override
    protected Map<ExportChoice, Object> getExportChoiceMap() {

        if (exportTypeCombo.getText().equals(EXPORTTYPE_SPRING_BOOT_DOCKER_IMAGE)) {
            return getExportChoiceMapForImage();
        }

        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<ExportChoice, Object>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.needJobItem, false);
        exportChoiceMap.put(ExportChoice.needSourceCode, false);

        exportChoiceMap.put(ExportChoice.needMetaInfo, true);
        exportChoiceMap.put(ExportChoice.needContext, true);
        exportChoiceMap.put(ExportChoice.needLauncher, exportAsZip);
        exportChoiceMap.put(ExportChoice.needMavenScript, false);

        exportChoiceMap.put(ExportChoice.onlyDefautContext, onlyExportDefaultContext);

        exportChoiceMap.put(ExportChoice.binaries, true);

        return exportChoiceMap;
    }

    private void createOptionsForKar(Composite optionsGroup, Font font) {
        if (!PluginChecker.isTIS()) {
            return;
        }

        addBSButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        addBSButton.setText("Add maven script");
        addBSButton.setSelection(addMavenScript);
        addBSButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                addMavenScript = addBSButton.getSelection();
            }
        });

        // TESB-17856: ESB Microservice can be exported only with Default context
        contextButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        contextButton.setText("Only export the default context"); //$NON-NLS-1$
        contextButton.setSelection(onlyExportDefaultContext);
        contextButton.setFont(font);
        contextButton.setVisible(PluginChecker.isTIS());
        contextButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onlyExportDefaultContext = contextButton.getSelection();
            }
        });

        exportAsZipButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        exportAsZipButton.setText("Export as ZIP"); //$NON-NLS-1$
        exportAsZipButton.setSelection(exportAsZip);
        exportAsZipButton.setFont(getFont());
        exportAsZipButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean selectContext = exportAsZipButton.getSelection();
                exportAsZip = selectContext;
                exportTypeCombo.notifyListeners(SWT.Selection, null);
            }
        });

        optionsGroupCompositeLayout = (GridData) optionsGroup.getParent().getLayoutData();
    }

    private void createOptionsForDocker(Composite optionsGroup, Font font) {
        if (!PluginChecker.isTIS()) {
            return;
        }

        // TESB-17856: ESB Microservice can be exported only with Default context
        contextButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        contextButton.setText("Only export the default context"); //$NON-NLS-1$
        contextButton.setFont(font);
        contextButton.setVisible(PluginChecker.isTIS());
        contextButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onlyExportDefaultContext = contextButton.getSelection();
            }
        });

        optionsGroupCompositeLayout = (GridData) optionsGroup.getParent().getLayoutData();
    }

    @Override
    protected void setDestinationValue(String value) {
        destinationNameField.setText(value);
    }

    @Override
    protected String getDestinationValue() {
        return destinationNameField.getText().trim();
    }

    @Override
    protected void giveFocusToDestination() {
        destinationNameField.setFocus();
    }

    @Override
    protected boolean ensureTargetIsValid() {
        if (JobExportType.getTypeFromString(exportTypeCombo.getText()) == JobExportType.IMAGE
                || JobExportType.getTypeFromString(exportTypeCombo.getText()) == JobExportType.MSESB_IMAGE) {
            return true;
        }
        return super.ensureTargetIsValid();
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

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.wizards.exportjob.JobScriptsExportWizardPage#getContextName()
     */
    @Override
    protected String getContextName() {
        String contextName = super.getContextName();
        if (StringUtils.isBlank(contextName)) {
            contextName = processItem.getProcess().getDefaultContext();
        }
        return contextName;
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

        IRunnableWithProgress buildJobHandlerAction = null;
        IRunnableWithProgress actionMS = null;
      
        Map<ExportChoice, Object> exportChoiceMap = getExportChoiceMap();
        boolean needMavenScript = false;
        // exportChoiceMap.containsKey(ExportChoice.needMavenScript) &&
        // exportChoiceMap.get(ExportChoice.needMavenScript) == Boolean.TRUE;

        if (needMavenScript && destinationKar.regionMatches(true, destinationKar.length() - 4, ".kar", 0, 4)) {
            destinationKar = destinationKar.substring(0, destinationKar.length() - 3) + "zip";
        }

        if(exportAsZip) {
            exportChoiceMap.put(ExportChoice.needAssembly, Boolean.TRUE);
        }

        if (new File(destinationKar).exists() && !exportTypeCombo.getText().equals(EXPORTTYPE_SPRING_BOOT_DOCKER_IMAGE)) {
            boolean yes = MessageDialog.openQuestion(getShell(),
                    Messages.getString("JavaCamelJobScriptsExportWSWizardPage.OverwriteKarTitle"),
                    Messages.getString("JavaCamelJobScriptsExportWSWizardPage.OverwriteKarMessage"));
            if (!yes) {
                return false;
            }
        } else if (!exportTypeCombo.getText().equals(EXPORTTYPE_KAR)) {
            exportChoiceMap.put(ExportChoice.needAssembly, Boolean.FALSE);
        }

        if (exportTypeCombo.getText().equals(EXPORTTYPE_SPRING_BOOT)
                || exportTypeCombo.getText().equals(EXPORTTYPE_SPRING_BOOT_DOCKER_IMAGE)) {

            IRunnableWithProgress worker = new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                    buildJobWithMaven(exportTypeCombo.getText().equals(EXPORTTYPE_SPRING_BOOT) ? JobExportType.ROUTE
                            : JobExportType.MSESB_IMAGE, monitor);

                }
            };
            try {
                getContainer().run(false, true, worker);
            } catch (Exception e) {
                MessageBoxExceptionHandler.process(e.getCause() == null ? e : e.getCause(), getShell());
                return false;
            }

        } else {

            if (getProcessItem() instanceof CamelProcessItem) {
                CamelProcessItem camelProcessItem = (CamelProcessItem) getProcessItem();
                if (camelProcessItem.isExportMicroService()) {
                    camelProcessItem.setExportMicroService(false);
                }
            }

            if (needMavenScript) {
                action = new JavaCamelJobScriptsExportWithMavenAction(exportChoiceMap, nodes[0], version, destinationKar, false);
            } else {

                exportChoiceMap.put(ExportChoice.esbExportType, "kar");

                buildJobHandlerAction = new IRunnableWithProgress() {

                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                        IBuildJobHandler buildJobHandler  = BuildJobFactory.createBuildJobHandler(getProcessItem(), getContextName(), version,
                                            exportChoiceMap, "ROUTE");

                        Map<String, Object> prepareParams = new HashMap<String, Object>();
                        prepareParams.put(IBuildResourceParametes.OPTION_ITEMS, true);
                        prepareParams.put(IBuildResourceParametes.OPTION_ITEMS_DEPENDENCIES, true);

                        try {
                              buildJobHandler.prepare(monitor, prepareParams);
                              buildJobHandler.build(monitor);

                              IFile targetFile = buildJobHandler.getJobTargetFile();

                              if (targetFile != null && targetFile.exists()) {
                                  try {
                                      FilesUtils.copyFile(targetFile.getLocation().toFile(), new File(getDestinationValue()));
                                  } catch (IOException e) {
                                      e.printStackTrace();
                                  }
                              }

                          } catch (Exception e) {
                              MessageBoxExceptionHandler.process(e.getCause() == null ? e : e.getCause(), getShell());
                          }

                    }
                };

                action = new JavaCamelJobScriptsExportWSAction(nodes[0], version, destinationKar, false);

                ProcessorUtilities.setExportAsOSGI(true);
            }

            try {
            	getContainer().run(false, true, action);
                getContainer().run(false, true, buildJobHandlerAction);
            } catch (Exception e) {
                MessageBoxExceptionHandler.process(e.getCause(), getShell());
                return false;
            }
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

    private Map<ExportChoice, Object> getExportChoiceMapForImage() {
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<ExportChoice, Object>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.buildImage, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needLauncher, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.launcherName, JobScriptsManager.UNIX_ENVIRONMENT);

        exportChoiceMap.put(ExportChoice.binaries, true);
//        exportChoiceMap.put(ExportChoice.needSystemRoutine, Boolean.TRUE);
//        exportChoiceMap.put(ExportChoice.needUserRoutine, Boolean.TRUE);
//        exportChoiceMap.put(ExportChoice.needTalendLibraries, Boolean.TRUE);
//        // TDQ-15391: when have tDqReportRun, must always export items.
//        if (EmfModelUtils.getComponentByName(getProcessItem(), "tDqReportRun") != null) { //$NON-NLS-1$
//            exportChoiceMap.put(ExportChoice.needJobItem, Boolean.TRUE);
//        } else {
//            exportChoiceMap.put(ExportChoice.needJobItem, Boolean.FALSE);
//        }
//        // TDQ-15391~
//        exportChoiceMap.put(ExportChoice.needSourceCode, Boolean.FALSE);
//        exportChoiceMap.put(ExportChoice.needDependencies, Boolean.TRUE);
//        exportChoiceMap.put(ExportChoice.needJobScript, Boolean.FALSE);
//        exportChoiceMap.put(ExportChoice.needAssembly, Boolean.FALSE);
        exportChoiceMap.put(ExportChoice.needContext, Boolean.TRUE);
//        exportChoiceMap.put(ExportChoice.contextName, getContextName());

        if (remoteRadio.getSelection()) {
            String host = hostText.getText();
            if (!StringUtils.isBlank(host)) {
                exportChoiceMap.put(ExportChoice.dockerHost, host);
            }
        }
        String imageName = imageText.getText();
        if (!StringUtils.isBlank(imageName)) {
            exportChoiceMap.put(ExportChoice.imageName, imageName);
        }
        String imageTag = tagText.getText();
        if (!StringUtils.isBlank(imageTag)) {
            exportChoiceMap.put(ExportChoice.imageTag, imageTag);
        }

//        if (applyToChildrenButton != null) {
//            exportChoiceMap.put(ExportChoice.applyToChildren, applyToChildrenButton.getSelection());
//        }
//        if (setParametersValueButton2 != null) {
//            exportChoiceMap.put(ExportChoice.needParameterValues, setParametersValueButton2.getSelection());
//            if (setParametersValueButton2.getSelection()) {
//                exportChoiceMap.put(ExportChoice.parameterValuesList, manager.getContextEditableResultValuesList());
//            }
//        }

//        exportChoiceMap.put(ExportChoice.binaries, Boolean.TRUE);
//        exportChoiceMap.put(ExportChoice.executeTests, Boolean.FALSE);
//        exportChoiceMap.put(ExportChoice.includeTestSource, Boolean.FALSE);
//        exportChoiceMap.put(ExportChoice.includeLibs, Boolean.TRUE);
//
//        exportChoiceMap.put(ExportChoice.needLog4jLevel, isNeedLog4jLevel());
//        exportChoiceMap.put(ExportChoice.log4jLevel, getLog4jLevel());

        return exportChoiceMap;
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
