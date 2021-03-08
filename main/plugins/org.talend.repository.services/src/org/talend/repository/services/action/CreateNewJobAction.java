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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.relationship.RelationshipRegistryReader;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.seeker.RepositorySeekerManager;
import org.talend.core.repository.ui.actions.metadata.AbstractCreateAction;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.MultiPageTalendEditor;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.editor.cmd.CreateNodeContainerCommand;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.wizards.NewProcessWizard;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.services.IESBConstant;
import org.talend.repository.services.model.services.ServiceConnection;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.model.services.ServiceOperation;
import org.talend.repository.services.model.services.ServicePort;
import org.talend.repository.services.utils.OperationRepositoryObject;
import org.talend.repository.services.utils.PortRepositoryObject;
import org.talend.repository.services.utils.WSDLUtils;

public class CreateNewJobAction extends AbstractCreateAction {

    static final String T_ESB_PROVIDER_REQUEST = "tESBProviderRequest"; //$NON-NLS-1$

    static final String T_ESB_PROVIDER_RESPONSE = "tESBProviderResponse"; //$NON-NLS-1$

    static final String T_ESB_PROVIDER_FAULT = "tESBProviderFault"; //$NON-NLS-1$

    public CreateNewJobAction() {
        super();

        this.setText("Create New Job");
        this.setToolTipText("Create New Job");
        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PROCESS_ICON));
    }

    @Override
    protected void init(RepositoryNode node) {
        ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        if (!ERepositoryObjectType.SERVICESOPERATION.equals(nodeType)) {
            return;
        }
        boolean flag = true;
        ServiceItem serviceItem = (ServiceItem) node.getParent().getParent().getObject().getProperty().getItem();
        for (ServicePort port : ((ServiceConnection) serviceItem.getConnection()).getServicePort()) {
            for (ServiceOperation operation : port.getServiceOperation()) {
                if (operation.getLabel().equals(node.getLabel()) && operation.getReferenceJobId() != null
                        && !operation.getReferenceJobId().equals("")) {
                    flag = false;
                }
            }
        }
        setEnabled(flag);
    }

    @Override
    protected void doRun() {
        RepositoryNode node = getSelectedRepositoryNode();
        if (node == null) {
            return;
        }
        NewProcessWizard processWizard = getNewProcessWizard(node);

        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), processWizard);
        if (dlg.open() == Window.OK) {
            createNewProcess(node, processWizard.getProcess());
        }
    }

    public NewProcessWizard getNewProcessWizard() {
        return getNewProcessWizard(getSelectedRepositoryNode());
    }

    public boolean createNewProcess(NewProcessWizard processWizard) {
        return createNewProcess(getSelectedRepositoryNode(), processWizard.getProcess());
    }

    private boolean createNewProcess(RepositoryNode nodeOperation, final ProcessItem process) {
        if (process == null) {
            return false;
        }
        final RepositoryNode portNode = nodeOperation.getParent();
        ServiceItem serviceItem = (ServiceItem) portNode.getParent().getObject().getProperty().getItem();

        addToMergeRelationship(process, serviceItem);

        try {
            // Set readonly to false since created job will always be editable.
            ProcessEditorInput fileEditorInput = new ProcessEditorInput(process, false, true, false);
            IRepositoryNode repositoryNode = RepositorySeekerManager.getInstance()
                    .searchRepoViewNode(fileEditorInput.getItem().getProperty().getId());
            fileEditorInput.setRepositoryNode(repositoryNode);

            IEditorPart openEditor = getActivePage().openEditor(fileEditorInput, MultiPageTalendEditor.ID, true);
            CommandStack commandStack = (CommandStack) openEditor.getAdapter(CommandStack.class);

            final Node nodeProviderRequest = new Node(ComponentsFactoryProvider.getInstance().get(T_ESB_PROVIDER_REQUEST,
                    ComponentCategory.CATEGORY_4_DI.getName()), fileEditorInput.getLoadedProcess());

            IFile wsdlPath = WSDLUtils.getWsdlFile(serviceItem);
            Map<String, String> serviceParameters = WSDLUtils.getServiceOperationParameters(wsdlPath,
                    ((OperationRepositoryObject) nodeOperation.getObject()).getName(), portNode.getObject().getLabel());
            setProviderRequestComponentConfiguration(nodeProviderRequest, serviceParameters);

            CreateNodeContainerCommand cNcc = new CreateNodeContainerCommand(fileEditorInput.getLoadedProcess(),
                    new NodeContainer(nodeProviderRequest), new Point(3 * Node.DEFAULT_SIZE, 4 * Node.DEFAULT_SIZE));
            commandStack.execute(cNcc);

            if (!WSDLUtils.ONE_WAY.equals(serviceParameters.get(WSDLUtils.COMMUNICATION_STYLE))) {
                Node node = new Node(ComponentsFactoryProvider.getInstance().get(T_ESB_PROVIDER_RESPONSE,
                        ComponentCategory.CATEGORY_4_DI.getName()), fileEditorInput.getLoadedProcess());
                cNcc = new CreateNodeContainerCommand(fileEditorInput.getLoadedProcess(), new NodeContainer(node),
                        new Point(9 * Node.DEFAULT_SIZE, 4 * Node.DEFAULT_SIZE));
                commandStack.execute(cNcc);
            }
            String faults = serviceParameters.get(WSDLUtils.FAULTS);
            if (null != faults) {
                int horMultiplier = 15;
                for (String fault : faults.split(",")) {
                    Node node = new Node(ComponentsFactoryProvider.getInstance().get(T_ESB_PROVIDER_FAULT,
                            ComponentCategory.CATEGORY_4_DI.getName()), fileEditorInput.getLoadedProcess());
                    cNcc = new CreateNodeContainerCommand(fileEditorInput.getLoadedProcess(), new NodeContainer(node),
                            new Point(horMultiplier * Node.DEFAULT_SIZE, 4 * Node.DEFAULT_SIZE));
                    commandStack.execute(cNcc);
                    node.getElementParameter("ESB_FAULT_TITLE").setValue('\"' + fault + '\"'); //$NON-NLS-1$

                    horMultiplier += 6;
                }
            }

            ServiceConnection serviceConnection = (ServiceConnection) serviceItem.getConnection();
            final String parentPortName = portNode.getObject().getLabel();
            for (ServicePort port : serviceConnection.getServicePort()) {
                if (port.getName().equals(parentPortName)) {
                    for (ServiceOperation operation : port.getServiceOperation()) {
                        if (operation.getLabel().equals(nodeOperation.getObject().getLabel())) {
                            String jobName = process.getProperty().getLabel();
                            String jobID = process.getProperty().getId();
                            operation.setReferenceJobId(jobID);
                            operation.setLabel(operation.getName() + "-" + jobName);
                            break;
                        }
                    }
                    break;
                }
            }

            repositoryChange(nodeOperation, nodeProviderRequest);

            ProxyRepositoryFactory.getInstance().save(serviceItem);
            return true;
        } catch (PartInitException e) {
            ExceptionHandler.process(e);
        } catch (PersistenceException e) {
            MessageBoxExceptionHandler.process(e);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return false;
    }

    /**
     * Add relation from job to service, without save action
     *
     * @param processItem
     * @param serviceItem
     */
    private void addToMergeRelationship(final ProcessItem processItem, ServiceItem serviceItem) {
        Set<Relation> relationSet = new HashSet<Relation>();
        Relation addedRelation = new Relation();
        addedRelation.setId(serviceItem.getProperty().getId());
        addedRelation.setType(RelationshipItemBuilder.SERVICES_RELATION);
        addedRelation.setVersion(serviceItem.getProperty().getVersion());
        relationSet.add(addedRelation);

        Map<Relation, Set<Relation>> merge = new HashMap<Relation, Set<Relation>>();
        Relation processRelation = new Relation();
        processRelation.setId(processItem.getProperty().getId());
        processRelation.setType(RelationshipItemBuilder.JOB_RELATION);
        processRelation.setVersion(processItem.getProperty().getVersion());

        merge.put(processRelation, relationSet);

        RelationshipItemBuilder.mergeRelationship(RelationshipItemBuilder.getInstance().getCurrentProjectItemsRelations(), merge);
    }

    private NewProcessWizard getNewProcessWizard(RepositoryNode node) {
        NewProcessWizard processWizard = null;
        if (isToolbar()) {
            processWizard = new NewProcessWizard(null);
        } else {
            ItemCacheManager.clearCache();

            String operationName = ((OperationRepositoryObject) node.getObject()).getName();
            String portName = "defaultPort";
            String servicesName = "Services";
            if (node.getParent() != null) {
                portName = node.getParent().getObject().getLabel();
                if (node.getParent().getParent() != null) {
                    servicesName = node.getParent().getParent().getObject().getLabel();
                }
            }
            IPath path = new Path(servicesName).append(portName).append(operationName);
            if (RepositoryConstants.isSystemFolder(path.toString())) {
                // Not allowed to create in system folder.
                return null;
            }
            processWizard = new NewProcessWizard(path, initLabel(node));
        }
        return processWizard;
    }

    private RepositoryNode getSelectedRepositoryNode() {
        ISelection selection = getSelection();
        if (selection == null) {
            return null;
        }
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        return (RepositoryNode) obj;
    }

    private String initLabel(RepositoryNode node) {
        return node.getParent().getObject().getLabel() + '_' + ((OperationRepositoryObject) node.getObject()).getName();
    }

    public static void setProviderRequestComponentConfiguration(INode providerRequestComponent,
            Map<String, String> serviceConfiguration) {
        for (Map.Entry<String, String> property : serviceConfiguration.entrySet()) {
            String name = property.getKey();
            IElementParameter parameter = providerRequestComponent.getElementParameter(name);
            if (parameter != null) {
                parameter.setValue(property.getValue());
            }
        }
        IElementParameter param = providerRequestComponent.getElementParameter(IESBConstant.PROPERTY);
        param.getChildParameters().get(IESBConstant.PROPERTY_TYPE).setValue(EmfComponent.REPOSITORY);
    }

    private void repositoryChange(RepositoryNode nodeOperation, Node nodeProviderRequest) {
        IElementParameter param = nodeProviderRequest.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
        if (param != null) {
            param.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName()).setValue(EmfComponent.REPOSITORY);
            ConnectionItem connectionItem = (ConnectionItem) nodeOperation.getObject().getProperty().getItem();
            String serviceId = connectionItem.getProperty().getId();
            String portId = ((PortRepositoryObject) nodeOperation.getParent().getObject()).getId();
            String operationId = ((OperationRepositoryObject) nodeOperation.getObject()).getId();
            ChangeValuesFromRepository command2 = new ChangeValuesFromRepository(nodeProviderRequest,
                    connectionItem.getConnection(), param.getName() + ':' + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(),
                    serviceId + " - " + portId + " - " + operationId); //$NON-NLS-1$
            command2.execute();
        }
    }
}
