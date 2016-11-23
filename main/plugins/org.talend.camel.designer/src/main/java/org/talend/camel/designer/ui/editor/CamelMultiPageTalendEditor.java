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
package org.talend.camel.designer.ui.editor;

import java.text.MessageFormat;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.talend.camel.designer.CamelDesignerPlugin;
import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.designer.ui.editor.dependencies.CamelDependenciesEditor;
import org.talend.camel.designer.util.CamelSpringUtil;
import org.talend.camel.designer.util.ECamelCoreImage;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.IProcess2;
import org.talend.core.services.ISVNProviderService;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;

public class CamelMultiPageTalendEditor extends AbstractMultiPageTalendEditor {

    public static final String ID = "org.talend.camel.designer.core.ui.CamelMultiPageTalendEditor"; //$NON-NLS-1$

    private CamelDependenciesEditor dependenciesEditor;

    public CamelMultiPageTalendEditor() {
        super();
        designerEditor = new CamelTalendEditor();
    }

    @Override
    public String getEditorId() {
        return ID;
    }

    @Override
    public AbstractTalendEditor getDesignerEditor() {
        return this.designerEditor;
    }

    @Override
    protected IImage getEditorTitleImage() {
        return ECamelCoreImage.ROUTE_EDITOR_ICON;
    }

    /**
     * The <code>MultiPageEditorExample</code> implementation of this method checks that the input is an instance of
     * <code>IFileEditorInput</code>.
     */
    @Override
    public void init(final IEditorSite site, final IEditorInput editorInput) throws PartInitException {
        if (!(editorInput instanceof CamelProcessEditorInput)) {
            throw new PartInitException(Messages.getString("MultiPageTalendEditor.InvalidInput")); //$NON-NLS-1$
        }
        super.init(site, editorInput);
        CamelSpringUtil.openSpringView(IWorkbenchPage.VIEW_CREATE);
    }

    @Override
    public void setName() {
        super.setName();
        if (getEditorInput() == null) {
            return;
        }
        final IProcess2 process2 = this.getProcess();
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            final ISVNProviderService service = (ISVNProviderService) GlobalServiceRegister.getDefault()
                    .getService(ISVNProviderService.class);
            if (revisionChanged && service.isProjectInSvnMode()) {
                revisionNumStr = service.getCurrentSVNRevision(process2);
                revisionChanged = false;
                if (revisionNumStr != null) {
                    revisionNumStr = ".r" + revisionNumStr;
                }
            }
        }

        final String itemName = process2.getElementName();
        final String label = getEditorInput().getName();
        final String jobVersion = (process2 != null) ? process2.getVersion() : "0.1";
        String title = MessageFormat.format("{0} {1} {2}", itemName, label, jobVersion); //$NON-NLS-1$
        if (revisionNumStr != null) {
            title += revisionNumStr;
        }
        setPartName(title);
    }

    @Override
    protected void createPage2() {
        dependenciesEditor = new CamelDependenciesEditor(this, designerEditor.isReadOnly());
        try {
            int index = addPage(dependenciesEditor, getEditorInput());
            setPageText(index,
                    org.talend.camel.designer.ui.editor.dependencies.Messages.EditDependenciesContextualAction_ActionName);
            setPageImage(index, CamelDesignerPlugin.getImage(CamelDesignerPlugin.DEPEN_ICON));
        } catch (PartInitException e) {
            ExceptionHandler.process(e);
        }
    }
}
