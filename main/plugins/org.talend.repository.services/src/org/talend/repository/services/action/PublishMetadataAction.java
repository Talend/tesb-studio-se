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
package org.talend.repository.services.action;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.services.Messages;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.utils.ESBRepositoryNodeType;
import org.talend.repository.services.utils.WSDLUtils;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * Action used to export job scripts. <br/>
 * 
 * $Id: ExportJobScriptAction.java 1 2006-12-13 ä¸‹å�ˆ03:12:05 bqian
 * 
 */
public class PublishMetadataAction extends AContextualAction {

    private Shell shell;

    private ServiceItem serviceItem;

    public PublishMetadataAction() {
        super();
        this.setText(Messages.PublishMetadataAction_Name);
        this.setToolTipText(Messages.PublishMetadataAction_Name);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.HIERARCHY_ICON));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        setEnabled(false);
        if (selection.size() != 1) {
            return;
        }
        RepositoryNode node = (RepositoryNode) selection.iterator().next();
        if (node.getType() == ENodeType.REPOSITORY_ELEMENT
                && node.getProperties(EProperties.CONTENT_TYPE) == ESBRepositoryNodeType.SERVICES
                && node.getObject() != null
                && ProxyRepositoryFactory.getInstance().getStatus(node.getObject()) != ERepositoryStatus.DELETED) {
            serviceItem = (ServiceItem) node.getObject().getProperty().getItem();

            if (viewer != null) {
                shell = viewer.getTree().getShell();
            } else {
                shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            }

            try {
            	IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
    			factory.updateLockStatus();
    			ERepositoryStatus status = factory.getStatus(node.getObject());
    			if(!status.isEditable() && !status.isPotentiallyEditable()){
    				setEnabled(false);
    				return;
    			}
    		} catch (PersistenceException e) {
    			e.printStackTrace();
    		}
            setEnabled(true);
        }
    }

    @Override
    protected void doRun() {
        try {
            new ProgressMonitorDialog(null).run(true, true,
                new PublishMetadataRunnable(WSDLUtils.getDefinition(serviceItem), shell));
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e.getCause());
        } catch (InterruptedException e) {
        }
    }

}
