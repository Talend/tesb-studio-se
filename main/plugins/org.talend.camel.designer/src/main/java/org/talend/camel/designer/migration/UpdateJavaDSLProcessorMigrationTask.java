package org.talend.camel.designer.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.PersistenceException;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class UpdateJavaDSLProcessorMigrationTask extends AbstractRouteItemComponentMigrationTask {

    @Override
    public String getComponentNameRegex() {
        return "cJavaDSLProcessor";
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2021, 6, 7, 13, 0, 0);
        return gc.getTime();
    }

	@Override
	protected boolean execute(NodeType node) throws Exception {
		return disableHandleFaultExpression(node);
	}

	private boolean disableHandleFaultExpression(NodeType currentNode) throws PersistenceException {
        boolean needSave = false;

        ElementParameterType param = UtilTool.findParameterType(currentNode, "CODE");
        String originalValue = param.getValue();
        String modifiedValue = "";
        
        if (originalValue != null) {
        	modifiedValue = originalValue.replaceAll("^(?!\\/\\*)\\.handleFault\\(\\)(?!\\*/)*", "/\\*\\.handleFault\\(\\)\\*/");
        	if (originalValue.equals(modifiedValue)) {
        		return false;
        	}
        }
        	
        for (Object e : currentNode.getElementParameter()) {
            ElementParameterType p = (ElementParameterType) e;
            if ("CODE".equals(p.getName())) {
                p.setValue(modifiedValue);
                needSave = true;
                break;
            }
        }

        return needSave;
	}

}
