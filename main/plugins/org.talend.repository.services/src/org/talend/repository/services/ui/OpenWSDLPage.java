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
package org.talend.repository.services.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.ReferenceFileItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.metadata.managment.ui.wizard.PropertiesWizardPage;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.services.Activator;
import org.talend.repository.services.Messages;
import org.talend.repository.services.action.OpenWSDLEditorAction;
import org.talend.repository.services.action.PublishMetadataRunnable;
import org.talend.repository.services.model.services.ServiceConnection;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.model.services.ServiceOperation;
import org.talend.repository.services.model.services.ServicePort;
import org.talend.repository.services.model.services.ServicesFactory;
import org.talend.repository.services.utils.WSDLUtils;
import org.talend.repository.utils.TemplateProcessor;
import org.talend.utils.wsdl.WSDLLoader;

/**
 * hwang class global comment. Detailed comment
 */
public class OpenWSDLPage extends WizardPage {

    private static final String TEMPLATE_SERVICE_WSDL = "/resources/wsdl-template.wsdl"; //$NON-NLS-1$

    private RepositoryNode repositoryNode;

    private LabelledFileField wsdlText;

    private String path;

    private final ServiceItem item;

    private final boolean creation;

    private IPath pathToSave;

    private Button checkImport;

    private Button radioImportWsdl;

    private Button radioCreateWsdl;

    private Definition definition;

    protected OpenWSDLPage(RepositoryNode repositoryNode, IPath pathToSave, ServiceItem item, boolean creation) {
        super("OpenWSDLPage"); //$NON-NLS-1$
        this.creation = creation;
        this.pathToSave = pathToSave;
        this.item = item;
        this.repositoryNode = repositoryNode;

        this.setTitle(Messages.AssignWsdlDialog_Title);
        this.setMessage(Messages.AssignWsdlDialog_Description);
    }

    public void createControl(Composite parent) {
        Composite parentArea = new Composite(parent, SWT.NONE);
        parentArea.setLayout(new GridLayout());

        radioCreateWsdl = new Button(parentArea, SWT.RADIO);
        radioCreateWsdl.setText(Messages.AssignWsdlDialog_WsdlChoice_CreateNew);
        radioCreateWsdl.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                switchRadio();
            }
        });
        radioCreateWsdl.setSelection(true);

        radioImportWsdl = new Button(parentArea, SWT.RADIO);
        radioImportWsdl.setText(Messages.AssignWsdlDialog_WsdlChoice_ImportExistent);
        radioImportWsdl.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                switchRadio();
            }
        });

        Composite wsdlFileArea = new Composite(parentArea, SWT.NONE);
        wsdlFileArea.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        GridLayout layout = new GridLayout(3, false);
