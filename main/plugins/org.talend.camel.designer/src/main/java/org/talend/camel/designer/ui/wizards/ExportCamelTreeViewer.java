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
package org.talend.camel.designer.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.talend.camel.model.CamelRepositoryNodeType;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.wizards.exportjob.ExportTreeViewer;
import org.talend.repository.ui.wizards.exportjob.JobScriptsExportWizardPage;

public class ExportCamelTreeViewer extends ExportTreeViewer {

    public ExportCamelTreeViewer(IStructuredSelection selection, JobScriptsExportWizardPage jobScriptExportWizardPage) {
        super(selection, jobScriptExportWizardPage);
    }

    @Override
    protected boolean filterRepositoryNode(RepositoryNode node) {
        if (node == null) {
            return false;
        }
        if (node.isBin()) {
            return false;
        }

        ERepositoryObjectType contentType = node.getContentType();
        if (contentType != null) {

            // Fix bug TESB-2939 LiXiaopeng
            if (contentType == CamelRepositoryNodeType.repositoryRoutesType) { // referenced project.
                return true;
            } else if (contentType == ERepositoryObjectType.SVN_ROOT) {
                return true;
            } else if (contentType == ERepositoryObjectType.REFERENCED_PROJECTS) {
                return true;
            } else {
                return false;
            }
        } else {
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected ERepositoryObjectType getCheckingType() {
        return CamelRepositoryNodeType.repositoryRoutesType;
    }

}
