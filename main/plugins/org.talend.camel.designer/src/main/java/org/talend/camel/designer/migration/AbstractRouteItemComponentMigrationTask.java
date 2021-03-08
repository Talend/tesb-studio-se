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

import java.util.List;

import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;


/**
 * Migration Task only handle nodes of one specific camel component.
 */
public abstract class AbstractRouteItemComponentMigrationTask extends
		AbstractRouteItemMigrationTask {

	/**
	 * the regex patten to filter component name.
	 *
	 * @return the component name regex
	 */
	public abstract String getComponentNameRegex();

	/*
	 *
	 */
	/* (non-Javadoc)
	 * @see org.talend.camel.designer.migration.AbstractRouteItemMigrationTask#execute(org.talend.camel.core.model.camelProperties.CamelProcessItem)
	 */
	@Override
	public final ExecutionResult execute(CamelProcessItem item) {
		List<NodeType> nodes = findComponentNodesRegex(item, getComponentNameRegex());
		if (nodes.isEmpty()) {
			return ExecutionResult.NOTHING_TO_DO;
		}
		try {
			boolean needSave = false;
			for (NodeType node : nodes) {
				needSave |= execute(node);
			}
			if (needSave) {
				saveItem(item);
				return ExecutionResult.SUCCESS_NO_ALERT;
			} else {
				return ExecutionResult.NOTHING_TO_DO;
			}
		} catch (Exception e) {
			ExceptionHandler.process(e);
			return ExecutionResult.FAILURE;
		}
	}

	/**
	 * execute migration tasks by handle node, return indicator: whether need
	 * save.
	 *
	 * @param node the Component node, can't not be null.
	 * @return whether need save items. true: item need save, false: item not
	 * modified.
	 * @throws Exception the exception
	 */
	protected abstract boolean execute(NodeType node) throws Exception;
}
