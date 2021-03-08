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
package org.talend.camel.designer.ui;

import java.util.List;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;

/**
 * The Action binding to a Talend Component UI eidotr,
 * and only effect to cTalendJob component.
 * Use to support a simple access point for editor job that linked to a cTalendJob component.
 */
public class OpenTalendJobRefAction extends org.talend.core.ui.editor.CustomExternalActions {

    private static final String NODE_PARAM_FROM_REPOSITORY_JOB = "FROM_REPOSITORY_JOB";
	private static final String NODE_PARAM_SELECTED_JOB_NAME = "SELECTED_JOB_NAME";
	private static final String REQUEST_TYPE_OPEN_NODE_PART = "open";
    private static final String C_TALEND_JOB_COMPONENT_NAME = "cTalendJob";
    private static final String ORG_TALEND_RCP_INTEGRATION_PERSPECTIVE = "org.talend.rcp.perspective";

    public OpenTalendJobRefAction() {
    }

    @Override
    public int getComponentType() {
        return 0;
    }

    /* Check if this action enable. Will ensure the cTalendJob was selected and has legal value.
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     *
     */
    @Override
    protected boolean calculateEnabled() {
        List<?> selectedObjects = getSelectedObjects();
        if (selectedObjects.size() != 1) {
        	//not select one object.
            return false;
        }
        Object select = selectedObjects.get(0);
        if (!(select instanceof NodePart)) {
        	//not select a node.
            return false;
        }

        NodePart nodePart = (NodePart) select;
        Node node = (Node) nodePart.getModel();
        if (!(node.getComponent() instanceof EmfComponent)) {
        	//not select a component.
            return false;
        }
        EmfComponent component = (EmfComponent) node.getComponent();
        if (!C_TALEND_JOB_COMPONENT_NAME.equals(component.getName())) {
        	//not cTalendJob component.
            return false;
        }
        Boolean isRepositoryJob=(Boolean) node.getElementParameter(NODE_PARAM_FROM_REPOSITORY_JOB).getValue();
        if(isRepositoryJob==null||!isRepositoryJob) {
        	//not from repository job.
        	return false;
        }
        String selectedJobName=(String)node.getElementParameter(NODE_PARAM_SELECTED_JOB_NAME).getValue();
        if(selectedJobName==null||selectedJobName.equals("")) {
        	//no reference job been choice.
        	return false;
        }
        return true;
    }

    /* Will send a "open" request to the selected nodePart, so it will try to open the reference job.
     * If the "open" command success, then will try to switch the perspective to Integration.
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
    	//diable switch perspective suggestion dialog. just for defense.
    	String notAskAutoSwitchToDIKey="notAskAutoSwitchToDI";
    	boolean oldValueNotSwitchToDiKey=PlatformUI.getPreferenceStore().getBoolean(notAskAutoSwitchToDIKey);
    	PlatformUI.getPreferenceStore().setValue(notAskAutoSwitchToDIKey, true);
    	//need to be  restore at the end .


        // open in editor, type and count already checked in calculateEnabled()
        List<?> selectedObjects = getSelectedObjects();
        Object select = selectedObjects.get(0);
        NodePart nodePart = (NodePart) select;
        nodePart.performRequest(new org.eclipse.gef.Request(REQUEST_TYPE_OPEN_NODE_PART));

        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if(activePage==null) {
        	return;
        }
        IEditorPart activeEditor = activePage.getActiveEditor();
        if(activeEditor==null||activeEditor.getEditorSite()==null) {
        	return;
        }

        Node node = (Node) nodePart.getModel();
        String selectedJobName=(String)node.getElementParameter(NODE_PARAM_SELECTED_JOB_NAME).getValue();
        String openJobName=activeEditor.getEditorInput().getName();
        if(!selectedJobName.equals(openJobName)) {
        	return;
        }
        // open/switch editor success and then  try to switch perspective.
        try {
        	//if current perspective is ORG_TALEND_RCP_INTEGRATION_PERSPECTIVE, will do nothing in under layer.
            PlatformUI.getWorkbench().showPerspective(ORG_TALEND_RCP_INTEGRATION_PERSPECTIVE, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
        } catch (WorkbenchException e) {
            e.printStackTrace();
        }finally {
        	//Restore config of switch perspective suggestion dialog. just for defense.
        	PlatformUI.getPreferenceStore().setValue(notAskAutoSwitchToDIKey, oldValueNotSwitchToDiKey);
        }

    }

}
