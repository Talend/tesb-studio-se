package org.talend.camel.designer.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.PersistenceException;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class UpdatecKafkaMigrationTask extends AbstractRouteItemComponentMigrationTask {
	
    @Override
    public String getComponentNameRegex() {
        return "cKafka";
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2021, 6, 25, 8, 0, 0);
        return gc.getTime();
    }

	@Override
	protected boolean execute(NodeType node) throws Exception {
        return renameAdvancedParameters(node);
	}

	private boolean renameAdvancedParameters(NodeType currentNode) throws PersistenceException {
        boolean needSave = false;

        for (Object e : currentNode.getElementParameter()) {
            ElementParameterType p = (ElementParameterType) e;
            if ("URI_OPTIONS".equals(p.getName())) {
                for (Object v : p.getElementValue()) {
                    ElementValueType ev = (ElementValueType) v;
                    if ("\"kafkaHeaderSerializer\"".equalsIgnoreCase(ev.getValue()) && "NAME".equalsIgnoreCase(ev.getElementRef())) {
                        ev.setValue("\"headerSerializer\"");
                        needSave = true;
                        }
                    if ("\"kafkaHeaderDeserializer\"".equalsIgnoreCase(ev.getValue()) && "NAME".equalsIgnoreCase(ev.getElementRef())) {
                        ev.setValue("\"headerDeserializer\"");
                        needSave = true;
                    }
                    needSave = true;
                }
            }           
        }
        return needSave;
    }
}
