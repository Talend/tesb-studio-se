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

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

/**
 * Common Abstract task for update module version of cMessgingEndpoint
 */
public abstract class CMessgingEndpointSwitchVersionTask extends AbstractRouteItemComponentMigrationTask {

	@Override
	public final String getComponentNameRegex() {
		return "cMessagingEndpoint";
	}

	@Override
	protected final boolean execute(NodeType node) throws Exception {
		return switchVersion(node);
	}

	private boolean switchVersion(NodeType currentNode) throws PersistenceException {
		boolean needSave = false;
		for (Object e : currentNode.getElementParameter()) {
			ElementParameterType p = (ElementParameterType) e;
			if ("HOTLIBS".equals(p.getName())) {
				EList<?> elementValue = p.getElementValue();
				for (Object pv : elementValue) {
					ElementValueType evt = (ElementValueType) pv;
					String evtValue = evt.getValue();
					String switchVersion = switchVersion(evtValue);
					if(switchVersion!=null && !switchVersion.equals(evtValue)) {
						evt.setValue(switchVersion);
						needSave = true;
					}
				}
			}
		}
		return needSave;
	}

	/**
	 * Switch version.
	 *
	 * @param evtValue the old version string
	 * @return the new version string
	 */
	protected abstract String switchVersion(String evtValue) ;
}
