package org.talend.repository.services.action;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.seeker.RepositorySeekerManager;
import org.talend.designer.core.ui.action.EditProcess;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.services.model.services.ServiceConnection;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.model.services.ServiceOperation;
import org.talend.repository.services.model.services.ServicePort;
import org.talend.repository.services.utils.WSDLUtils;

public class OpenJobAction extends EditProcess {

    private RepositoryNode jobNode;

    public OpenJobAction() {
        super();

        setText("Open Job");
        setToolTipText("Open Job");
        setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PROCESS_ICON));
    }

    public OpenJobAction(boolean isToolbar) {
        this();

        setToolbar(isToolbar);
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        if (selection.size() != 1) {
            setEnabled(false);
            return;
        }
        IRepositoryNode node = (IRepositoryNode) selection.getFirstElement();
        if (!ERepositoryObjectType.SERVICESOPERATION.equals((ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE))
                || !WSDLUtils.isOperationInBinding(node)) { // not enabled if the operation doesn't define in binding
            setEnabled(false);
            return;
        }
        String jobId = getReferenceJobId(node);
        if (jobId == null) {
            setEnabled(false);
            return;
        }
        IRepositoryNode repoNode = RepositorySeekerManager.getInstance().searchRepoViewNode(jobId, false);
        jobNode = repoNode == null ? null : (RepositoryNode) repoNode;
        if (jobNode == null) {
            removeReferenecJobId(node);
            setEnabled(false);
            return;
        }
        final IStructuredSelection jobSelection = new StructuredSelection(jobNode);
        setSpecialSelection(new ISelectionProvider() {

            public void setSelection(ISelection arg0) {
            }

            public void removeSelectionChangedListener(ISelectionChangedListener arg0) {
            }

            public ISelection getSelection() {
                return jobSelection;
            }

            public void addSelectionChangedListener(ISelectionChangedListener arg0) {
            }
        });
        super.init(viewer, jobSelection);
    }

    @Override
    protected RepositoryNode getCurrentRepositoryNode() {
        return jobNode;
    }

    public Class<?> getClassForDoubleClick() {
        final IRepositoryNode repositoryNode = super.getCurrentRepositoryNode();
        // not enabled if the operation doesn't define in binding
        if (null != repositoryNode && WSDLUtils.isOperationInBinding(repositoryNode) && getReferenceJobId(repositoryNode) != null) {
            return ServiceOperation.class;
        }
        return Object.class; // for isDoubleClickAction
    }

    protected static String getReferenceJobId(IRepositoryNode node) {
        String parentPortName = node.getParent().getObject().getLabel();
        ServiceItem serviceItem = (ServiceItem) node.getParent().getParent().getObject().getProperty().getItem();
        List<ServicePort> listPort = ((ServiceConnection) serviceItem.getConnection()).getServicePort();
        for (ServicePort port : listPort) {
            if (port.getName().equals(parentPortName)) {
                List<ServiceOperation> listOperation = port.getServiceOperation();
                for (ServiceOperation operation : listOperation) {
                    if (operation.getLabel().equals(node.getObject().getLabel())) {
                        return operation.getReferenceJobId();
                    }
                }
                break;
            }
        }
        return null;
    }

    protected static void removeReferenecJobId(IRepositoryNode node) {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        try {
            String parentPortName = node.getParent().getObject().getLabel();
            ServiceItem serviceItem = (ServiceItem) node.getParent().getParent().getObject().getProperty().getItem();
            List<ServicePort> listPort = ((ServiceConnection) serviceItem.getConnection()).getServicePort();
            for (ServicePort port : listPort) {
                if (port.getName().equals(parentPortName)) {
                    List<ServiceOperation> listOperation = port.getServiceOperation();
                    for (ServiceOperation operation : listOperation) {
                        if (operation.getLabel().equals(node.getObject().getLabel())) {
                            operation.setReferenceJobId(null);
                            operation.setLabel(operation.getName());
                            factory.save(node.getObject().getProperty().getItem());
                        }
                    }
                    break;
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

    }

}
