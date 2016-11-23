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
package org.talend.camel.designer.check;

import java.text.MessageFormat;

import org.talend.camel.designer.i18n.CamelDesignerMessages;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.designer.core.ICheckNodesService;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.views.problems.Problems;

public class CheckRouteComponentTypeService implements ICheckNodesService {

    @Override
    public void checkNode(Node node) {
        for (IElementParameter param : node.getElementParametersFromField(EParameterFieldType.ROUTE_COMPONENT_TYPE)) {
            if (!param.isShow(node.getElementParameters())) {
                continue;
            }
            final Object idParam = param.getValue();
            if (idParam == null || idParam.toString().isEmpty()) {
                Problems.add(ProblemStatus.ERROR, node, MessageFormat
                        .format(CamelDesignerMessages.getString("CheckJMSNodesService_emptyParaError"), param.getDisplayName())); //$NON-NLS-1$
            } else {
                String messageTemplate = CamelDesignerMessages.getString("CheckJMSNodesService_componentMissingError"); //$NON-NLS-1$
                for (INode n : node.getProcess().getGraphicalNodes()) {
                    if (n.getUniqueName().equals(idParam)) {
                        messageTemplate = n.isActivate() ? null
                                : CamelDesignerMessages.getString("CheckJMSNodesService_componentDisabledError"); //$NON-NLS-1$
                        break;
                    }
                }
                if (messageTemplate != null) {
                    Problems.add(ProblemStatus.ERROR, node, MessageFormat.format(messageTemplate, idParam));
                }
            }
        }
    }

}
