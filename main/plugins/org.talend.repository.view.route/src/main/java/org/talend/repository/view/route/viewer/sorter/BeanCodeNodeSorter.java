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

public class BeanCodeNodeSorter extends CodeRepositoryNodeSorter {

    /**
     * should be 3rd.
     */
    @Override
    protected void sortChildren(Object[] children) {
        int routinesIndex = -1;
        int beansIndex = -1;
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof RepositoryNode) {
                RepositoryNode node = (RepositoryNode) children[i];
                if (codeTester.isTypeTopNode(node, ERepositoryObjectType.ROUTINESJAR)) {
                    routinesIndex = i;
                } else if (codeTester.isTypeTopNode(node, ERepositoryObjectType.BEANS)) {
                    beansIndex = i;
                }
            }
        }
        int realIndex = 0; // top by default
        if (routinesIndex > -1) { // existed
            realIndex = 2;
        }
        if (beansIndex > -1) {
            swap(children, beansIndex, realIndex);
        }
    }
}
