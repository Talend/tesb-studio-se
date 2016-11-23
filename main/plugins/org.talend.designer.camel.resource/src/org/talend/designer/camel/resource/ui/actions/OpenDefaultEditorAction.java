package org.talend.designer.camel.resource.ui.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPage;
import org.talend.camel.core.model.camelProperties.RouteResourceItem;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.core.model.properties.Property;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.camel.resource.RouteResourceActivator;
import org.talend.designer.camel.resource.i18n.Messages;
import org.talend.designer.camel.resource.ui.util.RouteResourceEditorUtil;
import org.talend.designer.core.DesignerPlugin;
import org.talend.repository.model.BinRepositoryNode;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;

public class OpenDefaultEditorAction extends AContextualAction {

    public OpenDefaultEditorAction() {
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
        RepositoryNode node = (RepositoryNode) obj;
        opendTextEditor(node);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getClassForDoubleClick() {
        return RouteResourceItem.class;
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        if (canWork) {
            Object o = selection.getFirstElement();
            RepositoryNode node = (RepositoryNode) o;
            switch (node.getType()) {
            case REPOSITORY_ELEMENT:
                if (node.getObjectType() != CamelRepositoryNodeType.repositoryRouteResourceType) {
                    canWork = false;
                } else {
                    IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
                    IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
                    IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                    if (!factory.isUserReadOnlyOnCurrentProject() && repFactory.isPotentiallyEditable(node.getObject())) {
                        this.setText(Messages.getString("OpenDefaultEditorAction_title")); //$NON-NLS-1$
                    } else {
                        this.setText(Messages.getString("OpenDefaultEditorAction_title_read")); //$NON-NLS-1$
                    }
                }
                break;
            default:
                canWork = false;
            }
            RepositoryNode parent = node.getParent();
            if (canWork && parent != null && parent instanceof BinRepositoryNode) {
                canWork = false;
            }

            // If the editProcess action canWork is true, then detect that the
            // job version is the latest version or not.
            if (canWork) {
                canWork = isLastVersion(node);
            }

        }
        setEnabled(canWork);

        this.setToolTipText(Messages.getString("OpenDefaultEditorAction_tooltip")); //$NON-NLS-1$
        this.setImageDescriptor(RouteResourceActivator.createImageDesc("icons/edit-resource.png")); //$NON-NLS-1$
    }

    /*
     * Open or bind RouteResourceEditor
     */
    private void opendTextEditor(RepositoryNode node) {
        Property property = node.getObject().getProperty();
        RouteResourceItem item = null;
        if (property != null) {

            Assert.isTrue(property.getItem() instanceof RouteResourceItem);
            item = (RouteResourceItem) property.getItem();
            IWorkbenchPage page = getActivePage();
            RouteResourceEditorUtil.openDefaultEditor(page, node, item);
        }
    }

}
