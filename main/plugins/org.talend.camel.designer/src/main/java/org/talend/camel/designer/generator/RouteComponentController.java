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
package org.talend.camel.designer.generator;

import java.util.ArrayList;
import java.util.Collection;

import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.model.components.Expression;
import org.talend.designer.core.ui.editor.properties.controllers.ComponentListController;

/**
 * @author Xiaopeng Li
 *
 */
public class RouteComponentController extends ComponentListController {

    public RouteComponentController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    protected void doUpdateComponentList(IElement elem, IElementParameter param) {
        final String[] filters = param.getFilter().split("&");
        final String component = filters[0];
        final String additionalFilter = filters.length > 1 ? filters[1] : null;
        final Collection<INode> nodeList = new ArrayList<INode>();
        for (INode node : ((INode) elem).getProcess().getNodesOfType(component)) {
            if (node.isActivate() && node.getIncomingConnections().isEmpty()) {
                if (null != additionalFilter && !Expression.evaluate(additionalFilter, node.getElementParameters())) {
                    continue;
                }
                nodeList.add(node);
            }
        }
        updateComponentList(nodeList, (INode) elem, param, isSelectDefaultItem());
    }

    @Override
    protected boolean isSelectDefaultItem() {
        return false;
    }
}
