// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2017 Talend – www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.camel.designer.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class RenameToCSoapAndCRestMigrationTask extends AbstractRouteItemComponentMigrationTask {

    private static final String C_CXF = "cCXF";

    private static final String C_CXFRS = "cCXFRS";

    private static final String C_SOAP = "cSOAP";

    private static final String C_REST = "cREST";

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 2, 1, 0, 0, 0);
        return gc.getTime();
    }

    @Override
    public String getComponentNameRegex() {
        return "cCXF|cCXFRS";

    }

    @Override
    protected boolean execute(NodeType node) throws Exception {
        boolean needSave = false;
        if (node.getComponentName().equals(C_CXF)) {
            needSave = handleRenameCXFNode(node);
        } else if (node.getComponentName().equals(C_CXFRS)) {
            needSave = handleRenameCXFRSNode(node);
        }
        return needSave;
    }

    /**
     * Rename node cCXF to cSOAP
     *
     * @param node CXFNode node.
     *
     * @return true, if handle rename node
     */

    private boolean handleRenameCXFNode(NodeType node) {
        node.setComponentName(C_SOAP);
        ElementParameterType param = UtilTool.findParameterType(node, "UNIQUE_NAME");
        // have to retrieve the node to item
        String oldName = param.getValue();
        UtilTool.replaceValueSubSequence(param, C_CXF, C_SOAP);
        String newName = param.getValue();
        ProcessType item = (ProcessType) node.eContainer();
        renameConnections(item, oldName, newName);
        return true;
    }

    /**
     * Rename node cCXFRS to cREST
     *
     * @param node CXFRSNode node.
     *
     * @return true, if handle rename node
     */

    private boolean handleRenameCXFRSNode(NodeType node) {
        node.setComponentName(C_REST);
        ElementParameterType param = UtilTool.findParameterType(node, "UNIQUE_NAME");
        // have to retrieve the node to item
        String oldName = param.getValue();
        UtilTool.replaceValueSubSequence(param, C_CXFRS, C_REST);
        String newName = param.getValue();
        ProcessType item = (ProcessType) node.eContainer();
        renameConnections(item, oldName, newName);
        return true;
    }

    private void renameConnections(ProcessType item, String oldName, String newName) {
        for (Object o : item.getConnection()) {
            ConnectionType currentConnection = (ConnectionType) o;
            if (oldName.equals(currentConnection.getSource())) {
                currentConnection.setSource(newName);
            }
            if (oldName.equals(currentConnection.getTarget())) {
                currentConnection.setTarget(newName);
            }
            if (oldName.equals(currentConnection.getMetaname())) {
                currentConnection.setMetaname(newName);
            }
        }
    }
}
