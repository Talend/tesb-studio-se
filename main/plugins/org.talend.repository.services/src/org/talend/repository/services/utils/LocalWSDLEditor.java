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
package org.talend.repository.services.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.wsdl.ui.internal.InternalWSDLMultiPageEditor;
import org.eclipse.wst.wsdl.ui.internal.actions.OpenInNewEditor;
import org.eclipse.wst.wsdl.ui.internal.adapters.WSDLBaseAdapter;
import org.eclipse.wst.wsdl.ui.internal.asd.actions.BaseSelectionAction;
import org.eclipse.wst.wsdl.ui.internal.asd.design.directedit.DirectEditSelectionTool;
import org.eclipse.wst.wsdl.ui.internal.asd.util.IOpenExternalEditorHelper;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.designer.core.DesignerPlugin;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.services.Messages;
import org.talend.repository.services.action.AssignJobAction;
import org.talend.repository.services.model.services.ServiceConnection;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.model.services.ServiceOperation;
import org.talend.repository.services.model.services.ServicePort;
import org.talend.repository.services.model.services.ServicesFactory;

@SuppressWarnings("restriction")
public class LocalWSDLEditor extends InternalWSDLMultiPageEditor {

    private ServiceItem serviceItem;

    private RepositoryNode repositoryNode;

