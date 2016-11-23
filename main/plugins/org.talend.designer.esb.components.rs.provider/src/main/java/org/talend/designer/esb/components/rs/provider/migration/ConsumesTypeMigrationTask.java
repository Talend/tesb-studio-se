// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import java.util.List;

import org.talend.core.model.migration.AbstractJobItemComponentMigrationTask;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * Update consumes type (for POST case), in order to fix
 * [<a href="https://jira.talendforge.org/browse/TESB-11634">TESB-11634</a>]. Will update consumes type in case
 * POST/PUT, set default consumes type to "XML-JSON", instead of "NULL"(&lt;no-content&gt;).
 */
public class ConsumesTypeMigrationTask extends AbstractJobItemComponentMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2013, 8, 15, 00, 00, 00);
        return gc.getTime();
    }

    @Override
    protected String getComponentNameRegex() {
        return "tRESTRequest";
    }

    @Override
    protected boolean execute(NodeType node) {
        return updateConsumesType(node);
    }

    /**
     * Update consumes type in case POST/PUT, set default consumes type to "XML-JSON".
     * 
     * @param currentNode the current node
     * @return true, if successful
     */
    @SuppressWarnings("unchecked")
    private boolean updateConsumesType(NodeType currentNode) {
        List<?> elementParameter = currentNode.getElementParameter();
        ElementParameterType schemasParam = findElementByName(elementParameter, "SCHEMAS", ElementParameterType.class);
        if (schemasParam == null) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        List elementValues = schemasParam.getElementValue();
        ElementValueType value_HTTP_VERB = findElementByName(elementValues, "HTTP_VERB", ElementValueType.class);
        ElementValueType value_CONSUMES = findElementByName(elementValues, "CONSUMES", ElementValueType.class);

        if (value_HTTP_VERB == null || value_CONSUMES != null) {
            return false;
        }
        if (value_HTTP_VERB.getValue().equals("POST") || value_HTTP_VERB.getValue().equals("PUT")) {
            value_CONSUMES = TalendFileFactory.eINSTANCE.createElementValueType();
            value_CONSUMES.setElementRef("CONSUMES");
            value_CONSUMES.setValue("XML-JSON");
            elementValues.add(value_CONSUMES);
            return true;
        }
        return false;
    }

}
