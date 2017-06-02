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
package org.talend.repository.services.action;

import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.Item;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.seeker.RepositorySeekerManager;
import org.talend.core.repository.ui.actions.metadata.AbstractCreateAction;
import org.talend.core.ui.branding.IBrandingConfiguration;
import org.talend.designer.core.DesignerPlugin;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.services.model.services.ServiceItem;
import org.talend.repository.services.model.services.util.EServiceCoreImage;
import org.talend.repository.services.utils.ESBRepositoryNodeType;
import org.talend.repository.services.utils.WSDLUtils;

public class OpenWSDLEditorAction extends AbstractCreateAction implements IIntroAction {

    private final static String EDITOR_ID = "org.talend.repository.services.utils.LocalWSDLEditor";

    private final static String PERSPECTIVE_ID = IBrandingConfiguration.PERSPECTIVE_DI_ID;

    private ServiceItem serviceItem;

    public OpenWSDLEditorAction() {
        this.setText("Open WSDL Editor");
        this.setToolTipText("Open WSDL Editor");

        this.setImageDescriptor(ImageProvider.getImageDesc(EServiceCoreImage.SERVICE_ICON));

        // avoid NPE at save
        setAvoidUnloadResources(true);
    }

    @Override
    protected void init(RepositoryNode node) {
        // set to true for service node and service port node
        final boolean isServicesNode = ESBRepositoryNodeType.SERVICES == node.getObjectType()
                || ESBRepositoryNodeType.SERVICEPORT == node.getObjectType();
        setEnabled(isServicesNode);
        // anyway initialize for double-click
        if (isServicesNode && null != node.getObject()) {
            Item item = node.getObject().getProperty().getItem();
            if (item instanceof ServiceItem) {
                serviceItem = (ServiceItem) item;
            }
        }
    }

    @Override
    public Class<?> getClassForDoubleClick() {
        return ServiceItem.class;
    }

    @Override
    protected void doRun() {
        ServiceEditorInput editorInput = new ServiceEditorInput(WSDLUtils.getWsdlFile(serviceItem), serviceItem);
        editorInput.setRepositoryNode(repositoryNode);
        editorInput
                .setReadOnly(!DesignerPlugin.getDefault().getProxyRepositoryFactory().isEditableAndLockIfPossible(serviceItem));
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, EDITOR_ID, true);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    public void setServiceItem(ServiceItem serviceItem) {
        this.serviceItem = serviceItem;
    }

    public void run(IIntroSite site, Properties params) {
        PlatformUI.getWorkbench().getIntroManager().closeIntro(PlatformUI.getWorkbench().getIntroManager().getIntro());

        IPerspectiveDescriptor currentPerspective = site.getPage().getPerspective();
        if (!PERSPECTIVE_ID.equals(currentPerspective.getId())) {
            // show required perspective
            IWorkbenchWindow workbenchWindow = site.getWorkbenchWindow();
            try {
                workbenchWindow.getWorkbench().showPerspective(PERSPECTIVE_ID, workbenchWindow);
            } catch (WorkbenchException e) {
                ExceptionHandler.process(e);
                return;
            }
        }

        // find repository node
        repositoryNode = (RepositoryNode) RepositorySeekerManager.getInstance().searchRepoViewNode(params.getProperty("nodeId"),
                false);
        if (null != repositoryNode) {
            // expand/select node item
            RepositoryManagerHelper.getRepositoryView().getViewer().setSelection(new StructuredSelection(repositoryNode));
            init(repositoryNode);
            doRun();
        }
    }

}
