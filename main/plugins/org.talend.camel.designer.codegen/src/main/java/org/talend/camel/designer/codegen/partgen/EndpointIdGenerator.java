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
package org.talend.camel.designer.codegen.partgen;

import org.talend.camel.designer.codegen.util.NodeUtil;
import org.talend.core.model.process.INode;
import org.talend.designer.codegen.exception.CodeGeneratorException;

public class EndpointIdGenerator implements PartGenerator<INode> {

    private static final String getNodeId(INode node) {
        return node.getProcess().getName() + '_' + node.getUniqueName();
    }

    @Override
    public CharSequence generatePart(INode node, Object... ignoredParams) throws CodeGeneratorException {
        String part;
        // cJavaDSL should not be generating id() DSL statement
        if ("cJavaDSLProcessor".equals(node.getComponent().getName())) { //$NON-NLS-1$
            part = ""; //$NON-NLS-1$
        } else if (NodeUtil.isStartNode(node)) {
            if ("cErrorHandler".equals(node.getComponent().getName())) { //$NON-NLS-1$
                part = ""; //$NON-NLS-1$
            } else {
                part = ".routeId(\"" + getNodeId(node) + "\")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else {
            part = ".id(\"" + getNodeId(node) + "\")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return part;
    }

}
