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
package org.talend.repository.services.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.model.process.IContext;
import org.talend.core.prefs.IDEWorkbenchPlugin;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.services.Activator;
import org.talend.repository.services.Messages;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.ui.action.ExportServiceAction;
import org.talend.repository.services.ui.action.ExportServiceWithMavenAction;
import org.talend.repository.services.ui.scriptmanager.ServiceExportWithMavenManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
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
        IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection("ServiceExportWizard"); //$NON-NLS-1$
        if (section == null) {
            section = workbenchSettings.addNewSection("ServiceExportWizard"); //$NON-NLS-1$
        }
        setDialogSettings(section);

        //        setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/exportzip_wiz.png"));//$NON-NLS-1$
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
        try {
            if (mainPage.isAddMavenScript()) {
                ServiceExportWithMavenManager mavenManager = new ServiceExportWithMavenManager(exportChoiceMap,
                        IContext.DEFAULT, JobScriptsManager.LAUNCHER_ALL, IProcessor.NO_STATISTICS, IProcessor.NO_TRACES);
                IRunnableWithProgress action = new ExportServiceWithMavenAction(mavenManager, exportChoiceMap, serviceItem,
                        destinationValue);
                getContainer().run(false, true, action);
            } else {
                IRunnableWithProgress action = new ExportServiceAction(serviceItem, destinationValue, exportChoiceMap);
                getContainer().run(true, true, action);
            }

            mainPage.finish();
        } catch (InvocationTargetException e) {
            MessageBoxExceptionHandler.process(e.getCause(), getShell());
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean performCancel() {
        ProcessorUtilities.resetExportConfig();
        mainPage = null;
        return true;
    }
}
