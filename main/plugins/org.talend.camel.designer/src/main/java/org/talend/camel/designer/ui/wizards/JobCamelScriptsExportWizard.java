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

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.talend.camel.designer.CamelDesignerPlugin;
import org.talend.camel.designer.i18n.Messages;

/**
 * Job scripts export wizard.
 */
public class JobCamelScriptsExportWizard extends Wizard implements IExportWizard {

    protected IStructuredSelection selection;

    protected JavaCamelJobScriptsExportWSWizardPage mainPage;

    public JobCamelScriptsExportWizard() {
        setWindowTitle(Messages.getString("JobScriptsExportWizard.buildRounte")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/exportzip_wiz.png"));//$NON-NLS-1$
        setNeedsProgressMonitor(true);

        IDialogSettings workbenchSettings = CamelDesignerPlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection("JobCamelScriptsExportWizard"); //$NON-NLS-1$
        if (section == null) {
            section = workbenchSettings.addNewSection("JobCamelScriptsExportWizard"); //$NON-NLS-1$
        }
        setDialogSettings(section);
    }

    @Override
    public void addPages() {
        mainPage = new JavaCamelJobScriptsExportWSWizardPage(selection);
        addPage(mainPage);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        this.selection = currentSelection;
        List<?> selectedResources = IDE.computeSelectedResources(currentSelection);
        if (!selectedResources.isEmpty()) {
            this.selection = new StructuredSelection(selectedResources);
        }
    }

    @Override
    public boolean performFinish() {
        return mainPage.finish();
    }

}
