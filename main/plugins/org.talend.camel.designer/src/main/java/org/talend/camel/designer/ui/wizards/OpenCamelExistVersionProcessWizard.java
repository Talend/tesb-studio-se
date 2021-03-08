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
package org.talend.camel.designer.ui.wizards;

import org.eclipse.ui.IWorkbenchPage;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.designer.ui.editor.CamelMultiPageTalendEditor;
import org.talend.camel.designer.ui.editor.CamelProcessEditorInput;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.designer.core.ui.wizards.OpenExistVersionProcessWizard;
import org.talend.repository.model.RepositoryNode;

public class OpenCamelExistVersionProcessWizard extends OpenExistVersionProcessWizard {

    public OpenCamelExistVersionProcessWizard(IRepositoryViewObject processObject) {
        super(processObject);
    }

    @Override
    protected RepositoryEditorInput getEditorInput(Item item, boolean readonly, IWorkbenchPage page)
        throws SystemException {
        if (item instanceof CamelProcessItem) {
            final CamelProcessItem processItem = (CamelProcessItem) item;
            return new CamelProcessEditorInput(processItem, true, false, readonly);
        }
        return null;
    }

    @Override
    protected void openAnotherVersion(RepositoryNode node, boolean readonly) {
        final Item item = node.getObject().getProperty().getItem();
        final IWorkbenchPage page = getActivePage();
        try {
            final RepositoryEditorInput fileEditorInput = getEditorInput(item, readonly, page);
            page.openEditor(fileEditorInput, CamelMultiPageTalendEditor.ID, readonly);
        } catch (Exception e) {
            MessageBoxExceptionHandler.process(e);
        }
    }
}
