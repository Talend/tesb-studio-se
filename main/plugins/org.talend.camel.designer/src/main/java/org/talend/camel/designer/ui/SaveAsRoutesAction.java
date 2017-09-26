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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.EditorPart;
import org.talend.camel.designer.ui.editor.CamelMultiPageTalendEditor;
import org.talend.camel.designer.ui.editor.CamelProcessEditorInput;
import org.talend.camel.designer.ui.wizards.SaveAsRoutesWizard;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.repository.seeker.RepositorySeekerManager;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.repository.model.IRepositoryNode;

/**
 * DOC xtan class global comment. <br/>
 */
public class SaveAsRoutesAction extends Action {

    private final EditorPart editorPart;

    public SaveAsRoutesAction(EditorPart editorPart) {
        this.editorPart = editorPart;
    }

    @Override
    public void run() {
        SaveAsRoutesWizard processWizard = new SaveAsRoutesWizard((JobEditorInput) editorPart.getEditorInput());

        WizardDialog dlg = new WizardDialog(editorPart.getSite().getShell(), processWizard);
        if (dlg.open() == Window.OK) {

            try {

                // Set readonly to false since created routes will always be editable.
                JobEditorInput newRoutesEditorInput = new CamelProcessEditorInput(processWizard.getProcess(), true, true, false);

                IWorkbenchPage page = editorPart.getSite().getPage();

                IRepositoryNode repositoryNode = RepositorySeekerManager.getInstance().searchRepoViewNode(
                        newRoutesEditorInput.getItem().getProperty().getId(), false);
                newRoutesEditorInput.setRepositoryNode(repositoryNode);

                // close the old editor
                page.closeEditor(editorPart, false);

                // open the new editor, because at the same time, there will update the routes view
                page.openEditor(newRoutesEditorInput, CamelMultiPageTalendEditor.ID, true);

            } catch (Exception e) {
                MessageDialog.openError(editorPart.getSite().getShell(), "Error",
                        "Routes could not be saved" + " : " + e.getMessage());
                ExceptionHandler.process(e);
            }
        }
    }

}
