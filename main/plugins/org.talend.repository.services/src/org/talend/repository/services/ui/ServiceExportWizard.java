// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.IContext;
import org.talend.core.prefs.IDEWorkbenchPlugin;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.repository.build.IBuildResourceParametes;
import org.talend.designer.maven.tools.BuildCacheManager;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.services.Messages;
import org.talend.repository.services.ServicesPlugin;
import org.talend.repository.services.export.BuildDataServiceHandler;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

/**
 * Job scripts export wizard. <br/>
 *
 * $Id: ServiceExportWizard.java 1 2006-12-13 PM 03:13:18 bqian
 *
 */
public class ServiceExportWizard extends Wizard implements IExportWizard {

    private final ServiceItem serviceItem;

    protected String exportType;

    private ServiceExportWSWizardPage mainPage;

    /**
     * Creates a wizard for exporting workspace resources to a zip file.
     */
    public ServiceExportWizard(ServiceItem serviceItem) {
        this.serviceItem = serviceItem;
        IDialogSettings workbenchSettings = ServicesPlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection("ServiceExportWizard"); //$NON-NLS-1$
        if (section == null) {
            section = workbenchSettings.addNewSection("ServiceExportWizard"); //$NON-NLS-1$
        }
        setDialogSettings(section);

        // setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/exportzip_wiz.png"));//$NON-NLS-1$
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(IDEWorkbenchPlugin.IDE_WORKBENCH,
                "$nl$/icons/full/wizban/exportzip_wiz.png")); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
    }

    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
    }

    @Override
    public void addPages() {
        super.addPages();
        mainPage = new ServiceExportWSWizardPage(serviceItem);
        addPage(mainPage);
    }

    @Override
    public boolean performFinish() {
        final String destinationValue = mainPage.getDestinationValue();
        // TESB-7319: add confirm dialog
        if (new File(destinationValue).exists()) {
            boolean openQuestion = MessageDialog.openQuestion(getShell(), Messages.ServiceExportWizard_destinationExistTitle,
                    Messages.ServiceExportWizard_destinationExistMessage);
            if (!openQuestion) {
                return false;
            }
        }
        // END TESB-7319

        Map<ExportChoice, Object> exportChoiceMap = mainPage.getExportChoiceMap();
        exportChoiceMap.put(ExportChoice.doNotCompileCode, false);
        exportChoiceMap.put(ExportChoice.needDependencies, false);
        exportChoiceMap.put(ExportChoice.addStatistics, false);
        exportChoiceMap.put(ExportChoice.addTracs, false);
        exportChoiceMap.put(ExportChoice.needAntScript, false);
        exportChoiceMap.put(ExportChoice.needMavenScript, false);
        exportChoiceMap.put(ExportChoice.applyToChildren, false);
        exportChoiceMap.put(ExportChoice.needContext, true);
        exportChoiceMap.put(ExportChoice.binaries, true);
        exportChoiceMap.put(ExportChoice.needSourceCode, false);
        exportChoiceMap.put(ExportChoice.executeTests, false);
        exportChoiceMap.put(ExportChoice.includeTestSource, false);
        exportChoiceMap.put(ExportChoice.includeLibs, true);
        exportChoiceMap.put(ExportChoice.needLog4jLevel, false);
        exportChoiceMap.put(ExportChoice.needAssembly, true);

        // update to use BuildDataServiceHandler
        IProgressMonitor pMonitor = new NullProgressMonitor();
        int scale = 10;

        BuildDataServiceHandler buildServiceHandler = new BuildDataServiceHandler(serviceItem,
                serviceItem.getProperty().getVersion(), IContext.DEFAULT, exportChoiceMap);
        Map<String, Object> prepareParams = new HashMap<String, Object>();
        prepareParams.put(IBuildResourceParametes.OPTION_ITEMS, true);
        prepareParams.put(IBuildResourceParametes.OPTION_ITEMS_DEPENDENCIES, true);
        try {
            buildServiceHandler.prepare(pMonitor, prepareParams);
            buildServiceHandler.build(new SubProgressMonitor(pMonitor, scale));
            IFile serviceTargetFile = buildServiceHandler.getJobTargetFile();
            if (serviceTargetFile != null && serviceTargetFile.exists()) {
                FilesUtils.copyFile(serviceTargetFile.getLocation().toFile(), new File(destinationValue));
            } else {
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
                    IRunProcessService service = (IRunProcessService) GlobalServiceRegister.getDefault()
                            .getService(IRunProcessService.class);
                    ITalendProcessJavaProject talendJavaProject = service.getTalendJobJavaProject(serviceItem.getProperty());
                    String mvnLogFilePath = talendJavaProject.getProject().getFile("lastGenerated.log").getLocation() //$NON-NLS-1$
                            .toPortableString();
                    Exception e = new Exception(
                            "Service was not built successfully, please check the logs for more details available on "
                                    + mvnLogFilePath);
                    MessageBoxExceptionHandler.process(e, getShell());
                }
                return false;
            }
        } catch (Exception e) {
            MessageBoxExceptionHandler.process(e, getShell());
            BuildCacheManager.getInstance().performBuildFailure();
            return false;
        } finally {
            ProcessorUtilities.resetExportConfig();
        }

        mainPage.finish();

        return true;
    }

    @Override
    public boolean performCancel() {
        ProcessorUtilities.resetExportConfig();
        mainPage = null;
        return true;
    }
}
