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
package org.talend.camel.designer.ui;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.designer.ui.editor.CamelMultiPageTalendEditor;
import org.talend.camel.designer.ui.editor.CamelProcessEditorInput;
import org.talend.camel.designer.ui.view.SpringConfigurationView;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.designer.core.ui.action.EditProcess;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: EditProcess.java 52559 2010-12-13 04:14:06Z nrousseau $
 * 
 */
public class EditCamelProcess extends EditProcess {

    private final String EDIT_LABEL = Messages.getString("EditProcess.editJob"); //$NON-NLS-1$

    private final String OPEN_LABEL = Messages.getString("EditProcess.openJob"); //$NON-NLS-1$

    public EditCamelProcess() {
        super();
        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.ROUTES_ICON));
    }

    @Override
    protected JobEditorInput getEditorInput(ProcessItem processItem) throws PersistenceException {
        return new CamelProcessEditorInput(processItem, true, true);
    }

    @Override
    protected String getEditorId() {
        return CamelMultiPageTalendEditor.ID;
    }

    @Override
    protected ERepositoryObjectType getProcessType() {
        return CamelRepositoryNodeType.repositoryRoutesType;
    }

    @Override
    protected String getLabel(boolean editable) {
        return editable ? EDIT_LABEL : OPEN_LABEL;
    }

    @Override
    public Class<?> getClassForDoubleClick() {
        return CamelProcessItem.class;
    }

}
