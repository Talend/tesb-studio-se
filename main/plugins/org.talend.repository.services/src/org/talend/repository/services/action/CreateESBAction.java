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
package org.talend.repository.services.action;

import java.util.Properties;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.branding.IBrandingConfiguration;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.services.model.services.util.EServiceCoreImage;
import org.talend.repository.services.ui.ESBWizard;
import org.talend.repository.services.utils.ESBRepositoryNodeType;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class CreateESBAction extends AContextualAction implements IIntroAction {

    private static final String CREATE_LABEL = "Create Service";

    public CreateESBAction() {
        super();
        this.setText(CREATE_LABEL);
        this.setToolTipText(CREATE_LABEL);

        this.setImageDescriptor(ImageProvider.getImageDesc(EServiceCoreImage.SERVICE_ICON));
    }

    public CreateESBAction(boolean isToolbar) {
        this();
        setToolbar(isToolbar);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }
        if (canWork) {
            Object o = selection.getFirstElement();
            RepositoryNode node = (RepositoryNode) o;
            switch (node.getType()) {
            case SIMPLE_FOLDER:
            case SYSTEM_FOLDER:
                ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
                if (nodeType != ESBRepositoryNodeType.SERVICES) {
                    canWork = false;
                }
                if (node.getObject() != null && node.getObject().isDeleted()) {
                    canWork = false;
                }
                break;
            default:
                canWork = false;
            }
            if (canWork && !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                canWork = false;
            }
        }
        setEnabled(canWork);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    protected void doRun() {
        RepositoryNode beanNode = getCurrentRepositoryNode();
        if (isToolbar()) {
            if (beanNode != null && beanNode.getContentType() != ESBRepositoryNodeType.SERVICES) {
                beanNode = null;
            }
            if (beanNode == null) {
                beanNode = getRepositoryNodeForDefault(ESBRepositoryNodeType.SERVICES);
            }
        }
        ISelection selection;
        IWorkbenchPage activePage = getActivePage();
        if (activePage == null) {
            selection = getSelection();
        } else {
            selection = getRepositorySelection();
        }
        if (selection.isEmpty()) {
            return;
        }

        ESBWizard beanWizard = new ESBWizard(PlatformUI.getWorkbench(), true, selection);
        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), beanWizard);
        dlg.open();
    }

    /*
     * (non-Jsdoc)
     * 
     * @see org.eclipse.ui.intro.config.IIntroAction#run(org.eclipse.ui.intro.IIntroSite, java.util.Properties)
     */
    @Override
    public void run(IIntroSite site, Properties params) {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            MessageDialog.openWarning(null, "User Authority", "Can't create Service! Current user is read-only on this project!");
        } else {
            PlatformUI.getWorkbench().getIntroManager().closeIntro(PlatformUI.getWorkbench().getIntroManager().getIntro());
            selectRootObject(params);
            doRun();
        }
    }

    private void selectRootObject(Properties params) {

        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (null == workbenchWindow) {
            return;
        }
        IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
        if (null == workbenchPage) {
            return;
        }

        IPerspectiveDescriptor currentPerspective = workbenchPage.getPerspective();
        if (!IBrandingConfiguration.PERSPECTIVE_DI_ID.equals(currentPerspective.getId())) {
            // show Integration perspective
            try {
                workbenchWindow.getWorkbench().showPerspective(IBrandingConfiguration.PERSPECTIVE_DI_ID, workbenchWindow);
                workbenchPage = workbenchWindow.getActivePage();
            } catch (WorkbenchException e) {
                ExceptionHandler.process(e);
                return;
            }
        }

        IRepositoryView view = RepositoryManagerHelper.getRepositoryView();
        if (view != null) {

            Object type = params.get("type");

            if (ESBRepositoryNodeType.SERVICES.name().equals(type)) {
                RepositoryNode servicesNode = ((ProjectRepositoryNode) view.getRoot())
                        .getRootRepositoryNode(ESBRepositoryNodeType.SERVICES);
                if (servicesNode != null) {
                    setWorkbenchPart(view);
                    final StructuredViewer viewer = view.getViewer();
                    if (viewer instanceof TreeViewer) {
                        ((TreeViewer) viewer).expandToLevel(servicesNode, 1);
                    }
                    viewer.setSelection(new StructuredSelection(servicesNode));
                }
            }
        }
    }

}
