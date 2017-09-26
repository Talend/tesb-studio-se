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
package org.talend.camel.designer.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class UpdateRouteResourceTypeMigrationTask extends AbstractRouteItemComponentMigrationTask {

    @Override
    public String getComponentNameRegex() {
        return "cSOAP|cFlatPack";
    }

    public Date getOrder() {
        return new GregorianCalendar(2015, 10, 7).getTime();
    }

    @Override
    protected boolean execute(NodeType node) throws Exception {
        final ElementParameterType newParam = findParameterTypeByField(node, EParameterName.ROUTE_RESOURCE_TYPE.getName());
        if (newParam == null) {
            return false;
        }
        ElementParameterType oldParam = UtilTool.findParameterType(node, newParam.getName() + ":ROUTE_RESOURCE_TYPE_ID");
        if (oldParam == null) {
            return false;
        }
        newParam.setValue(oldParam.getValue());
        UtilTool.removeParameterType(node, oldParam);

        oldParam = UtilTool.findParameterType(node, newParam.getName() + ":ROUTE_RESOURCE_TYPE_RES_URI");
        if (oldParam == null) {
            return false;
        }
        UtilTool.removeParameterType(node, oldParam);

        return true;
    }

    private static ElementParameterType findParameterTypeByField(NodeType node, String paramField) {
        for (Object param : node.getElementParameter()) {
            final ElementParameterType paramType = (ElementParameterType) param;
            if (paramType.getField().equals(paramField)) {
                return paramType;
            }
        }
        return null;
    }

}
