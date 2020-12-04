// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.ui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.talend.camel.core.model.camelProperties.BeanItem;
import org.talend.camel.core.model.camelProperties.BeansJarItem;
import org.talend.camel.designer.util.ECamelCoreImage;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.OverlayImageProvider;
import org.talend.core.model.properties.Item;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

public class AssignBeansToJarAction extends AbstractBeanAction {

    public AssignBeansToJarAction() {
        super();
        setText("Assign Bean to..."); //$NON-NLS-1$
        setToolTipText("Assign Bean to..."); //$NON-NLS-1$

        Image folderImg = ImageProvider.getImage(ECamelCoreImage.BEAN_ICON);
        this.setImageDescriptor(OverlayImageProvider.getImageWithNew(folderImg));
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        super.init(viewer, selection);
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }
        RepositoryNode node = (RepositoryNode) selection.getFirstElement();
        if (canWork) {
            if (node.getObjectType() != ERepositoryObjectType.BEANS
                    || !ProjectManager.getInstance().isInCurrentMainProject(node) || !isLastVersion(node)) {
                canWork = false;
            } else {
                Item item = node.getObject().getProperty().getItem();
                if (item instanceof BeanItem && RoutinesUtil.isInnerCodes(item.getProperty())) {
                    canWork = false;
                }
            }
        }
        if (canWork) {
            canWork = (factory.getStatus(node.getObject()) != ERepositoryStatus.DELETED);
        }
        setEnabled(canWork);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void doRun() {

        RepositoryReviewDialog dialog = new RepositoryReviewDialog(getWorkbenchPart().getSite().getShell(),
                ERepositoryObjectType.BEANSJAR);
        if (dialog.open() == Window.OK) {
            RepositoryNode targetBeansJarNode = dialog.getResult();
            ISelection selection = getSelection();
            Iterator<?> iterator = ((IStructuredSelection) selection).iterator();
            while (iterator.hasNext()) {
                RepositoryNode node = (RepositoryNode) iterator.next();
                IPath targetPath = RepositoryNodeUtilities.getPath(targetBeansJarNode);
                BeanItem sourceItem = (BeanItem) node.getObject().getProperty().getItem();
                List backupImports = new ArrayList<>(sourceItem.getImports());
                sourceItem.getImports().clear();
                RoutinesUtil.setInnerCodes(sourceItem.getProperty(), true);
                BeansJarItem beansJarItem = (BeansJarItem) targetBeansJarNode.getObject().getProperty().getItem();
                beansJarItem.getRoutinesJarType().getImports().addAll(backupImports);
                try {
                    ProxyRepositoryFactory.getInstance().copy(sourceItem, targetPath, sourceItem.getProperty().getLabel());
                    ProxyRepositoryFactory.getInstance().save(beansJarItem);
                    RelationshipItemBuilder.getInstance().addOrUpdateItem(beansJarItem);

                    // reset
                    sourceItem.getImports().addAll(backupImports);
                    RoutinesUtil.setInnerCodes(sourceItem.getProperty(), false);

                    ProxyRepositoryFactory.getInstance().deleteObjectPhysical(node.getObject());
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

}
