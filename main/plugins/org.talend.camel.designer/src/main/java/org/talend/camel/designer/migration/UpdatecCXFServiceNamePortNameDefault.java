package org.talend.camel.designer.migration;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.PersistenceException;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class UpdatecCXFServiceNamePortNameDefault extends AbstractRouteItemComponentMigrationTask {

    @Override
    public String getComponentNameRegex() {
        return "cCXF";
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 11, 24, 14, 00, 00);
        return gc.getTime();
    }

    @Override
    protected boolean execute(NodeType node) throws Exception {
        return changeDefaulValues(node);
    }

    private boolean changeDefaulValues(NodeType currentNode) throws PersistenceException, IOException {
        boolean needSave = false;
        for (Object e : currentNode.getElementParameter()) {
            ElementParameterType p = (ElementParameterType) e;
            if ("SPECIFY_SERVICE".equals(p.getName()) && "false".equals(p.getValue())) {
                needSave = true;
                break;
            }
        }
        if (needSave) {
            for (Object e : currentNode.getElementParameter()) {
                ElementParameterType p = (ElementParameterType) e;
                if (!"SERVICE_NAME".equals(p.getName()) && !"PORT_NAME".equals(p.getName())) {
                    continue;
                }
                p.setValue("\"\"");
            }
        }
        return needSave;
    }
}
