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
package org.talend.camel.designer.ui.bean;

import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.designer.ui.wizards.CamelNewBeanWizard;
import org.talend.camel.designer.util.ECamelCoreImage;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.OverlayImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: CreateProcess.java 52792 2010-12-17 08:20:23Z cli $
 * 
 */
public class CreateCamelBean extends AbstractBeanAction implements IIntroAction {

    private static final String CREATE_LABEL = Messages.getString("CreateProcess.createBean"); //$NON-NLS-1$

    public CreateCamelBean() {
        super();
        this.setText(CREATE_LABEL);
        this.setToolTipText(CREATE_LABEL);

        Image folderImg = ImageProvider.getImage(ECamelCoreImage.BEAN_ICON);
        this.setImageDescriptor(OverlayImageProvider.getImageWithNew(folderImg));
    }

    public CreateCamelBean(boolean isToolbar) {
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
                if (nodeType != CamelRepositoryNodeType.repositoryBeansType) {
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
        // RepositoryNode codeNode = getViewPart().getRoot().getChildren().get(4);
        // RepositoryNode routineNode = codeNode.getChildren().get(0);
        RepositoryNode beanNode = getCurrentRepositoryNode();

        if (isToolbar()) {
            if (beanNode != null && beanNode.getContentType() != CamelRepositoryNodeType.repositoryBeansType) {
                beanNode = null;
            }
            if (beanNode == null) {
                beanNode = getRepositoryNodeForDefault(CamelRepositoryNodeType.repositoryBeansType);
            }
        }
        RepositoryNode node = null;
        IPath path = null;
        if (!isToolbar()) {
            ISelection selection = getSelection();
            Object obj = ((IStructuredSelection) selection).getFirstElement();
            node = (RepositoryNode) obj;
            path = RepositoryNodeUtilities.getPath(node);
        }

        CamelNewBeanWizard beanWizard = new CamelNewBeanWizard(path);
        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), beanWizard);

        if (dlg.open() == Window.OK) {

            try {
                openBeanEditor(beanWizard.getBean(), false);
            } catch (PartInitException e) {
                MessageBoxExceptionHandler.process(e);
            } catch (SystemException e) {
                MessageBoxExceptionHandler.process(e);
            }
        }
    }

    /*
     * (non-Jsdoc)
     * 
     * @see org.eclipse.ui.intro.config.IIntroAction#run(org.eclipse.ui.intro.IIntroSite, java.util.Properties)
     */
    public void run(IIntroSite site, Properties params) {
        PlatformUI.getWorkbench().getIntroManager().closeIntro(PlatformUI.getWorkbench().getIntroManager().getIntro());
        selectRootObject(params);
        doRun();
    }

    private void selectRootObject(Properties params) {

        IRepositoryView view = RepositoryManagerHelper.getRepositoryView();
        if (view != null) {
            Object type = params.get("type");
            if (CamelRepositoryNodeType.repositoryBeansType.name().equals(type)) {
                IRepositoryNode processNode = ((ProjectRepositoryNode) view.getRoot())
                        .getRootRepositoryNode(ERepositoryObjectType.PROCESS);
                if (processNode != null) {
                    setWorkbenchPart(view);
                    final StructuredViewer viewer = view.getViewer();
                    if (viewer instanceof TreeViewer) {
                        ((TreeViewer) viewer).expandToLevel(processNode, 1);
                    }
                    viewer.setSelection(new StructuredSelection(processNode));
                }

            }
        }

    }

}
