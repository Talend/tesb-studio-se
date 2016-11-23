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
package org.talend.camel.designer.ui;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.designer.ui.editor.CamelMultiPageTalendEditor;
import org.talend.camel.designer.ui.editor.CamelProcessEditorInput;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.designer.core.ui.action.AbstractProcessAction;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;

public class ReadCamelProcess extends AbstractProcessAction {

    private static final String LABEL = Messages.getString("ReadProcess.label"); //$NON-NLS-1$

    public ReadCamelProcess() {
        super();
        this.setText(LABEL);
        this.setToolTipText(LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.READ_ICON));
    }

    @Override
    protected void doRun() {
        final IRepositoryNode node = (IRepositoryNode) ((IStructuredSelection) getSelection()).getFirstElement();
        CamelProcessItem processItem = (CamelProcessItem) node.getObject().getProperty().getItem();

        IWorkbenchPage page = getActivePage();

        try {
            CamelProcessEditorInput fileEditorInput = new CamelProcessEditorInput(processItem, true, null, true);
            checkUnLoadedNodeForProcess(fileEditorInput);
            IEditorPart editorPart = page.findEditor(fileEditorInput);

            if (editorPart == null) {
                fileEditorInput.setRepositoryNode(node);
                page.openEditor(fileEditorInput, CamelMultiPageTalendEditor.ID, true);
            } else {
                page.activate(editorPart);
            }
        } catch (PartInitException | PersistenceException e) {
            MessageBoxExceptionHandler.process(e);
        }
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        if (canWork) {
            final IRepositoryNode node = (IRepositoryNode) selection.getFirstElement();
            canWork = node.getType() == ENodeType.REPOSITORY_ELEMENT
                    && node.getObjectType() == CamelRepositoryNodeType.repositoryRoutesType
                    && !RepositoryManager.isOpenedItemInEditor(node.getObject());
        }
        setEnabled(canWork);
    }
}
