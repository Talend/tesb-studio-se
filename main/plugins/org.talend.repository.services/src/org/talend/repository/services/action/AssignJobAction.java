package org.talend.repository.services.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.actions.metadata.AbstractCreateAction;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.services.Messages;
import org.talend.repository.services.model.services.ServiceConnection;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.model.services.ServiceOperation;
import org.talend.repository.services.model.services.ServicePort;
import org.talend.repository.services.utils.OperationRepositoryObject;
import org.talend.repository.services.utils.PortRepositoryObject;
import org.talend.repository.services.utils.WSDLUtils;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

public class AssignJobAction extends AbstractCreateAction {

    private static final String createLabel = "Assign Job";

    private static final ERepositoryObjectType currentNodeType = ERepositoryObjectType.SERVICESOPERATION;

    public AssignJobAction() {
        super();

        this.setText(createLabel);
        this.setToolTipText(createLabel);

        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PROCESS_ICON));
    }

    public AssignJobAction(RepositoryNode operationNode) {
        this();
        repositoryNode = operationNode;
    }

    public AssignJobAction(boolean isToolbar) {
        this();
        setToolbar(isToolbar);
    }

    @Override
    protected void init(RepositoryNode node) {
        ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        if (!currentNodeType.equals(nodeType)) {
            return;
        }

        IProxyRepositoryFactory proxyFactory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
        try {
            proxyFactory.updateLockStatus();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        IRepositoryViewObject repositoryViewObject = node.getObject();
        ERepositoryStatus status = proxyFactory.getStatus(repositoryViewObject);
        if (!status.isEditable() && !status.isPotentiallyEditable()) {
            setEnabled(false);
        } else {
            // not enabled if the operation doesn't define in binding
            if (!WSDLUtils.isOperationInBinding(node)) {
                setEnabled(false);
                return;
            }
            setEnabled(true);
        }
    }

    @Override
    protected void doRun() {
        if (repositoryNode == null) {
            repositoryNode = getCurrentRepositoryNode();
        }
        if (isToolbar()) {
            if (repositoryNode != null && repositoryNode.getContentType() != currentNodeType) {
                repositoryNode = null;
            }
            if (repositoryNode == null) {
                repositoryNode = getRepositoryNodeForDefault(currentNodeType);
            }
        }
        RepositoryReviewDialog dialog = new RepositoryReviewDialog(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), ERepositoryObjectType.PROCESS, "");
        List<String> jobIDList = getAllReferenceJobId(repositoryNode);
        dialog.setJobIDList(jobIDList);
        if (dialog.open() == RepositoryReviewDialog.OK) {
            changeOldJob();
            assign(dialog.getResult());
        }
    }

    public void changeOldJob() {
        String operationName = repositoryNode.getObject().getLabel();
        String portName = repositoryNode.getParent().getObject().getLabel();
        ServiceItem serviceItem = (ServiceItem) repositoryNode.getParent().getParent().getObject().getProperty().getItem();
        List<ServicePort> listPort = ((ServiceConnection) serviceItem.getConnection()).getServicePort();
        String oldJobID = null;
        for (ServicePort port : listPort) {
            if (port.getName().equals(portName)) {
                List<ServiceOperation> listOperation = port.getServiceOperation();
                for (ServiceOperation operation : listOperation) {
                    if (operation.getLabel().equals(operationName)) {
                        oldJobID = operation.getReferenceJobId();
                        break;
                    }
                }
                break;
            }
        }
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        try {
            if (oldJobID != null) {
                IRepositoryViewObject object = factory.getLastVersion(oldJobID);
                Item item = object.getProperty().getItem();
                ProcessItem processItem = (ProcessItem) item;
                //
                IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
                IProcess2 process = null;
                IEditorReference[] reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .getEditorReferences();
                List<IProcess2> processes = RepositoryPlugin.getDefault().getDesignerCoreService().getOpenedProcess(reference);
                for (IProcess2 processOpen : processes) {
                    if (processOpen.getProperty().getItem() == processItem) {
                        process = processOpen;
                        break;
                    }
                }
                if (process == null) {
                    IProcess proc = service.getProcessFromProcessItem(processItem);
                    if (proc instanceof IProcess2) {
                        process = (IProcess2) proc;
                    }
                }

                if (process != null) {
                    List<? extends INode> nodelist = process.getGraphicalNodes();
                    for (INode node : nodelist) {
                        if (node.getComponent().getName().equals("tESBProviderRequest")) {
                            repositoryChangeToBuildIn(repositoryNode, node);
                        }
                    }
                    processItem.setProcess(process.saveXmlFile());
                    factory.save(processItem);
                }
            }
        } catch (PersistenceException | IOException e) {
            ExceptionHandler.process(e);
        }
    }

    public boolean assign(IRepositoryNode jobNode) {
        if (jobNode == null) {
            return false;
        }
        IRepositoryViewObject repositoryObject = jobNode.getObject();
        final Item item = repositoryObject.getProperty().getItem();
        // judge the job whether had T_ESB_PROVIDER_REQUEST
        ProcessItem processItem = (ProcessItem) item;
        NodeType providerNode = null;
        for (Object obj : processItem.getProcess().getNode()) {
            NodeType node = (NodeType) obj;
            if (CreateNewJobAction.T_ESB_PROVIDER_REQUEST.equals(node.getComponentName())) {
                providerNode = node;
                break;
            }
        }
        if (null == providerNode) {
            MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.AssignJobAction_WarningTitle,
                    Messages.AssignJobAction_WarningMessage);
            return false;
        }
        try {
            String jobID = item.getProperty().getId();
            String jobName = item.getProperty().getLabel();
            String operationName = repositoryNode.getObject().getLabel();
            String portName = repositoryNode.getParent().getObject().getLabel();
            ServiceItem serviceItem = (ServiceItem) repositoryNode.getParent().getParent().getObject().getProperty().getItem();

            List<ServicePort> listPort = ((ServiceConnection) serviceItem.getConnection()).getServicePort();
            for (ServicePort port : listPort) {
                if (port.getName().equals(portName)) {
                    List<ServiceOperation> listOperation = port.getServiceOperation();
                    for (ServiceOperation operation : listOperation) {
                        if (operation.getLabel().equals(operationName)) {
                            operation.setReferenceJobId(jobID);
                            operation.setLabel(operation.getName() + "-" + jobName);
                            break;
                        }
                    }
                    break;
                }
            }

            IFile wsdlPath = WSDLUtils.getWsdlFile(serviceItem);
            Map<String, String> serviceParameters = WSDLUtils.getServiceOperationParameters(wsdlPath,
                    ((OperationRepositoryObject) repositoryNode.getObject()).getName(), portName);

            for (Object paramObj : providerNode.getElementParameter()) {
                ElementParameterType param = (ElementParameterType) paramObj;
                String name = param.getName();
                if (serviceParameters.containsKey(name)) {
                    param.setValue(serviceParameters.get(name));
                }
            }

            IProcess2 process = null;
            IEditorReference[] reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .getEditorReferences();
            List<IProcess2> processes = RepositoryPlugin.getDefault().getDesignerCoreService().getOpenedProcess(reference);
            for (IProcess2 processOpen : processes) {
                if (processOpen.getProperty().getItem() == processItem) {
                    process = processOpen;
                    break;
                }
            }
            if (process == null) {
                IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
                IProcess proc = service.getProcessFromProcessItem(processItem);
                if (proc instanceof IProcess2) {
                    process = (IProcess2) proc;
                }
            }

            if (process != null) {
                List<? extends INode> nodelist = process.getGraphicalNodes();
                for (INode node : nodelist) {
                    if (node.getComponent().getName().equals("tESBProviderRequest")) {
                        repositoryChange(repositoryNode, node);
                    }
                }
                processItem.setProcess(process.saveXmlFile());
            }

            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            factory.save(processItem);

            factory.save(serviceItem);
            return true;
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return false;
    }

    private RepositoryNode getTopParent(RepositoryNode repositoryNode) {
        repositoryNode = repositoryNode.getParent();
        if (repositoryNode.getParent() instanceof ProjectRepositoryNode) {
            return repositoryNode;
        }
        return getTopParent(repositoryNode);
    }

    public List<String> getAllReferenceJobId() {
        return getAllReferenceJobId(getCurrentRepositoryNode());
    }

    private List<String> getAllReferenceJobId(RepositoryNode repositoryNode) {
        repositoryNode = getTopParent(repositoryNode);
        List<IRepositoryNode> nodeList = repositoryNode.getChildren();
        List<ServiceOperation> operaList = new ArrayList<>();
        List<String> jobIDList = new ArrayList<>();
        for (IRepositoryNode node : nodeList) {
            if (node.getObject().getProperty().getItem() instanceof ServiceItem) {
                ServiceItem item = (ServiceItem) node.getObject().getProperty().getItem();
                ServiceConnection conn = (ServiceConnection) item.getConnection();
                EList<ServicePort> portList = conn.getServicePort();
                for (ServicePort port : portList) {
                    operaList.addAll(port.getServiceOperation());
                }
            }
        }
        for (ServiceOperation operation : operaList) {
            String jobID = operation.getReferenceJobId();
            if (jobID != null) {
                jobIDList.add(jobID);
            }
        }
        return jobIDList;
    }

    private void repositoryChange(RepositoryNode repNode, INode node) {
        IElementParameter param = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
        ConnectionItem connectionItem = (ConnectionItem) repNode.getObject().getProperty().getItem();
        if (param != null) {
            param.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName()).setValue(EmfComponent.REPOSITORY);
            String serviceId = connectionItem.getProperty().getId();
            String portId = ((PortRepositoryObject) repNode.getParent().getObject()).getId();
            String operationId = ((OperationRepositoryObject) repNode.getObject()).getId();
            ChangeValuesFromRepository command2 = new ChangeValuesFromRepository(node, connectionItem.getConnection(),
                    param.getName() + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), //$NON-NLS-1$
                    serviceId + " - " + portId + " - " + operationId);
            command2.execute();
        }
    }

    private void repositoryChangeToBuildIn(RepositoryNode repNode, INode node) {
        IElementParameter param = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
        ConnectionItem connectionItem = (ConnectionItem) repNode.getObject().getProperty().getItem();
        if (param != null) {
            param.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName()).setValue(EmfComponent.BUILTIN);
            ChangeValuesFromRepository command2 = new ChangeValuesFromRepository(node, connectionItem.getConnection(),
                    param.getName() + ":" + EParameterName.PROPERTY_TYPE.getName(), EmfComponent.BUILTIN); //$NON-NLS-1$
            command2.execute();
            command2 = new ChangeValuesFromRepository(node, connectionItem.getConnection(),
                    param.getName() + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), ""); //$NON-NLS-1$
            command2.execute();
        }
    }

}
