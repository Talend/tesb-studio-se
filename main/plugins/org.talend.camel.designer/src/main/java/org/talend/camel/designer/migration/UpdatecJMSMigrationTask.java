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

import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

/**
 * http://jira.talendforge.org/browse/TESB-6440
 * 
 * @author LiXiaopeng
 * Update: Removed common functions to handle NodeType to {@link AbstractRouteItemComponentMigrationTask.UtilTool} - by GaoZone.
 * {@link }
 */
public class UpdatecJMSMigrationTask extends AbstractRouteItemComponentMigrationTask {

    @Override
	public String getComponentNameRegex() {
		return "cJMS";
	}

    @Override
	protected boolean execute(NodeType node) throws Exception {
		return updateJMSComponent(node);
	}

    public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2012, 7, 10, 14, 00, 00);
        return gc.getTime();
    }

    /**
     * Update cJMS, add cJMSConnectionFactory.
     * 
     * @param item
     * @throws PersistenceException
     */
    private boolean updateJMSComponent(NodeType currentNode) throws PersistenceException {
    	ElementParameterType oldParam = UtilTool.findParameterType(currentNode, "CONNECTION_FACOTRY");
    	if(oldParam == null) {
    		return false;
    	}
		String oldId = oldParam.getValue();
		boolean paramRemoved = UtilTool.removeParameterType(currentNode, oldParam);
		
		if(oldId == null) {
			return paramRemoved;
		}

		ElementParameterType newConnectionFactoryParam = UtilTool.createParameterType(
				EParameterFieldType.ROUTE_COMPONENT_TYPE
				.getName(),
				"CONNECTION_FACOTRY_CONFIGURATION", "");
		UtilTool.addParameterType(currentNode, newConnectionFactoryParam);

		String newId = oldId.replace("cJMSConnectionFactory", "cMQConnectionFactory_");

		ElementParameterType idParam = UtilTool.createParameterType(
				EParameterFieldType.TECHNICAL.getName(),
				"CONNECTION_FACOTRY_CONFIGURATION:ROUTE_COMPONENT_TYPE_ID",
				newId);
		UtilTool.addParameterType(currentNode, idParam);
		return true;
    }

}
