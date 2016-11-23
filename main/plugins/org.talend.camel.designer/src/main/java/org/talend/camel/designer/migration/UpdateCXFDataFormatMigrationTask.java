// ============================================================================
package org.talend.camel.designer.migration;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.PersistenceException;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class UpdateCXFDataFormatMigrationTask extends AbstractRouteItemComponentMigrationTask {

    @Override
    public String getComponentNameRegex() {
        return "cCXF";
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 10, 18, 14, 00, 00);
        return gc.getTime();
    }

    @Override
    protected boolean execute(NodeType node) throws Exception {
        return changeMESSAGE2RAW(node);
    }

    private boolean changeMESSAGE2RAW(NodeType currentNode) throws PersistenceException, IOException {
        boolean needSave = false;
        for (Object e : currentNode.getElementParameter()) {
            ElementParameterType p = (ElementParameterType) e;
            if (!"DATAFORMAT".equals(p.getName())) {
                continue;
            }
            String value = p.getValue();
            if ("MESSAGE".equals(value)) {
                p.setValue("RAW");
                needSave = true;
            }
        }
        return needSave;
    }

}
