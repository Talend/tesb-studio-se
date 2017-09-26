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
package org.talend.repository.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.utils.IXSDPopulationUtil;
import org.talend.core.utils.KeywordsValidator;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.services.action.CreateNewJobAction;
import org.talend.repository.services.model.services.ServiceConnection;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.model.services.ServiceOperation;
import org.talend.repository.services.model.services.ServicePort;
import org.talend.repository.services.model.services.ServicesPackage;
import org.talend.repository.services.utils.ESBRepositoryNodeType;
import org.talend.repository.services.utils.LocalWSDLEditor;
import org.talend.repository.services.utils.OperationRepositoryObject;
import org.talend.repository.services.utils.PortRepositoryObject;
import org.talend.repository.services.utils.WSDLPopulationUtil;
import org.talend.repository.services.utils.WSDLUtils;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class ESBService implements IESBService {

    // public AbstractMetadataObject getServicesOperation(Connection connection, String operationName) {
    // List<ServiceOperation> list = new ArrayList<ServiceOperation>();
    // if (connection instanceof ServiceConnection) {
    // ServiceConnection serConnection = (ServiceConnection) connection;
    // EList<ServicePort> serPort = serConnection.getServicePort();
    // for (ServicePort port : serPort) {
    // list.addAll(port.getServiceOperation());
    // }
    // }
    // for (ServiceOperation ope : list) {
    // if (ope.getLabel().equals(operationName)) {
    // return ope;
    // }
    // }
    // return null;
    // }

    // public void changeOperationLabel(RepositoryNode newNode, INode node, Connection connection) {
    // if (!(connection instanceof ServiceConnection)) {
    // return;
    // }
    // ServiceConnection serConn = (ServiceConnection) connection;
    // changeOldOperationLabel(serConn, node);
    // changenewOperationLabel(newNode, node, serConn);
    // }

    private void changeOldOperationLabel(RepositoryNode topParent, INode node, ServiceOperation newOperation) {
        // here should be all the ports, not just ports of one connection
        List<IRepositoryNode> nodeList = topParent.getChildren();
        IElementParameter elePara = node.getElementParameter("PROPERTY:" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        if (elePara == null) {
            return;
        }
        ServiceConnection serConn = null;
        ServiceItem servicesItem = null;
        String paraValue = (String) elePara.getValue();
        if (paraValue == null || "".equals(paraValue)) {
            return;
        }
        String connID = null;
        if (paraValue.contains(" - ")) {
            connID = paraValue.split(" - ")[0];
        } else {
            connID = paraValue;
        }
        for (IRepositoryNode repNode : nodeList) {
            String id = repNode.getObject().getProperty().getId();
            if (id.equals(connID)) {
                servicesItem = (ServiceItem) repNode.getObject().getProperty().getItem();
                serConn = (ServiceConnection) servicesItem.getConnection();
                break;
            }
        }
        if (serConn == null) {
            return;
        }
        EList<ServicePort> portList = serConn.getServicePort();

        IElementParameter portPara = node.getElementParameter(WSDLUtils.PORT_NAME);
        IElementParameter opePara = node.getElementParameter(WSDLUtils.OPERATION_NAME);
        if (portPara != null && opePara != null) {
            String portValue = (String) portPara.getValue();
            String opeValue = (String) opePara.getValue();
            if (portValue != null && !"".equals(portValue) && opeValue != null && !"".equals(opeValue)) {
                out: for (ServicePort port : portList) {
                    if (port.getName().equals(portValue)) {
                        for (ServiceOperation ope : port.getServiceOperation()) {
                            if (ope.getName().equals(opeValue) && newOperation != null
                                    && !ope.getId().equals(newOperation.getId())) {
                                ope.setLabel(opeValue);
                                ope.setReferenceJobId(null);
                                if (servicesItem != null) {
                                    IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                                    try {
                                        factory.save(servicesItem);
                                    } catch (PersistenceException e) {
                                        ExceptionHandler.process(e);
                                    }
                                }
                                break out;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getWsdlFilePath(Item item) {
        if (item != null && item instanceof ServiceItem) {
            ServiceItem si = (ServiceItem) item;
            IFile wsdlFile = WSDLUtils.getWsdlFile(si);
            if (wsdlFile != null) {
                return wsdlFile.getLocation().toPortableString();
            }
        }
        return null;
    }

    // private void changenewOperationLabel(RepositoryNode newNode, INode node, ServiceConnection serConn) {
    // String operationName = newNode.getObject().getLabel();
    // String parentPortName = newNode.getParent().getObject().getLabel();
    //
    // String wsdlPath = serConn.getWSDLPath();
    // try {
    // Map<String, String> serviceParameters = WSDLUtils.getServiceParameters(wsdlPath);
    // IRepositoryViewObject newObj = newNode.getObject();
    // if (newObj instanceof OperationRepositoryObject) {
    // ServiceOperation newOpe = (ServiceOperation) ((OperationRepositoryObject) newObj).getAbstractMetadataObject();
    //
    // IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
    //
    // if (newOpe.getReferenceJobId() != null) {
    // changeOtherJobSchemaValue(factory, newOpe, serConn);
    // MessageDialog.openWarning(new Shell(), "warning",
    // "This other job which based on the Operation will be unset!");
    // }
    //
    // IEditorPart activeEditor =
    // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    // IEditorInput input = activeEditor.getEditorInput();
    // if (input instanceof ProcessEditorInput) {
    // Item jobItem = ((ProcessEditorInput) input).getItem();
    // String jobID = jobItem.getProperty().getId();
    // String jobName = jobItem.getProperty().getLabel();
    //
    // newOpe.setReferenceJobId(jobID);
    // newOpe.setLabel(newOpe.getName() + "-" + jobName);
    //
    // serviceParameters.put(WSDLUtils.PORT_NAME, parentPortName);
    // serviceParameters.put(WSDLUtils.OPERATION_NAME, operationName);
    //
    // CreateNewJobAction.setProviderRequestComponentConfiguration(node, serviceParameters);
    //
    // try {
    // factory.save(jobItem);
    // } catch (PersistenceException e) {
    // e.printStackTrace();
    // }
    // try {
    // factory.save(newNode.getParent().getParent().getObject().getProperty().getItem());
    // } catch (PersistenceException e) {
    // e.printStackTrace();
    // }
    // RepositoryManager.refreshSavedNode(newNode);
    // }
    // }
    //
    // } catch (CoreException e1) {
    // ExceptionHandler.process(e1);
    // } catch (PersistenceException e) {
    // ExceptionHandler.process(e);
    // }
    // }

    private void changeOtherJobSchemaValue(IProxyRepositoryFactory factory, ServiceOperation newOpe, /*
                                                                                                      * ServiceConnection
                                                                                                      * serConn,
                                                                                                      */
            RepositoryNode selectNode) throws PersistenceException, CoreException {
        IRepositoryViewObject jobObj = factory.getLastVersion(newOpe.getReferenceJobId());
        if (jobObj == null) {
            return;
        }
        ProcessItem processItem = (ProcessItem) jobObj.getProperty().getItem();

        IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
        boolean foundInOpen = false;

        IProcess2 process = null;
        IEditorReference[] reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
        List<IProcess2> processes = RepositoryPlugin.getDefault().getDesignerCoreService().getOpenedProcess(reference);
        for (IProcess2 processOpen : processes) {
            if (processOpen.getProperty().getItem() == processItem) {
                foundInOpen = true;
                process = processOpen;
                break;
            }
        }
        if (!foundInOpen) {
            IProcess proc = service.getProcessFromProcessItem(processItem);
            if (proc instanceof IProcess2) {
                process = (IProcess2) proc;
            }
        }

        newOpe.setReferenceJobId(null);
        newOpe.setLabel(newOpe.getName());

        if (process != null) {
            List<? extends INode> nodelist = process.getGraphicalNodes();
            for (INode node : nodelist) {
                if (node.getComponent().getName().equals("tESBProviderRequest")) {
                    repositoryChange(selectNode, node, process);
                    break;
                }
            }
            try {
                if (!foundInOpen) {
                    processItem.setProcess(process.saveXmlFile());
                    factory.save(processItem);
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }
        }

        // ProcessType process = item.getProcess();
        // EList<NodeType> nodeList = process.getNode();
        //
        // for (NodeType node : nodeList) {
        // EList parameters = node.getElementParameter();
        // for (Object paramObj : parameters) {
        // ElementParameterType param = (ElementParameterType) paramObj;
        // String name = param.getName();
        // if (name.equals(WSDLUtils.OPERATION_NAME)) {
        // if (!newOpe.getName().equals(param.getValue())) {
        // break;
        // }
        // param.setValue(null);
        // }
        // if (name.equals("SCHEMA:SCHEMA_TYPE")) {
        // param.setValue("BUILT_IN");
        // break;
        // }
        //
        // }
        //
        // }
        // factory.save(item);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.IESBService#getServicesType()
     */
    @Override
    public ERepositoryObjectType getServicesType() {
        return ESBRepositoryNodeType.SERVICES;
    }

    @Override
    public String getServiceLabel(Item item, String linkedRepository) {
        String serviceName = item.getProperty().getLabel();
        if (item instanceof ServiceItem) {
            String[] ids = linkedRepository.split(" - ");
            ServiceConnection serConn = (ServiceConnection) ((ServiceItem) item).getConnection();
            if (ids.length == 3) {
                String portName = "";
                String operationName = "";
                EList<ServicePort> portList = serConn.getServicePort();
                out: for (ServicePort port : portList) {
                    if (port.getId().equals(ids[1])) {
                        portName = port.getName();
                        EList<ServiceOperation> opeList = port.getServiceOperation();
                        for (ServiceOperation ope : opeList) {
                            if (ope.getId().equals(ids[2])) {
                                operationName = ope.getName();
                                return serviceName + " - " + portName + " - " + operationName;
                            }
                        }
                    }
                }
            }

        }
        return serviceName;
    }

    @Override
    public void updateOperation(INode node, String linkedRepository, RepositoryNode selectNode) {
        String[] ids = linkedRepository.split(" - ");
        if (ids.length == 3) {
            try {
                IRepositoryViewObject reViewObject = ProxyRepositoryFactory.getInstance().getLastVersion(ids[0]);
                ServiceItem servicesItem = (ServiceItem) reViewObject.getProperty().getItem();
                ServiceConnection serConn = (ServiceConnection) servicesItem.getConnection();

                String portName = "";
                EList<ServicePort> portList = serConn.getServicePort();
                ServiceOperation operation = null;
                for (ServicePort port : portList) {
                    if (port.getId().equals(ids[1])) {
                        portName = port.getName();
                        EList<ServiceOperation> opeList = port.getServiceOperation();
                        for (ServiceOperation ope : opeList) {
                            if (ope.getId().equals(ids[2])) {
                                operation = ope;
                                break;
                            }
                        }
                        break;
                    }
                }

                if (operation == null) {
                    return;
                }

                RepositoryNode topParent = getServicesTopNode(selectNode);
                changeOldOperationLabel(topParent, node, operation);

                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

                IProcess2 process = (IProcess2) RepositoryPlugin.getDefault().getDesignerCoreService().getCurrentProcess();
                Item jobItem = process.getProperty().getItem();
                String jobID = jobItem.getProperty().getId();
                String jobName = jobItem.getProperty().getLabel();

                if (operation.getReferenceJobId() != null && !operation.getReferenceJobId().equals(jobID)) {
                    changeOtherJobSchemaValue(factory, operation, /* serConn, */selectNode);
                    MessageDialog.openWarning(new Shell(), Messages.ESBService_DisconnectWarningTitle,
                            Messages.ESBService_DisconnectWarningMsg);
                }

                operation.setReferenceJobId(jobID);
                operation.setLabel(operation.getName() + '-' + jobName);

                IFile wsdlPath = WSDLUtils.getWsdlFile(selectNode);
                Map<String, String> serviceParameters = WSDLUtils.getServiceOperationParameters(wsdlPath, operation.getName(),
                        portName);
                CreateNewJobAction.setProviderRequestComponentConfiguration(node, serviceParameters);

                factory.save(servicesItem);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            } catch (CoreException e) {
                ExceptionHandler.process(e);
            }

        }
    }

    private void repositoryChange(RepositoryNode repNode, INode node, IProcess2 process) {
        IElementParameter param = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
        ConnectionItem connectionItem = (ConnectionItem) repNode.getObject().getProperty().getItem();
        if (param != null) {
            param.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName()).setValue(EmfComponent.REPOSITORY);
            param.getChildParameters().get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName()).setValue("");
            connectionItem.getProperty().getId();
            ((PortRepositoryObject) repNode.getParent().getObject()).getId();
            ((OperationRepositoryObject) repNode.getObject()).getId();
            ChangeValuesFromRepository command2 = new ChangeValuesFromRepository(node, null, param.getName()
                    + ":" + EParameterName.PROPERTY_TYPE.getName(), "BUILT_IN"); //$NON-NLS-1$
            IEditorPart editor = process.getEditor();
            if (editor == null) {
                command2.execute();
            } else {
                ((AbstractMultiPageTalendEditor) editor).getTalendEditor().getCommandStack().execute(command2);
            }
        }
    }

    @Override
    public Object getValue(Item connItem, String value, INode node) {
        if (connItem instanceof ServiceItem) {
            ServiceItem serviceItem = ((ServiceItem) connItem);
            if (WSDLUtils.ENDPOINT_URI.equals(value) || WSDLUtils.OPERATION_NAME.equals(value)
                    || WSDLUtils.OPERATION_NS.equals(value) || WSDLUtils.PORT_NAME.equals(value)
                    || WSDLUtils.PORT_NS.equals(value) || WSDLUtils.SERVICE_NAME.equals(value)
                    || WSDLUtils.SERVICE_NS.equals(value) || WSDLUtils.WSDL_LOCATION.equals(value)) {

                String wsdlPortTypeName = null;
                String wsdlOperationName = null;

                // find operation that job is bind to
                String processId = node.getProcess().getId();
                ServiceConnection connection = (ServiceConnection) serviceItem.getConnection();
                for (ServicePort port : connection.getServicePort()) {
                    for (ServiceOperation operation : port.getServiceOperation()) {
                        if (processId.equals(operation.getReferenceJobId())) {
                            wsdlOperationName = operation.getName();
                            wsdlPortTypeName = port.getName();
                            break;
                        }
                    }
                    if (null != wsdlOperationName) {
                        break;
                    }
                }

                if (null == wsdlOperationName) {
                    // job is not bind to any data service operation -> no need any updates
                    return null;
                }

                if (WSDLUtils.OPERATION_NAME.equals(value)) {
                    return wsdlOperationName;
                }

                IFile wsdl = WSDLUtils.getWsdlFile(serviceItem);
                if (WSDLUtils.WSDL_LOCATION.equals(value)) {
                    return wsdl.getLocation().toPortableString();
                }

                try {
                    return WSDLUtils.getServiceOperationParameters(wsdl, wsdlOperationName, wsdlPortTypeName).get(value);
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        return null;
    }

    private RepositoryNode getServicesTopNode(RepositoryNode node) {
        RepositoryNode parent = node.getParent();
        if (parent.getParent() instanceof ProjectRepositoryNode) {
            return parent;
        }
        parent = getServicesTopNode(parent);
        return parent;
    }

    /**
     * When services connection is renamed, refresh the connection label in the component view of job.
     * 
     * @param item
     */
    @Override
    public void refreshComponentView(Item item) {
        try {
            IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
            IEditorReference[] editors = activePage.getEditorReferences();
            for (IEditorReference er : editors) {
                IEditorPart part = er.getEditor(false);
                if (part instanceof AbstractMultiPageTalendEditor) {
                    AbstractMultiPageTalendEditor editor = (AbstractMultiPageTalendEditor) part;
                    CommandStack stack = (CommandStack) editor.getTalendEditor().getAdapter(CommandStack.class);
                    if (stack != null) {
                        IProcess process = editor.getProcess();
                        for (final INode processNode : process.getGraphicalNodes()) {
                            if (processNode instanceof Node) {
                                checkRepository((Node) processNode, item, stack);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    private void checkRepository(final Node node, Item item, CommandStack stack) {
        final String updataComponentParamName = EParameterName.UPDATE_COMPONENTS.getName();
        final List<IElementParameter> repositoryParam = new ArrayList<IElementParameter>();

        for (IElementParameter param : node.getElementParameters()) {
            if (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)) {
                String value = (String) param.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName()).getValue();

                if (value.equals(EmfComponent.REPOSITORY)) {
                    IElementParameter schema = param.getChildParameters().get(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
                    if (schema != null && schema.getValue() != null) {
                        String[] names = ((String) schema.getValue()).split(" - "); //$NON-NLS-1$
                        if (names.length > 0 && names[0].equals(item.getProperty().getId())) {
                            repositoryParam.add(schema);
                        }
                    }
                }

            } else if (param.getFieldType().equals(EParameterFieldType.PROPERTY_TYPE)) {
                Object value = param.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName()).getValue();
                if (value.equals(EmfComponent.REPOSITORY)) {
                    IElementParameter property = param.getChildParameters()
                            .get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                    if (property != null && property.getValue() != null) {
                        String proValue = (String) property.getValue();
                        if (proValue != null && proValue.contains(" - ")) {
                            proValue = proValue.split(" - ")[0];
                        }
                        if (proValue.equals(item.getProperty().getId())) {
                            repositoryParam.add(property);
                        }

                    }
                }
            }
        }

        if (repositoryParam.isEmpty()) {
            return;
        }

        stack.execute(new Command() {

            @Override
            public void execute() {
                node.setPropertyValue(updataComponentParamName, Boolean.TRUE);
                for (IElementParameter param : repositoryParam) {
                    // force to reload label
                    param.setListItemsDisplayName(new String[0]);
                    param.setListItemsValue(new String[0]);
                }
            }

        });
    }

    @Override
    public void refreshOperationLabel(String jobID) {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        try {
            List<IRepositoryViewObject> repList = factory.getAll(getServicesType());
            for (IRepositoryViewObject obj : repList) {
                ServiceItem item = (ServiceItem) obj.getProperty().getItem();
                ServiceConnection conn = (ServiceConnection) item.getConnection();
                for (ServicePort port : conn.getServicePort()) {
                    for (ServiceOperation operation : port.getServiceOperation()) {
                        if (operation.getReferenceJobId() != null && operation.getReferenceJobId().endsWith(jobID)) {
                            operation.setLabel(operation.getName());
                            operation.setReferenceJobId(null);
                            factory.save(item);
                            return;
                        }
                    }
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    public StringBuffer getAllTheJObNames(IRepositoryNode jobObject) {
        StringBuffer jobNames = null;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        List<IRepositoryNode> jobList = new ArrayList<IRepositoryNode>();
        if (jobObject.getObjectType() == ERepositoryObjectType.PROCESS) {
            jobList.add(jobObject);
        } else if (jobObject.getObjectType() == ERepositoryObjectType.FOLDER) {
            jobList = getJobObject(jobObject);
        }

        try {
            List<IRepositoryViewObject> repList = factory.getAll(getServicesType());
            for (IRepositoryNode node : jobList) {
                ERepositoryObjectType repositoryObjectType = node.getObjectType();
                if (repositoryObjectType != ERepositoryObjectType.PROCESS) {
                    continue;
                }
                String jobID = node.getObject().getProperty().getId();
                for (IRepositoryViewObject obj : repList) {
                    ServiceItem item = (ServiceItem) obj.getProperty().getItem();
                    ServiceConnection conn = (ServiceConnection) item.getConnection();
                    middle: for (ServicePort port : conn.getServicePort()) {
                        for (ServiceOperation operation : port.getServiceOperation()) {
                            if (operation.getReferenceJobId() != null && operation.getReferenceJobId().endsWith(jobID)) {
                                if (jobNames == null) {
                                    jobNames = new StringBuffer(node.getObject().getProperty().getLabel());
                                } else {
                                    jobNames.append(",");
                                    jobNames.append(node.getObject().getProperty().getLabel());
                                }
                                break middle;
                            }
                        }
                    }
                }
            }

        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return jobNames;
    }

    @Override
    public void editJobName(String originaleObjectLabel, String newLabel) {
        IProxyRepositoryFactory proxyRepositoryFactory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        Project project = ProjectManager.getInstance().getCurrentProject();
        List<IRepositoryViewObject> service = null;
        try {
            service = proxyRepositoryFactory.getAll(project, ESBRepositoryNodeType.SERVICES, true);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        if (service != null && service.size() > 0) {
            for (IRepositoryViewObject Object : service) {
                boolean flag = false;
                ServiceItem item = (ServiceItem) Object.getProperty().getItem();
                ServiceConnection serviceConnection = (ServiceConnection) item.getConnection();
                List<ServicePort> servicePorts = serviceConnection.getServicePort();
                for (ServicePort port : servicePorts) {
                    List<ServiceOperation> serviceOperations = port.getServiceOperation();
                    for (ServiceOperation operation : serviceOperations) {
                        String originaleItemLabel = operation.getLabel();
                        if (originaleItemLabel.contains("-")) {
                            String[] array = originaleItemLabel.split("-");
                            if (originaleObjectLabel.equals(array[1])) {
                                operation.setLabel(array[0] + "-" + newLabel);
                                flag = true;
                            }
                        }
                    }
                }
                if (flag) {
                    try {
                        proxyRepositoryFactory.save(item);
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
        // RepositoryManager.refresh(ESBRepositoryNodeType.SERVICES);
    }

    private List<IRepositoryNode> getJobObject(IRepositoryNode folderObj) {
        List<IRepositoryNode> objList = new ArrayList<IRepositoryNode>();
        for (IRepositoryNode child : folderObj.getChildren()) {
            ERepositoryObjectType repositoryObjectType = child.getObjectType();
            if (repositoryObjectType == ERepositoryObjectType.PROCESS) {
                objList.add(child);
            } else if (repositoryObjectType == ERepositoryObjectType.FOLDER) {
                objList.addAll(getJobObject(child));
            }
        }
        return objList;
    }

    @Override
    public void deleteOldRelation(String jobID) {
        boolean portBreak = false;
        boolean serviceBreak = false;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        try {
            for (IRepositoryViewObject viewObject : factory.getAll(ERepositoryObjectType.SERVICESOPERATION)) {
                ServiceItem serviceItem = (ServiceItem) viewObject.getProperty().getItem();
                ServiceConnection serviceConnection = (ServiceConnection) serviceItem.getConnection();
                List<ServicePort> ports = serviceConnection.getServicePort();
                for (ServicePort port : ports) {
                    List<ServiceOperation> operations = port.getServiceOperation();
                    for (ServiceOperation operation : operations) {
                        String referenceJobId = operation.getReferenceJobId();
                        if (jobID.equals(referenceJobId)) {
                            operation.setLabel(operation.getName());
                            operation.setReferenceJobId(null);
                            portBreak = true;
                            break;
                        }
                    }
                    if (portBreak) {
                        serviceBreak = true;
                        break;
                    }
                }
                if (serviceBreak) {
                    factory.save(serviceItem, null);
                    break;
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    public boolean isServiceItem(int classifierID) {
        return classifierID == ServicesPackage.SERVICE_ITEM;
    }

    @Override
    public void copyDataServiceRelateJob(Item newItem) {
        if (newItem instanceof ServiceItem) {
            ServiceItem serviceItem = (ServiceItem) newItem;
            ServiceConnection serviceConnection = (ServiceConnection) serviceItem.getConnection();
            List<ServicePort> ports = serviceConnection.getServicePort();
            for (ServicePort port : ports) {
                List<ServiceOperation> operations = port.getServiceOperation();
                for (ServiceOperation operation : operations) {
                    String referenceJobId = operation.getReferenceJobId();
                    if (referenceJobId != null && !referenceJobId.equals("")) {
                        IRepositoryViewObject jobObj = null;
                        try {
                            jobObj = ProxyRepositoryFactory.getInstance().getLastVersion(referenceJobId);
                        } catch (PersistenceException e) {
                            ExceptionHandler.process(e);
                        }
                        if (jobObj == null) {
                            continue;
                        }
                        ProcessItem processItem = (ProcessItem) jobObj.getProperty().getItem();
                        String initNameValue = "Copy_of_" + processItem.getProperty().getLabel();
                        final IPath path = RepositoryNodeUtilities.getPath(processItem.getProperty().getId());
                        String jobNameValue = null;

                        try {
                            jobNameValue = getDuplicateName(RepositoryNodeUtilities.getRepositoryNode(jobObj), initNameValue);
                        } catch (BusinessException e) {
                            jobNameValue = ""; //$NON-NLS-1$
                        }

                        Item newProcessItem = copyJobForService(processItem, path, jobNameValue);

                        String operationLabel = operation.getLabel();
                        if (operationLabel.contains("-")) {
                            String[] array = operationLabel.split("-");
                            operation.setLabel(array[0] + "-" + jobNameValue);
                        }
                        // update nodes in newProcessItem
                        updateNodesInNewProcessItem(newProcessItem, serviceItem, port, operation);

                        operation.setReferenceJobId(newProcessItem.getProperty().getId());
                        try {
                            ProxyRepositoryFactory.getInstance().save(serviceItem);
                        } catch (PersistenceException e) {
                            ExceptionHandler.process(e);
                        }
                    }
                }
            }
        }
    }

    /**
     * To fix [TESB-6072], tESBProviderRequest_x in job need to be update to binding to the new service.
     * 
     * @param newProcessItem The cloned job process item.
     * @param serviceItem The cloned service item.
     * @param port
     * @param operation
     * @return
     */
    private void updateNodesInNewProcessItem(Item newProcessItem, ServiceItem serviceItem, ServicePort port,
            ServiceOperation operation) {
        ProcessType process = ((ProcessItem) newProcessItem).getProcess();
        for (Object o : process.getNode()) {
            if (o instanceof NodeType) {
                NodeType node = (NodeType) o;
                if (node.getComponentName().equals("tESBProviderRequest")) {
                    EList elementParameter = node.getElementParameter();
                    for (Object param : elementParameter) {
                        ElementParameterType paramType = (ElementParameterType) param;
                        if (paramType.getName().equals("PROPERTY:REPOSITORY_PROPERTY_TYPE")) {
                            // value is SERVICE_ID - PORT_ID - OPERATION_ID
                            StringBuilder sb = new StringBuilder(serviceItem.getProperty().getId());
                            sb.append(" - ");
                            sb.append(port.getId());
                            sb.append(" - ");
                            sb.append(operation.getId());
                            paramType.setValue(sb.toString());

                        }
                    }
                }
            }
        }
        try {
            ProxyRepositoryFactory.getInstance().save(newProcessItem, true);
        } catch (PersistenceException e1) {
            ExceptionHandler.process(e1);
        }
    }

    private Item copyJobForService(final Item item, final IPath path, final String newName) {
        try {
            final Item newItem = ProxyRepositoryFactory.getInstance().copy(item, path, newName);
            if (newItem instanceof ConnectionItem) {
                Connection connection = ((ConnectionItem) newItem).getConnection();
                if (connection != null) {
                    connection.setLabel(newName);
                    connection.setName(newName);
                    connection.getSupplierDependency().clear();
                }
            }
            ProxyRepositoryFactory.getInstance().save(newItem);
            return newItem;
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        } catch (BusinessException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    public String getDuplicateName(RepositoryNode node, String value) throws BusinessException {

        if (validJobName(node, value) == null) {
            return value;
        } else {
            char j = 'a';
            String temp = value;
            while (validJobName(node, temp) != null) {
                if (j > 'z') {
                    throw new BusinessException(Messages.ESBService_cannotGenerateItem);
                }
                temp = value + "_" + (j++) + ""; //$NON-NLS-1$ //$NON-NLS-2$
            }
            return temp;
        }
    }

    private String validJobName(RepositoryNode node, String itemName) {
        if (!isValid(node, itemName)) {
            return "DuplicateAction.NameFormatError";
        }
        IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        IProxyRepositoryFactory repositoryFactory = service.getProxyRepositoryFactory();
        if (itemName.length() == 0) {
            return "DuplicateAction.NameEmptyError"; //$NON-NLS-1$
        } else if (!Pattern.matches(RepositoryConstants.getPattern(ESBRepositoryNodeType.SERVICES), itemName)) {
            /*
             * maybe Messages.getString("PropertiesWizardPage.KeywordsError")
             */
            return "DuplicateAction.NameFormatError"; //$NON-NLS-1$
        } else {
            final Item testNewItem = createNewItem(node);
            try {
                if (testNewItem != null && !repositoryFactory.isNameAvailable(testNewItem, itemName)) {
                    return "DuplicateAction.ItemExistsError"; //$NON-NLS-1$
                }
            } catch (PersistenceException e) {
                return "DuplicateAction.ItemExistsError"; //$NON-NLS-1$
            }
            // see bug 0004157: Using specific name for (main) tream
            if (isKeyword(itemName, node)) {
                return "DuplicateAction.KeywordsError"; //$NON-NLS-1$
            }
        }

        return null;
    }

    private boolean isValid(RepositoryNode node, String str) {
        String namePattern = "^\\w+$";
        Object contentType = node.getContentType();
        if (contentType == null) {
            contentType = node.getProperties(EProperties.CONTENT_TYPE);
        }
        if (contentType != null && contentType instanceof ERepositoryObjectType) {
            String tmp = ((ERepositoryObjectType) contentType).getNamePattern();
            if (tmp != null) {
                namePattern = tmp;
            }
        }
        Pattern pattern = Pattern.compile(namePattern);
        return pattern.matcher(str).matches();
    }

    private Item createNewItem(IRepositoryNode sourceNode) {
        Item item = null;
        if (sourceNode.getObjectType() == ERepositoryObjectType.PROCESS) {
            item = PropertiesFactory.eINSTANCE.createProcessItem();
            item.setProperty(PropertiesFactory.eINSTANCE.createProperty());
        }
        return item;
    }

    private boolean isKeyword(String itemName, RepositoryNode sourceNode) {
        ERepositoryObjectType itemType = sourceNode.getObjectType();
        ERepositoryObjectType[] types = { ERepositoryObjectType.PROCESS, ERepositoryObjectType.ROUTINES,
                ERepositoryObjectType.JOB_DOC, ERepositoryObjectType.JOBLET, ERepositoryObjectType.JOBLET_DOC,
                ERepositoryObjectType.JOB_SCRIPT };
        List<ERepositoryObjectType> arraysList = Arrays.asList(types);
        List<ERepositoryObjectType> typeList = new ArrayList<ERepositoryObjectType>();
        addExtensionRepositoryNodes(typeList);
        typeList.addAll(arraysList);
        if (typeList.contains(itemType)) {
            return KeywordsValidator.isKeyword(itemName);
        }
        return false;
    }

    private void addExtensionRepositoryNodes(List<ERepositoryObjectType> arraysList) {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] configurationElements = registry
                .getConfigurationElementsFor("org.talend.core.repository.repository_node_provider");
        for (IConfigurationElement element : configurationElements) {
            String type = element.getAttribute("type");
            ERepositoryObjectType repositoryNodeType = ERepositoryObjectType.valueOf(ERepositoryObjectType.class, type);
            if (repositoryNodeType != null) {
                arraysList.add(repositoryNodeType);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.IESBService#getXSDPopulationUtil()
     */
    @Override
    public IXSDPopulationUtil getXSDPopulationUtil() {
        return new WSDLPopulationUtil();
    }

    @Override
    public boolean isWSDLEditor(IWorkbenchPart part) {
        return part instanceof LocalWSDLEditor;
    }

    @Override
    public Item getWSDLEditorItem(IWorkbenchPart part) {
        if (part instanceof LocalWSDLEditor) {
            return ((LocalWSDLEditor) part).getServiceItem();
        }
        return null;
    }

    @Override
    public boolean executeCommand(IEditorPart editorPart, Object cmd) {
        if (editorPart instanceof LocalWSDLEditor && cmd instanceof Command) {
            CommandStack commandStack = (CommandStack) editorPart.getAdapter(CommandStack.class);
            editorPart.getEditorSite().getShell().getDisplay().syncExec(new Runnable() {
                
                @Override
                public void run() {
                    commandStack.execute((Command) cmd);
                }
                
            });
            return true;
        }
        return false;
    }
}
