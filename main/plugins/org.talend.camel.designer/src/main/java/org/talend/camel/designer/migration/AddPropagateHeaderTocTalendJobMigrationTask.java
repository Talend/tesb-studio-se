package org.talend.camel.designer.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

public class AddPropagateHeaderTocTalendJobMigrationTask extends AbstractRouteItemComponentMigrationTask {

    @Override
    public String getComponentNameRegex() {
        return "cTalendJob";
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 9, 18, 14, 00, 00);
        return gc.getTime();
    }

    @Override
    public boolean execute(NodeType node) throws PersistenceException {
        return addPropagateHeader(node);
    }

    /**
     * Adds the propagate header.
     *
     * @param currentNode the current node
     * @return true, if need save.
     */
    private boolean addPropagateHeader(NodeType currentNode) {
        EList elementParameter = currentNode.getElementParameter();
        boolean isNewElement = false;
        for (Object obj : elementParameter) {
            if (!(obj instanceof ElementParameterType)) {
                continue;
            }
            ElementParameterType param = (ElementParameterType) obj;
            String name = param.getName();
            if ("PROPAGATE_HEADER".equals(name)) {
                isNewElement = true;
                break;
            }
        }
        if (isNewElement) {
            return false;
        }

        ElementParameterType propageteHeader = TalendFileFactory.eINSTANCE.createElementParameterType();
        propageteHeader.setName("PROPAGATE_HEADER");
        propageteHeader.setField("CHECK");
        propageteHeader.setValue("true");
        elementParameter.add(propageteHeader);
        return true;
    }

}
