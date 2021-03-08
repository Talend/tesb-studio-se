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
package org.talend.camel.designer.ui;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.designer.ui.wizards.JobCamelScriptsExportWizard;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * Action used to export job scripts. <br/>
 *
 * $Id: ExportJobScriptAction.java 1 2006-12-13 下午03:12:05 bqian
 *
 */
public class ExporCameltJobScriptAction extends AContextualAction {

    private static final String EXPORTJOBSCRIPTS = Messages.getString("ExportJobScriptAction.buildRoute"); //$NON-NLS-1$

    private Shell shell;

    public ExporCameltJobScriptAction() {
        super();
        this.setText(EXPORTJOBSCRIPTS);
        this.setToolTipText(EXPORTJOBSCRIPTS);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.EXPORT_JOB_ICON));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        setEnabled(false);
        if (selection.isEmpty() || selection.size() != 1) {
            return;
        }
        IRepositoryNode node = (IRepositoryNode) selection.getFirstElement();
        if (node.getType() == ENodeType.REPOSITORY_ELEMENT
                && node.getProperties(EProperties.CONTENT_TYPE) == CamelRepositoryNodeType.repositoryRoutesType
                && ERepositoryStatus.DELETED != ProxyRepositoryFactory.getInstance().getStatus(node.getObject())) {
            shell = viewer.getTree().getShell();
            setEnabled(true);
        }
    }

    protected void doRun() {
        JobCamelScriptsExportWizard processWizard = new JobCamelScriptsExportWizard();
        IWorkbench workbench = getWorkbench();
        processWizard.setWindowTitle(EXPORTJOBSCRIPTS);
        processWizard.init(workbench, (IStructuredSelection) this.getSelection());

        WizardDialog dialog = new WizardDialog(shell, processWizard);
        workbench.saveAllEditors(true);
        dialog.setPageSize(830, 450);
        dialog.open();
    }
}