//        layout.marginLeft = 15;
//        layout.marginHeight = 0;
        wsdlFileArea.setLayout(layout);

        String[] xmlExtensions = { "*.xml;*.xsd;*.wsdl", "*.*", "*" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        wsdlText = new LabelledFileField(wsdlFileArea, Messages.AssignWsdlDialog_ExistentWsdlFilePath, xmlExtensions);
        final String initialPath = null == item || null == item.getConnection() ? "" //$NON-NLS-1$
                : ((ServiceConnection) item.getConnection()).getWSDLPath();
        wsdlText.setText(initialPath);
        wsdlText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                path = wsdlText.getText().trim();
                setPageComplete(!path.isEmpty());
            }
        });
        new Label(wsdlFileArea, SWT.NONE);
        checkImport = new Button(wsdlFileArea, SWT.CHECK);
        checkImport.setText(Messages.AssignWsdlDialog_ImportWsdlSchemas);
        checkImport.setSelection(true);

        switchRadio();

        setControl(parentArea);
    }

    private void switchRadio() {
        if (radioCreateWsdl.getSelection()) {
            wsdlText.setVisible(false);
            checkImport.setVisible(false);
            path = null;
            setPageComplete(true);
        } else {
            wsdlText.setVisible(true);
            checkImport.setVisible(true);
            path = wsdlText.getText().trim();
            setPageComplete(!path.isEmpty());
        }
    }

    /**
     * Gets the path to save the Service node. Created by Marvin Wang on May 11, 2012.
     *
     * @return
     */
    protected IPath getDestinationPath() {
        IWizardPage previousPage = this.getPreviousPage();
        if (previousPage instanceof PropertiesWizardPage) {
            PropertiesWizardPage wizardPage = (PropertiesWizardPage) previousPage;
            pathToSave = wizardPage.getDestinationPath();
        }
        return pathToSave;
    }

    public boolean finish() {
        // changed by hqzhang for TDI-19527, label=displayName
        final String label = item.getProperty().getDisplayName();
        item.getProperty().setLabel(label);

        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            public void run(final IProgressMonitor monitor) throws CoreException {
            	IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                try {
                    item.setConnection(ServicesFactory.eINSTANCE.createServiceConnection());
                    if (creation) {
                        item.getProperty().setId(factory.getNextId());
                        factory.create(item, getDestinationPath());
                        repositoryNode = new RepositoryNode(new RepositoryViewObject(item.getProperty()), repositoryNode.getParent(),
                                ENodeType.REPOSITORY_ELEMENT);
                    }

                    ((ServiceConnection) item.getConnection()).setWSDLPath(path);
                    ((ServiceConnection) item.getConnection()).getServicePort().clear();

                    final IFile fileWsdl = WSDLUtils.getWsdlFile(item);
                    final InputStream is;
                    if (null == path) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        // create new WSDL file from template
                        TemplateProcessor.processTemplate("DATA_SERVICE_WSDL",
                                Collections.singletonMap("serviceName", (Object) label),
                                baos, getClass().getResourceAsStream(TEMPLATE_SERVICE_WSDL));
                        is = new ByteArrayInputStream(baos.toByteArray());
                    } else {
                        String filenameTemplate = item.getProperty().getLabel() + '_' + item.getProperty().getVersion() + ".%d.wsdl"; //$NON-NLS-1$
                        Map<String, InputStream> wsdls = new WSDLLoader().load(path, filenameTemplate);
                        is = wsdls.remove(WSDLLoader.DEFAULT_FILENAME);
                        for (Map.Entry<String, InputStream> wsdl : wsdls.entrySet()) {
                            String filename = wsdl.getKey();
                            IFile importedWsdl = fileWsdl.getParent().getFile(new Path(filename));
                            if (!importedWsdl.exists()) {
                                importedWsdl.create(wsdl.getValue(), true, monitor);
                            } else {
                                importedWsdl.setContents(wsdl.getValue(), 0, monitor);
                            }
                            createReferenceResources(filename.substring(filename.lastIndexOf('.', filename.lastIndexOf('.') - 1) + 1));
                        }
                    }

                    // store WSDL in service
                    if (!fileWsdl.exists()) {
                        fileWsdl.create(is, true, monitor);
                    } else {
                        fileWsdl.setContents(is, 0, monitor);
                    }

                    // create reference to wsdl
                    createReferenceResources("wsdl"); //$NON-NLS-1$

                    definition = WSDLUtils.getDefinition(fileWsdl); // path
                    populateModelFromWsdl(factory, definition, item, repositoryNode);

                    factory.save(item);
                    ProxyRepositoryFactory.getInstance().saveProject(ProjectManager.getInstance().getCurrentProject());

                } catch (Exception e) {
                	//delete the node if any exception during the creation
                	if(creation){
	                	try{
	                		factory.save(item);
							factory.deleteObjectPhysical(repositoryNode.getObject());
						} catch (PersistenceException e1) {
		                    throw getCoreException("WDSL creation failed", e1);
						}
                	}
                	//throw the exception
                	if(e instanceof CoreException){
                		throw (CoreException)e;
                	}
                	if(e instanceof InvocationTargetException){
                		throw getCoreException("WDSL creation failed", e.getCause()); 
                	}
                	throw getCoreException("WDSL creation failed", e);
                } 
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            ISchedulingRule schedulingRule = workspace.getRoot();// we use the workspace scheduling rule to lock all
                                                                 // workspace modifications during the run.
            // the update of the project files need to be done in the workspace runnable to avoid all notification
            // of changes before the end of the modifications.
            workspace.run(runnable, schedulingRule, IWorkspace.AVOID_UPDATE, null);
        } catch (CoreException e) {
            MessageBoxExceptionHandler.process(e);
            return false;
        }

        // open wsdl editor
        OpenWSDLEditorAction action = new OpenWSDLEditorAction();
        action.setServiceItem(item);
        action.run();

        // import schemas if required
        if (checkImport.isVisible() && checkImport.getSelection() && null != definition) {
            PublishMetadataRunnable publishMetadataRunnable = new PublishMetadataRunnable(definition, getShell());
            try {
                getContainer().run(true, true, publishMetadataRunnable);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                String message = (null != cause.getMessage()) ? cause.getMessage() : cause.getClass().getName();
                setErrorMessage("Populate schema to repository: " + message);
                return false;
            } catch (InterruptedException e) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings({ "unchecked" })
    private static void populateModelFromWsdl(IProxyRepositoryFactory factory, Definition definition, ServiceItem serviceItem,
            RepositoryNode serviceRepositoryNode) throws CoreException {
        serviceRepositoryNode.getChildren().clear();
        ((ServiceConnection) serviceItem.getConnection()).getServicePort().clear();
        for (PortType portType : (Collection<PortType>) definition.getAllPortTypes().values()) {
            ServicePort port = ServicesFactory.eINSTANCE.createServicePort();
            port.setId(factory.getNextId());
            port.setName(portType.getQName().getLocalPart());
            for (Operation operation : (Collection<Operation>) portType.getOperations()) {
            	String operationName = operation.getName();
                ServiceOperation serviceOperation = ServicesFactory.eINSTANCE.createServiceOperation();
                serviceOperation.setId(factory.getNextId());
                RepositoryNode operationNode = new RepositoryNode(new RepositoryViewObject(serviceItem.getProperty()),
                        serviceRepositoryNode, ENodeType.REPOSITORY_ELEMENT);
                operationNode.setProperties(EProperties.LABEL, serviceItem.getProperty().getLabel());
                operationNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.SERVICESOPERATION);
				serviceOperation.setName(operationName);
                if (operation.getDocumentationElement() != null) {
                    serviceOperation.setDocumentation(operation.getDocumentationElement().getTextContent());
                }
                serviceOperation.setLabel(operationName);
                serviceOperation.setInBinding(WSDLUtils.isOperationInBinding(definition, port.getName(), operationName));
                port.getServiceOperation().add(serviceOperation);
            }
            ((ServiceConnection) serviceItem.getConnection()).getServicePort().add(port);
        }
    }

    private void createReferenceResources(String fileExtension) {
        for (Object resource : item.getReferenceResources()) {
            if (resource instanceof ReferenceFileItem && fileExtension.equals(((ReferenceFileItem) resource).getExtension())) {
                return;
            }
        }
        ReferenceFileItem referenceFileItem = PropertiesFactory.eINSTANCE.createReferenceFileItem();
        referenceFileItem.setExtension(fileExtension);
        item.getReferenceResources().add(referenceFileItem);
    }

    private static CoreException getCoreException(String message, Throwable initialException) {
        return new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
            message, initialException));
    }

}
