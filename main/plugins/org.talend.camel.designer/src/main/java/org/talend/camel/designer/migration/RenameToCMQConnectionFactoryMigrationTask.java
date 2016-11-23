package org.talend.camel.designer.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class RenameToCMQConnectionFactoryMigrationTask extends AbstractRouteItemComponentMigrationTask {

    private static final String C_MQ_CONNECTION_FACTORY = "cMQConnectionFactory";

    private static final String C_JMS_CONNECTION_FACTORY = "cJMSConnectionFactory";

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 7, 2, 0, 0, 0);
        return gc.getTime();
    }

    @Override
    public String getComponentNameRegex() {
        return C_JMS_CONNECTION_FACTORY + "|cJMS|cAMQP";
    }

    @Override
    protected boolean execute(NodeType node) throws Exception {
        boolean needSave = false;
        if (node.getComponentName().equals(C_JMS_CONNECTION_FACTORY)) {
            needSave = handleRenameNode(node);
        } else {
            needSave = handlRenameConnection(node);
        }
        return needSave;
    }

    private boolean handlRenameConnection(NodeType node) {
        ElementParameterType param = UtilTool.findParameterType(node, "CONNECTION_FACOTRY_CONFIGURATION:ROUTE_COMPONENT_TYPE_ID");
        if (param == null) {
            return false;
        }
        boolean renameSuccess = UtilTool.replaceValueSubSequence(param, C_JMS_CONNECTION_FACTORY, C_MQ_CONNECTION_FACTORY);
        return renameSuccess;
    }

    /**
     * Rename node cJMSConnectionFactory to cMQConnectionFactory
     * 
     * @param node cJMSConnectionFactory node.
     *
     * @return true, if handle rename node
     */
    private boolean handleRenameNode(NodeType node) {
        node.setComponentName(C_MQ_CONNECTION_FACTORY);
        ElementParameterType param = UtilTool.findParameterType(node, "UNIQUE_NAME");
        UtilTool.replaceValueSubSequence(param, C_JMS_CONNECTION_FACTORY, C_MQ_CONNECTION_FACTORY);
        return true;
    }

}
