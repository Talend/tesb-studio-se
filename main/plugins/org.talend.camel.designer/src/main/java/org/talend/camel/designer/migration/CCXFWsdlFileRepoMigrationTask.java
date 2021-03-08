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
package org.talend.camel.designer.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * TESB-19647 cSOAP wsdl empty, fix import item WSDL_FILE_REPO fields
 */
public class CCXFWsdlFileRepoMigrationTask extends AbstractRouteItemComponentMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 1, 31, 0, 0, 0); // earlier than
                                                                            // RenameToCSoapAndCRestMigrationTask to
                                                                            // avoid the cCXF has been renamed to cSOAP
        return gc.getTime();
    }

    @Override
    public String getComponentNameRegex() {
        return "cCXF|cSOAP"; // check both ccxf and csoap components to avoid it already been imported once.
    }

    @Override
    protected boolean execute(NodeType node) throws Exception {
        ElementParameterType resourceID = UtilTool.findParameterType(node, "WSDL_FILE_REPO:ROUTE_RESOURCE_TYPE_ID");
        if (resourceID != null) {
            // need to update ID first
            ElementParameterType fromExternal = TalendFileFactory.eINSTANCE.createElementParameterType();
            fromExternal.setField("ROUTE_RESOURCE_TYPE");
            fromExternal.setName("WSDL_FILE_REPO");
            fromExternal.setValue(resourceID.getValue());
            UtilTool.addParameterType(node, fromExternal);

            UtilTool.removeParameterType(node, resourceID);

            // remove uri
            ElementParameterType resourceURI = UtilTool.findParameterType(node, "WSDL_FILE_REPO:ROUTE_RESOURCE_TYPE_RES_URI");
            if (resourceURI != null) {
                UtilTool.removeParameterType(node, resourceURI);
            }
        }
        return true;
    }

}
