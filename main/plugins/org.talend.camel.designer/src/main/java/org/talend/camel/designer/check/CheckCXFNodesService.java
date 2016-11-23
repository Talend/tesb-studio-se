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
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.ICheckNodesService;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.views.problems.Problems;

public class CheckCXFNodesService implements ICheckNodesService {

    @Override
    public void checkNode(Node node) {
        if (!node.getComponent().getName().equals("cCXF")) { //$NON-NLS-1$
            return;
        }
        final IElementParameter serviceParam = node.getElementParameter("SERVICE_TYPE"); //$NON-NLS-1$

        // Select WSDL
        if (serviceParam != null && "wsdlURL".equals(serviceParam.getValue())) { //$NON-NLS-1$
            final IElementParameter wsdlFileParam = node.getElementParameter("WSDL_FILE"); //$NON-NLS-1$
            final IElementParameter wsdlTypeParam = node.getElementParameter("WSDL_TYPE"); //$NON-NLS-1$
            // Select File
            if (wsdlTypeParam != null && "file".equals(wsdlTypeParam.getValue())) { //$NON-NLS-1$
                // WSDL file is empty
                if (wsdlFileParam == null || wsdlFileParam.getValue() == null || wsdlFileParam.getValue().toString().isEmpty()
                        || wsdlFileParam.getValue().toString().equals("\"\"")) { //$NON-NLS-1$
                    String errorMessage = MessageFormat.format(CamelDesignerMessages.getString("CheckCXFNodesService_emptyError") //$NON-NLS-1$
                            , wsdlFileParam == null ? "" : wsdlFileParam.getDisplayName());
                    Problems.add(ProblemStatus.ERROR, node, errorMessage);
                }
            } // Select Repository
            else if (wsdlTypeParam != null && "repo".equals(wsdlTypeParam.getValue())) { //$NON-NLS-1$
                final IElementParameter resourceParam = node
                        .getElementParameterFromField(EParameterFieldType.ROUTE_RESOURCE_TYPE);
                // WSDL file is empty
                String errorMessage = ""; //$NON-NLS-1$
                if (resourceParam == null || resourceParam.getValue() == null || resourceParam.getValue().toString().isEmpty()) {
                    errorMessage = MessageFormat.format(CamelDesignerMessages.getString("CheckCXFNodesService_emptyError") //$NON-NLS-1$
                            , wsdlFileParam.getDisplayName());
                    Problems.add(ProblemStatus.ERROR, node, errorMessage);
                } else {
                    String id = (String) resourceParam.getValue();
                    try {
                        IRepositoryViewObject lastVersion = ProxyRepositoryFactory.getInstance().getLastVersion(id);
                        if (lastVersion == null) {
                            errorMessage = MessageFormat
                                    .format(CamelDesignerMessages.getString("CheckCXFNodesService_nonexistError") //$NON-NLS-1$
                                            , wsdlFileParam.getDisplayName());
                            Problems.add(ProblemStatus.ERROR, node, errorMessage);
                        } else if (lastVersion.isDeleted()) {
                            errorMessage = MessageFormat
                                    .format(CamelDesignerMessages.getString("CheckCXFNodesService_removedError") //$NON-NLS-1$
                                            , resourceParam.getDisplayName());
                            Problems.add(ProblemStatus.ERROR, node, errorMessage);
                        }
                    } catch (PersistenceException e) {
                        errorMessage = MessageFormat.format(CamelDesignerMessages.getString("CheckCXFNodesService_emptyError") //$NON-NLS-1$
                                , wsdlFileParam.getDisplayName());
                        Problems.add(ProblemStatus.ERROR, node, errorMessage);
                    }
                }
            }
        }
        // Select Service class
        else if (serviceParam != null && "serviceClass".equals(serviceParam.getValue())) { //$NON-NLS-1$
            final IElementParameter clazzParam = node.getElementParameter("SERVICE_CLASS"); //$NON-NLS-1$
            // Service class is empty
            if (clazzParam == null || clazzParam.getValue() == null || clazzParam.getValue().toString().isEmpty()) {
                String errorMessage = MessageFormat.format(CamelDesignerMessages.getString("CheckCXFNodesService_emptyError") //$NON-NLS-1$
                        , clazzParam.getDisplayName());
                Problems.add(ProblemStatus.ERROR, node, errorMessage);
            }
        }

    }

}
