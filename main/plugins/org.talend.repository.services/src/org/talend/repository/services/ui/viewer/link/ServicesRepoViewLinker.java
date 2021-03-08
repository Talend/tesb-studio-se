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
package org.talend.repository.services.ui.viewer.link;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.link.AbstractFileEditorInputLinker;
import org.talend.core.repository.seeker.RepositorySeekerManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.services.action.ServiceEditorInput;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 *
 */
public class ServicesRepoViewLinker extends AbstractFileEditorInputLinker {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.repository.link.AbstractFileEditorInputLinker#checkFileExtension(org.eclipse.core.resources.IFile
     * )
     */
    @Override
    protected boolean checkFileExtension(IFile file) {
        return file != null && file.getName().endsWith(FileConstants.WSDL_FILE_SUFFIX);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.link.AbstractFileEditorInputLinker#getEditor(org.eclipse.ui.IWorkbenchPage,
     * org.eclipse.ui.IEditorInput)
     */
    @Override
    protected IEditorPart getEditor(IEditorInput editorInput) {
        IEditorPart wsdlEditor = super.getEditor(editorInput);
        if (wsdlEditor != null && wsdlEditor instanceof org.talend.repository.services.utils.LocalWSDLEditor) {
            return wsdlEditor;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.repository.link.AbstractFileEditorInputLinker#getRepoNodeFromEditor(org.eclipse.ui.IEditorPart)
     */
    @Override
    protected RepositoryNode getRepoNodeFromEditor(IEditorPart editorPart) {
    	if (editorPart != null && editorPart instanceof org.talend.repository.services.utils.LocalWSDLEditor) {

    		IEditorInput editorInput= ((org.talend.repository.services.utils.LocalWSDLEditor) editorPart)
    				.getEditorInput();
    		if(editorInput instanceof ServiceEditorInput) {
    			Item item=((ServiceEditorInput) editorInput).getItem();
    			if(item!=null) {
    				IRepositoryNode node = RepositorySeekerManager.getInstance().searchRepoViewNode(item.getProperty().getId(), false);
    				if(node!=null&&node instanceof RepositoryNode) {
    					return (RepositoryNode) node;
    				}
    			}
    		}

    	}
    	return null;
    }

}
