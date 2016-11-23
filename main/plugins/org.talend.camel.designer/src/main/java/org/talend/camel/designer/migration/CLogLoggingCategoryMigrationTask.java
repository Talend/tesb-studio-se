package org.talend.camel.designer.migration;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

public class CLogLoggingCategoryMigrationTask extends AbstractRouteItemComponentMigrationTask {

    private static final String LOGGING_CATEGORY = "LOGGING_CATEGORY";

    @Override
    public String getComponentNameRegex() {
        return "cLog";
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 11, 6, 00, 00, 00);
        return gc.getTime();
    }

    @Override
    protected boolean execute(NodeType node) throws Exception {
        @SuppressWarnings("unchecked")
        List<ElementParameterType> parameters = node.getElementParameter();
        boolean hasLoggingCategory = false;
        String label = null;
        String cid = null;
        for (ElementParameterType param : parameters) {
            if (LOGGING_CATEGORY.equals(param.getName())) {
                hasLoggingCategory = true;
            } else if ("LABEL".equals(param.getName())) {
                label = param.getValue();
            } else if ("UNIQUE_NAME".equals(param.getName())) {
                cid = param.getValue();
            }
        }
        if (hasLoggingCategory) {
            return false;
        }
        if (label != null && !"__UNIQUE_NAME__".equals(label)) {
            cid = label + "_" + cid;
        }

        ElementParameterType newParameter = TalendFileFactory.eINSTANCE.createElementParameterType();
        newParameter.setName(LOGGING_CATEGORY);
        newParameter.setValue("\"" + cid + "\"");
        parameters.add(newParameter);
        return true;
    }

}