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
package org.talend.repository.view.route.viewer.sorter;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.viewer.sorter.CodeRepositoryNodeSorter;

public class BeansJarCodeNodeSorter extends CodeRepositoryNodeSorter {

    /**
     * should be 4th.
     */
    @Override
    protected void sortChildren(Object[] children) {
        int beansIndex = -1;
        int beansJarIndex = -1;
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof RepositoryNode) {
                RepositoryNode node = (RepositoryNode) children[i];
                if (codeTester.isTypeTopNode(node, ERepositoryObjectType.BEANS)) {
                    beansIndex = i;
                } else if (codeTester.isTypeTopNode(node, ERepositoryObjectType.BEANSJAR)) {
                    beansJarIndex = i;
                }
            }
        }
        int realIndex = 0; // top by default
        if (beansIndex > -1) { // existed
            realIndex = 3;
        }
        if (beansJarIndex > -1) {
            swap(children, beansJarIndex, realIndex);
        }
    }
}
