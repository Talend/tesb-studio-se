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
package org.talend.designer.esb.components.rs.provider.migration;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

public class RestRequestWrongCallMigrationTask extends AbstractJobMigrationTask{

	private static final String httpMethod = "method";

	private static final ProxyRepositoryFactory FACTORY = ProxyRepositoryFactory.getInstance();

	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2011, 12, 30, 17, 21, 00);
		return gc.getTime();
	}

	public ExecutionResult execute(Item item) {
		try {
			addMoreWrongCallInfo(item);
		} catch (Exception e) {
			ExceptionHandler.process(e);
			return ExecutionResult.FAILURE;
		}
		return ExecutionResult.SUCCESS_NO_ALERT;
	}

	private void addMoreWrongCallInfo(Item item)
			throws PersistenceException {
		if (item instanceof ProcessItem) {
			boolean needSave = false;
			for (Object o : ((ProcessItem) item).getProcess().getNode()) {
				if (o instanceof NodeType) {
					NodeType currentNode = (NodeType) o;
					if ("tRESTRequest".equals(currentNode.getComponentName())) {
						Iterator iterator = currentNode.getMetadata().iterator();
						while (iterator.hasNext()) {
							MetadataType metadataType = (MetadataType) iterator.next();
							if ("WRONG_CALLS".equals(metadataType.getConnector())) {
								addColumn(metadataType.getColumn(), httpMethod);
								needSave = true;
							}
						}
					}
				}
			}
			if(needSave) {
				FACTORY.save(item, true);
			}
		}
	}

	private void addColumn(EList columns, String name) {
		Iterator iterator = columns.iterator();
		while (iterator.hasNext()) {
			Object next = iterator.next();
			if (next instanceof ColumnType && name.equals(((ColumnType) next).getName())) {
				return;
			}
		}
		ColumnType columnType = TalendFileFactory.eINSTANCE.createColumnType();
		columnType.setKey(false);
		columnType.setName(name);
		columnType.setSourceType("");
		columnType.setType("id_String");
		columnType.setLength(255);
		columnType.setPrecision(0);
		columnType.setNullable(true);
		columns.add(columnType);
	}

}
