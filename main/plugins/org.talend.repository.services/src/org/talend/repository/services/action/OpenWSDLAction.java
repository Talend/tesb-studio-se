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

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.actions.metadata.AbstractCreateAction;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.services.model.services.util.EServiceCoreImage;
import org.talend.repository.services.ui.OpenWSDLWizard;
import org.talend.repository.services.utils.ESBRepositoryNodeType;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class OpenWSDLAction extends AbstractCreateAction {

    private static final String createLabel = "Assign WSDL";

    private static final ERepositoryObjectType currentNodeType = ESBRepositoryNodeType.SERVICES;

    private static final int WIZARD_WIDTH = 500;

    private static final int WIZARD_HEIGHT = 140;

    public OpenWSDLAction() {
        super();

        this.setText(createLabel);
        this.setToolTipText(createLabel);
        this.setImageDescriptor(ImageProvider.getImageDesc(EServiceCoreImage.SERVICE_ICON));
    }

    public OpenWSDLAction(boolean isToolbar) {
        this();
        setToolbar(isToolbar);
    }

    @Override
    protected void init(RepositoryNode node) {
        ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        if (!currentNodeType.equals(nodeType)) {
            return;
        }
        this.setText(createLabel);
        // IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (node.getType() != ENodeType.REPOSITORY_ELEMENT) {
            return;
        }

        try {
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            factory.updateLockStatus();
            ERepositoryStatus status = factory.getStatus(node.getObject());
            if (status.isEditable() || ERepositoryStatus.DEFAULT == status) {
                setEnabled(isLastVersion(node));
            } else {
                setEnabled(false);
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
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

        if (isToolbar()) {
            init(repositoryNode);
        }
        WizardDialog wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), new OpenWSDLWizard(repositoryNode));

        wizardDialog.setPageSize(WIZARD_WIDTH, WIZARD_HEIGHT);
        wizardDialog.create();

        wizardDialog.open();
    }
}
