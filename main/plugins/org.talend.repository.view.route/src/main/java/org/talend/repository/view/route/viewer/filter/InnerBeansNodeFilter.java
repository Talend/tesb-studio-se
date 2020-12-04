// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.view.route.viewer.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;

public class InnerBeansNodeFilter extends ViewerFilter {

    /**
     * don't show inner beans directly under beansjar top node
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof RepositoryNode) {
            RepositoryNode node = (RepositoryNode) element;
            if (node.getContentType() == ERepositoryObjectType.BEANSJAR
                    && node.getType() == IRepositoryNode.ENodeType.SIMPLE_FOLDER) {
                RepositoryNode topNode = getParentNode(node);
                if (topNode.getContentType() == ERepositoryObjectType.BEANSJAR
                        && topNode.getType() == ENodeType.SYSTEM_FOLDER) {
                    return false;
                }
            }
        }
        return true;
    }

    private RepositoryNode getParentNode(RepositoryNode parentNode) {
        if (parentNode.getContentType() == ERepositoryObjectType.BEANSJAR && parentNode.getType() == ENodeType.SYSTEM_FOLDER) {
            return parentNode;
        }
        return getParentNode(parentNode.getParent());
    }
}