    public LocalWSDLEditor() {
        super();
        DirectEditSelectionTool tool = new DirectEditSelectionTool() {

            @Override
            protected boolean handleButtonDown(int button) {
                try {
                    return super.handleButtonDown(button);
                } catch (NullPointerException e) {
                    // ignore NPE when click wrong area.
                    return false;
                }
            }
        };
        getEditDomain().setActiveTool(tool);
        getEditDomain().setDefaultTool(tool);
        getEditDomain().setCommandStack(new LocalCommandStack(this));
    }

    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        super.init(site, editorInput);
        if (editorInput instanceof RepositoryEditorInput) {
            RepositoryEditorInput serviceEditorInput = (RepositoryEditorInput) editorInput;
            serviceItem = (ServiceItem) serviceEditorInput.getItem();
            repositoryNode = serviceEditorInput.getRepositoryNode();
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (isEditorInputReadOnly()) {
            MessageDialog.openWarning(getSite().getShell(), Messages.WSDLFileIsReadOnly_Title,
                    Messages.WSDLFileIsReadOnly_Message);
            return;
        }
        super.doSave(monitor);
        if (null != serviceItem && null != repositoryNode) {
            // save();

            try {
                String name = "Save Service"; //$NON-NLS-1$
                RepositoryWorkUnit<Object> repositoryWorkUnit = new RepositoryWorkUnit<Object>(name, this) {

                    @Override
                    protected void run() throws LoginException, PersistenceException {
                        save();
                    }
                };
                repositoryWorkUnit.setAvoidSvnUpdate(true);
                repositoryWorkUnit.setAvoidUnloadResources(true);
                ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);
                repositoryWorkUnit.throwPersistenceExceptionIfAny();
            } catch (Exception e) {
                e.printStackTrace();
                ExceptionHandler.process(e);
            }
        }
    }

    public boolean isEditorInputReadOnly() {
        return ((RepositoryEditorInput) getEditorInput()).isReadOnly();
    }

    private void save() {
        try {
            saveModel();

            // update
            RepositoryUpdateManager.updateServices(serviceItem);

            ProxyRepositoryFactory.getInstance().save(serviceItem);

            if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
                IESBService service = (IESBService) GlobalServiceRegister.getDefault().getService(IESBService.class);
                if (service != null) {
                    service.refreshComponentView(serviceItem);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // ////////// TODO: remove this ugly patch! do correct changeset
        EList<ServicePort> servicePorts = ((ServiceConnection) serviceItem.getConnection()).getServicePort();
        for (ServicePort servicePort : servicePorts) {
            List<IRepositoryNode> portNodes = repositoryNode.getChildren();
            IRepositoryNode portNode = null;
            for (IRepositoryNode node : portNodes) {
                if (node.getObject().getLabel().equals(servicePort.getName())) {
                    portNode = node;
                }
            }
            if (portNode == null) {
                // for now, if the port has been renamed, we just lose all links (avoid an NPE for nothing)
                continue;
            }
            EList<ServiceOperation> operations = servicePort.getServiceOperation();
            for (ServiceOperation operation : operations) {
                String referenceJobId = operation.getReferenceJobId();
                if (referenceJobId != null) {
                    for (IRepositoryNode operationNode : portNode.getChildren()) {
                        if (operationNode.getObject().getLabel().startsWith(operation.getName() + '-')) {
                            IRepositoryNode jobNode = org.talend.core.repository.seeker.RepositorySeekerManager.getInstance()
                                    .searchRepoViewNode(referenceJobId, false);
                            AssignJobAction action = new AssignJobAction((RepositoryNode) operationNode);
                            action.assign(jobNode);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void saveModel() throws CoreException {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        Definition definition = WSDLUtils.getDefinition(serviceItem);

        // changed for TDI-18005
        Map<String, String> portNameIdMap = new HashMap<>();
        Map<String, EMap<String, String>> portAdditionalMap = new HashMap<>();
        Map<String, String> operNameIdMap = new HashMap<>();
        Map<String, String> operJobMap = new HashMap<>();

        EList<ServicePort> oldServicePorts = ((ServiceConnection) serviceItem.getConnection()).getServicePort();
        // get old service port item names and operation names under them
        HashMap<String, ArrayList<String>> oldPortItemNames = new HashMap<>();

        for (ServicePort servicePort : oldServicePorts) {
            // keep id
            portNameIdMap.put(servicePort.getName(), servicePort.getId());

            // keep additional infos
            portAdditionalMap.put(servicePort.getId(), servicePort.getAdditionalInfo());

            EList<ServiceOperation> operations = servicePort.getServiceOperation();
            ArrayList<String> operationNames = new ArrayList<>();
            for (ServiceOperation operation : operations) {
                operNameIdMap.put(operation.getName(), operation.getId());
                operationNames.add(operation.getLabel());
                // record assigned job
                operJobMap.put(operation.getId(), operation.getReferenceJobId());
            }
            oldPortItemNames.put(servicePort.getName(), operationNames);
        }

        ((ServiceConnection) serviceItem.getConnection()).getServicePort().clear();
        for (Object obj : definition.getAllPortTypes().values()) {
            PortType portType = (PortType) obj;
            if (portType.isUndefined()) {
                continue;
            }

            ServicePort port = ServicesFactory.eINSTANCE.createServicePort();
            String portName = portType.getQName().getLocalPart();
            port.setName(portName);
            // set port id
            String id = portNameIdMap.get(portName);
            if (id != null) {
                port.setId(id);
                // restore additional infos
                port.getAdditionalInfo().putAll(portAdditionalMap.get(id));
            } else {
                port.setId(factory.getNextId());
            }

            @SuppressWarnings("unchecked")
            List<Operation> list = portType.getOperations();
            for (Operation operation : list) {
                if (operation.isUndefined()) {
                    // means the operation has been removed already ,why ?
                    continue;
                }
                ServiceOperation serviceOperation = ServicesFactory.eINSTANCE.createServiceOperation();
                serviceOperation.setName(operation.getName());
                Iterator<String> operationIterator = operNameIdMap.keySet().iterator();
                while (operationIterator.hasNext()) {
                    String oldOperationName = operationIterator.next();
                    String operationId = operNameIdMap.get(oldOperationName);
                    if (oldOperationName.equals(operation.getName())) {
                        serviceOperation.setId(operationId);
                        // re-assign job
                        String jobId = operJobMap.get(operationId);
                        if (jobId != null) {
                            serviceOperation.setReferenceJobId(jobId);
                        }
                    }
                }
                if (serviceOperation.getId() == null || serviceOperation.getId().equals("")) {
                    serviceOperation.setId(factory.getNextId());
                }
                if (operation.getDocumentationElement() != null) {
                    serviceOperation.setDocumentation(operation.getDocumentationElement().getTextContent());
                }
                boolean hasAssignedjob = false;
                ArrayList<String> operationNames = oldPortItemNames.get(portName);
                String referenceJobId = serviceOperation.getReferenceJobId();
                if (operationNames != null && referenceJobId != null) {
                    IRepositoryViewObject repObj = null;
                    try {
                        repObj = factory.getLastVersion(referenceJobId);
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    }
                    if (repObj != null) {
                        for (String name : operationNames) {
                            if (name.equals(operation.getName() + '-' + repObj.getLabel())) {
                                serviceOperation.setLabel(name);
                                hasAssignedjob = true;
                                break;
                            }
                        }
                    }
                }
                if (!hasAssignedjob) {
                    serviceOperation.setLabel(operation.getName());
                }
                serviceOperation.setInBinding(WSDLUtils.isOperationInBinding(definition, portName, operation.getName()));
                port.getServiceOperation().add(serviceOperation);
            }
            ((ServiceConnection) serviceItem.getConnection()).getServicePort().add(port);
        }
    }

    @Override
    protected void createActions() {
        super.createActions();
        ActionRegistry registry = getActionRegistry();
        BaseSelectionAction action = new OpenInNewEditor(this) {

            @Override
            public void run() {

                if (getSelectedObjects().size() > 0) {
                    Object o = getSelectedObjects().get(0);
                    // should make this generic and be able to get the owner from a facade object
                    if (o instanceof WSDLBaseAdapter) {
                        WSDLBaseAdapter baseAdapter = (WSDLBaseAdapter) o;
                        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                        IEditorPart editorPart = workbenchWindow.getActivePage().getActiveEditor();
                        Object object = editorPart.getAdapter(org.eclipse.wst.wsdl.Definition.class);
                        if (object instanceof org.eclipse.wst.wsdl.Definition) {
                            EObject eObject = (EObject) baseAdapter.getTarget();
                            OpenOnSelectionHelper openHelper = new OpenOnSelectionHelper(
                                    (org.eclipse.wst.wsdl.Definition) object);
                            openHelper.openEditor(eObject);
                        }
                    }
                }
            }
        };
        action.setSelectionProvider(getSelectionManager());
        registry.registerAction(action);
    }

    @Override
    protected void initializeGraphicalViewer() {
        super.initializeGraphicalViewer();
        MenuManager contextMenu = getGraphicalViewer().getContextMenu();
        if (isEditorInputReadOnly()) {
            contextMenu.setVisible(false);
        }
    }

    @Override
    public boolean isFileReadOnly() {
        return super.isFileReadOnly() || isEditorInputReadOnly();
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
        if (type == IOpenExternalEditorHelper.class && isEditorInputReadOnly()) {
            return null;
        }
        return super.getAdapter(type);
    }

    @Override
    public void dispose() {
        // unlock item if necessary
        IEditorInput currentEditorInput = getEditorInput();
        if (currentEditorInput instanceof RepositoryEditorInput) {
            RepositoryEditorInput serviceEditorInput = (RepositoryEditorInput) currentEditorInput;
            Item currentItem = serviceEditorInput.getItem();
            if (currentItem != null) {
                // unlock item if no other editors open it.
                boolean openItemInOtherEditor = false;
                IEditorReference[] editorRefs = getEditorSite().getPage().getEditorReferences();
                for (IEditorReference editorRef : editorRefs) {
                    if (editorRef.getEditor(false) == this) {
                        continue;
                    }
                    try {
                        IEditorInput editorInput = editorRef.getEditorInput();
                        if (editorInput instanceof RepositoryEditorInput) {
                            Item item = ((RepositoryEditorInput) editorInput).getItem();
                            if (item == currentItem) {
                                // open this item & not this one.
                                openItemInOtherEditor = true;
                            }
                        }
                    } catch (PartInitException e) {
                        // ignore and compare others
                        ExceptionHandler.process(e);
                    }
                }
                if (!openItemInOtherEditor) {
                    try {
                        DesignerPlugin.getDefault().getProxyRepositoryFactory().unlock(currentItem);
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }

            }
        }

        super.dispose();
    }

}
