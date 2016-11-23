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
package org.talend.repository.services.action;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.services.Messages;
import org.talend.repository.services.model.services.ServiceConnection;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.model.services.ServiceOperation;
import org.talend.repository.services.model.services.ServicePort;
import org.talend.repository.services.ui.ServiceExportWizard;
import org.talend.repository.services.utils.ESBRepositoryNodeType;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * Action used to export job scripts. <br/>
 */
public class ExportServiceAction extends AContextualAction {

    private static final String EXPORT_SERVICE_LABEL = Messages.ExportServiceAction_Action_Label;

    private ServiceItem serviceItem;

    private Shell shell;

    public ExportServiceAction() {
        super();
        this.setText(EXPORT_SERVICE_LABEL);
        this.setToolTipText(EXPORT_SERVICE_LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.EXPORT_JOB_ICON));
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        setEnabled(false);
        if (selection.isEmpty() || selection.size() > 1) {
            return;
        }

        final IRepositoryNode node = (IRepositoryNode) selection.getFirstElement();
        if (node.getType() == ENodeType.REPOSITORY_ELEMENT
                && node.getProperties(EProperties.CONTENT_TYPE) == ESBRepositoryNodeType.SERVICES && node.getObject() != null
                && ProxyRepositoryFactory.getInstance().getStatus(node.getObject()) != ERepositoryStatus.DELETED) {
            serviceItem = (ServiceItem) node.getObject().getProperty().getItem();
            if (viewer != null) {
                shell = viewer.getTree().getShell();
            } else {
                shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            }
            setEnabled(true);
        }
    }

    @Override
    public boolean isVisible() {
        return isEnabled();
    }

    @Override
    protected void doRun() {
        try {
            if (!isAllOperationsAssignedJob(serviceItem)) {
                boolean isContinue = MessageDialog.openQuestion(shell, Messages.ExportServiceAction_noJobDialogTitle,
                        Messages.ExportServiceAction_noJobDialogMsg);
                if (!isContinue) {
                    return;
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        ServiceExportWizard processWizard = new ServiceExportWizard(serviceItem);
        IWorkbench workbench = getWorkbench();
        processWizard.setWindowTitle(EXPORT_SERVICE_LABEL);

        WizardDialog dialog = new WizardDialog(shell, processWizard);
        workbench.saveAllEditors(true);
        dialog.open();
    }

    private static boolean isAllOperationsAssignedJob(ServiceItem serviceItem) throws PersistenceException {
        ServiceConnection connection = (ServiceConnection) serviceItem.getConnection();
        List<IRepositoryViewObject> jobs = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.PROCESS);
        for (ServicePort port : connection.getServicePort()) {
            for (ServiceOperation operation : port.getServiceOperation()) {
                String referenceJobId = operation.getReferenceJobId();
                if (referenceJobId == null || referenceJobId.equals("")) { //$NON-NLS-1$
                    return false;
                }

                boolean found = false;
                for (IRepositoryViewObject job : jobs) {
                    if (referenceJobId.equals(job.getId())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return true;
    }
}
