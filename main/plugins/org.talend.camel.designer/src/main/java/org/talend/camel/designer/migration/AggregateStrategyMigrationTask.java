package org.talend.camel.designer.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.PersistenceException;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class AggregateStrategyMigrationTask extends AbstractRouteItemComponentMigrationTask {

    @Override
    public String getComponentNameRegex() {
        return "cAggregate";
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2011, 11, 25, 10, 27, 00);
        return gc.getTime();
    }

    @Override
    protected boolean execute(NodeType node) throws Exception {
        return addBeanDotForStrategy(node);
    }

    private boolean addBeanDotForStrategy(NodeType currentNode) throws PersistenceException {
        boolean needSave = false;
        for (Object e : currentNode.getElementParameter()) {
            ElementParameterType p = (ElementParameterType) e;
            if ("AGGREGATION_STRATEGY".equals(p.getName())) {
                String value = p.getValue();
                value = "beans." + value;
                p.setValue(value);
                needSave = true;
                break;
            }
        }
        return needSave;
    }
}
