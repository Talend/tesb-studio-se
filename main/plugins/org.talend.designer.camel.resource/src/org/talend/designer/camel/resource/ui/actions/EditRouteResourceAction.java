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
package org.talend.designer.camel.resource.ui.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.camel.core.model.camelProperties.RouteResourceItem;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.core.model.properties.Property;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.camel.resource.RouteResourceActivator;
import org.talend.designer.camel.resource.i18n.Messages;
import org.talend.designer.camel.resource.ui.util.RouteResourceEditorUtil;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: EditProcess.java 52559 2010-12-13 04:14:06Z nrousseau $
 * 
 */
public class EditRouteResourceAction extends AContextualAction {

	public EditRouteResourceAction() {
        setText(Messages.getString("EditRouteResourceAction_Title"));
        setToolTipText(Messages.getString("EditRouteResourceAction_Tooltip"));
        setImageDescriptor(RouteResourceActivator.createImageDesc("icons/edit-resource.png"));
	}

	@Override
	protected void doRun() {
		ISelection selection = getSelection();
		if (selection == null) {
			return;
		}
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj == null) {
			return;
		}
		openOrBindEditor((IRepositoryNode) obj);
	}

	@Override
	public Class<?> getClassForDoubleClick() {
		return RouteResourceItem.class;
	}

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1
            && !ProxyRepositoryFactory.getInstance().isUserReadOnlyOnCurrentProject();
        if (canWork) {
            final IRepositoryNode node = (IRepositoryNode) selection.getFirstElement();
            canWork = node.getObjectType() == CamelRepositoryNodeType.repositoryRouteResourceType
                && node.getObject() != null
                && ProxyRepositoryFactory.getInstance().getStatus(node.getObject()) != ERepositoryStatus.DELETED
                && ProjectManager.getInstance().isInCurrentMainProject(node)
                && isLastVersion(node);
        }
        setEnabled(canWork);
    }

	/**
	 * Open or bind RouteResourceEditor
	 * 
	 * @param node
	 */
	private void openOrBindEditor(IRepositoryNode node) {
		final Property property = node.getObject().getProperty();
		if (property != null) {
			Assert.isTrue(property.getItem() instanceof RouteResourceItem);
			final RouteResourceItem item = (RouteResourceItem) property.getItem();
			RouteResourceEditorUtil.openEditor(getActivePage(), node, item);
		}

	}
}
