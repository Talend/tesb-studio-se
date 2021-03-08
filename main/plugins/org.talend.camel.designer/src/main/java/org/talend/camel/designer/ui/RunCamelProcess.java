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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.camel.designer.i18n.Messages;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.core.debug.JobLaunchShortcutManager;
import org.talend.repository.model.BinRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * bqian A action to run the selected process without opening it's editor. <br/>
 *
 * $Id: RunProcess.java 0 2007-12-28 11:09:48Z bqian $
 *
 */
public class RunCamelProcess extends AContextualAction {

    private static final String TEST_CONTAINER = "TEST_CONTAINER";

    private static final String LABEL = Messages.getString("RunProcess.runJob"); //$NON-NLS-1$

    public RunCamelProcess() {
        super();
        this.setText(LABEL);
        this.setToolTipText(LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.ROUTES_ICON));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    protected void doRun() {
        ISelection selection = getSelection();
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        if (!(obj instanceof RepositoryNode)) {
            return;
        }
        // Add this job to running history list.
        JobLaunchShortcutManager.run(selection);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        // if
        // (DesignerPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory().isUserReadOnlyOnCurrentProject())
        // {
        // canWork = false;
        // }
        if (canWork) {
            Object o = selection.getFirstElement();
            RepositoryNode node = (RepositoryNode) o;

            switch (node.getType()) {
            case REPOSITORY_ELEMENT:
                if (node.getParent() == null || node.getParent().getContentType() != CamelRepositoryNodeType.repositoryRoutesType) {
                    canWork = false;
                }
                // Avoid showing in route test case
                if (node.getObjectType().getType().equals(TEST_CONTAINER)) {
                    canWork = false;
                }
                break;
            default:
                canWork = false;
            }
            RepositoryNode parent = node.getParent();
            if (canWork && parent != null && parent instanceof BinRepositoryNode) {
                canWork = false;
            }
        }
        setEnabled(canWork);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.AContextualView#getClassForDoubleClick()
     */
    @Override
    public Class<?> getClassForDoubleClick() {
        return ProcessItem.class;
    }
}
