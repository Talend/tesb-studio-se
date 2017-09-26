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
package org.talend.designer.esb.webservice.migration;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

public class ConsumerFaultResponseMigrationTask extends
		AbstractItemMigrationTask {

	private static String faultCode = "faultCode";
	private static String faultActor = "faultActor";
	private static String faultNode = "faultNode";
	private static String faultRole = "faultRole";

	private static final ProxyRepositoryFactory FACTORY = ProxyRepositoryFactory
			.getInstance();

	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2011, 12, 30, 17, 21, 00);
		return gc.getTime();
	}

	public ExecutionResult execute(Item item) {
		try {
			addMoreFaultResponseMessage(item);
		} catch (Exception e) {
			ExceptionHandler.process(e);
			return ExecutionResult.FAILURE;
		}
		return ExecutionResult.SUCCESS_NO_ALERT;
	}

	private void addMoreFaultResponseMessage(Item item)
			throws PersistenceException {
		if (item instanceof ProcessItem) {
			ProcessType processType = ((ProcessItem) item).getProcess();
			for (Object o : processType.getNode()) {
				if (o instanceof NodeType) {
					NodeType currentNode = (NodeType) o;
					if ("tESBConsumer".equals(currentNode.getComponentName())) {
						Iterator<?> iterator = currentNode.getMetadata().iterator();
						while (iterator.hasNext()) {
							MetadataType metadataType = (MetadataType) iterator
									.next();
							if ("FAULT".equals(metadataType.getConnector())) {
								EList<?> column = metadataType.getColumn();
								addColumn(column, faultActor);
								addColumn(column, faultCode);
								addColumn(column, faultNode);
								addColumn(column, faultRole);
							}
						}
					}
				}
			}
			FACTORY.save(item, true);
		}
	}

	private void addColumn(EList column, String name) {
		Iterator<?> iterator = column.iterator();
		while (iterator.hasNext()) {
			Object next = iterator.next();
			if (next instanceof ColumnType) {
				ColumnType ct = (ColumnType) next;
				if (name.equals(ct.getName())) {
					return;
				}
			}
		}
		ColumnType columnType = TalendFileFactory.eINSTANCE.createColumnType();
		columnType.setDefaultValue("");
		columnType.setKey(false);
		columnType.setName(name);
		columnType.setSourceType("");
		columnType.setType("id_String");
		columnType.setLength(1024);
		columnType.setPrecision(0);
		columnType.setNullable(true);
		column.add(columnType);
	}

}
