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

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

public class FormContentTypeMigrationTask extends AbstractJobMigrationTask {

	private static final ProxyRepositoryFactory FACTORY = ProxyRepositoryFactory.getInstance();

	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2013, 8, 27, 13, 21, 00);
		return gc.getTime();
	}

	public ExecutionResult execute(Item item) {
		try {
			changeDefaultFormContentType(item);
		} catch (Exception e) {
			ExceptionHandler.process(e);
			return ExecutionResult.FAILURE;
		}
		return ExecutionResult.SUCCESS_NO_ALERT;
	}

	private void changeDefaultFormContentType(Item item)
			throws PersistenceException {
		if (!(item instanceof ProcessItem)) {
			return;
		}
		boolean needSave = false;
		for (Object o : ((ProcessItem) item).getProcess().getNode()) {
			if (!(o instanceof NodeType)) {
				continue;
			}
			NodeType currentNode = (NodeType) o;
			if ("tRESTRequest".equals(currentNode.getComponentName())) {
	            for (Object m : currentNode.getMetadata()) {
	                MetadataType metadataType = (MetadataType) m;
	                if ("WRONG_CALLS".equals(metadataType.getConnector())) {
	                    continue;
	                }
	                needSave |= updateFormContentTypeOf(metadataType, currentNode);
	            }
                // assume only one tRESTRequest inside
	            break;
			}
		}
		if (needSave) {
			FACTORY.save(item, true);
		}
	}

	private boolean updateFormContentTypeOf(MetadataType metadataType, NodeType currentNode) {
		boolean isChanged = false;
		for (Object o : metadataType.getColumn()) {
			if (!(o instanceof ColumnType)) {
				continue;
			}
			ColumnType ct = (ColumnType) o;
			// if it's body, then continue
			if ("body".equals(ct.getName())) {
				continue;
			}
			// if it's not form, then continue
			String comment = ct.getComment();
			if (!"form".equalsIgnoreCase(comment)) {
				continue;
			}
			updateFormConsumeContentTypeFor(metadataType.getName(), currentNode);
			isChanged = true;
		}
		return isChanged;
	}

	private void updateFormConsumeContentTypeFor(String connectorName, NodeType currentNode) {
        for (Object o : currentNode.getElementParameter()) {
			if (!(o instanceof ElementParameterType)) {
				continue;
			}
			ElementParameterType ept = (ElementParameterType) o;
			if ("SCHEMAS".equals(ept.getName())) {
    			EList elementValue = ept.getElementValue();
    			if (elementValue == null || elementValue.isEmpty()) {
    				break;
    			}
    			int size = elementValue.size();
    			for (int i = 0; i < size; i++) {
    				ElementValueType evt = (ElementValueType) elementValue.get(i);
    				if ("SCHEMA".equals(evt.getElementRef())
    						&& connectorName.equals(evt.getValue())) {
        				/*
        				 * i+1 : HTTP_VERB i+2 : URI_PATTERN
        				 */
        				if (i + 3 == size) {
        					ElementValueType newType = TalendFileFactory.eINSTANCE
        							.createElementValueType();
        					newType.setElementRef("CONSUMES");
        					newType.setValue("FORM");
        					elementValue.add(newType);
        				} else if (i + 3 < size
        						&& !"CONSUMES".equals((ElementValueType) elementValue
        								.get(i + 3))) {
        					ElementValueType newType = TalendFileFactory.eINSTANCE
        							.createElementValueType();
        					newType.setElementRef("CONSUMES");
        					newType.setValue("FORM");
        					elementValue.add(i + 3, newType);
        				}
                        break;
    				}
    			}
                break;
			}
		}
	}

}