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
package org.talend.repository.services.action;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ICoreService;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.services.model.services.ServiceConnection;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.ui.ServiceMetadataDialog;
import org.talend.repository.services.utils.ESBRepositoryNodeType;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * Action used to set service metadata. <br/>
 *
 *
 */
public class ServiceMetadataAction extends AContextualAction {

    private static final String ACTION_LABEL = "ESB Runtime Options";

    public ServiceMetadataAction() {
        super();
        this.setText(ACTION_LABEL);
        this.setToolTipText(ACTION_LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.EDIT_ICON));
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        if (selection.size() != 1) {
            setEnabled(false);
            return;
        }
        RepositoryNode node = (RepositoryNode) selection.getFirstElement();
        if (node.getType() != ENodeType.REPOSITORY_ELEMENT
                || node.getProperties(EProperties.CONTENT_TYPE) != ESBRepositoryNodeType.SERVICES
                || node.getObject() == null
                || ProxyRepositoryFactory.getInstance().getStatus(node.getObject()) == ERepositoryStatus.DELETED) {
            setEnabled(false);
            return;
        }
    	setNode(node);
    	setEnabled(true);
    }

    @Override
    protected void doRun() {
        final IRepositoryNode node = getNode();
        final ServiceItem serviceItem = (ServiceItem) node.getObject().getProperty().getItem();
        final boolean isLocked = isLocked(node.getObject());
        new ServiceMetadataDialog(getWorkbenchWindow(), serviceItem, (ServiceConnection) serviceItem.getConnection())
            .open();
        if (!isLocked) {
            // restore lock state.
            try {
                ProxyRepositoryFactory.getInstance().unlock(node.getObject());
            } catch (PersistenceException | LoginException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    private static boolean isLocked(IRepositoryViewObject object) {
         if (GlobalServiceRegister.getDefault().isServiceRegistered(ICoreService.class)) {
             ICoreService coreService = (ICoreService) GlobalServiceRegister.getDefault().getService(ICoreService.class);
             boolean isOpened = coreService.isOpenedItemInEditor(object);
             if(isOpened) {
            	 return true;
             }
         }
         ServiceItem serviceItem = (ServiceItem) object.getProperty().getItem();
         return serviceItem.getState().isLocked();
    }
}
