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
package org.talend.camel.designer.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.designer.ui.wizards.OpenCamelExistVersionProcessWizard;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.services.IUIRefresher;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.actions.EditPropertiesAction;

public class OpenCamelExistVersionProcessAction extends EditPropertiesAction {

    private static final String ACTION_LABEL = Messages.getString("OpenExistVersionProcess.open"); //$NON-NLS-1$

    public OpenCamelExistVersionProcessAction() {
        super();

        this.setText(ACTION_LABEL);
        this.setToolTipText(ACTION_LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.ROUTES_ICON));
    }

    @Override
    protected void doRun() {
        ISelection selection = getSelection();
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        IRepositoryNode node = (IRepositoryNode) obj;

        IPath path = RepositoryNodeUtilities.getPath(node);
        String originalName = node.getObject().getLabel();

        RepositoryObject repositoryObj = new RepositoryObject(node.getObject().getProperty());
        repositoryObj.setRepositoryNode(node.getObject().getRepositoryNode());
        OpenCamelExistVersionProcessWizard wizard = new OpenCamelExistVersionProcessWizard(repositoryObj);
        WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
        dialog.setHelpAvailable(false);
        dialog.setPageSize(300, 250);
        dialog.setTitle(Messages.getString("OpenExistVersionProcess.open.dialog")); //$NON-NLS-1$
        if (dialog.open() == Dialog.OK) {
            refresh(node);
            // refresh the corresponding editor's name
            IEditorPart part = getCorrespondingEditor(node);
            if (part != null && part instanceof IUIRefresher) {
                ((IUIRefresher) part).refreshName();
            } else {
                processRoutineRenameOperation(originalName, node, path);
            }
        }
    }

    // http://jira.talendforge.org/browse/TESB-5930
    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = selection.size() == 1;
        if (canWork) {
            Object o = selection.getFirstElement();
            if (o instanceof IRepositoryNode) {
                IRepositoryNode node = (IRepositoryNode) o;
                switch (node.getType()) {
                case REPOSITORY_ELEMENT:
                    if (node.getObjectType() == CamelRepositoryNodeType.repositoryRoutesType) {
                        canWork = true;
                    } else {
                        canWork = false;
                    }
                    break;
                default:
                    canWork = false;
                    break;
                }
                if (canWork) {
                    canWork = node.getObject().getRepositoryStatus() != ERepositoryStatus.DELETED;
                }
                if (canWork) {
                    canWork = isLastVersion(node);
                }
            }
        }
        setEnabled(canWork);
    }

}
