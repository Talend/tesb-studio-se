// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * @author LiXiaopeng
 * Update: Removed common functions to handle NodeType to {@link AbstractRouteItemComponentMigrationTask.UtilTool} - by GaoZone.

 */
public class UpdateCJMSProjectMigrationTask extends AbstractRouteItemMigrationTask {

    private static final ProxyRepositoryFactory REPO_FACTORY = ProxyRepositoryFactory.getInstance();

    private static final TalendFileFactory FILE_FACTORY = TalendFileFactory.eINSTANCE;


    /**
     * 
     * @param currentNode
     * @return
     */
    private NodeType createConnectionFactoryNode(NodeType currentNode) {
        NodeType nodeType = FILE_FACTORY.createNodeType();
        nodeType.setSizeX(32);
        nodeType.setSizeY(32);
        nodeType.setComponentName("cMQConnectionFactory");

        String label = UtilTool.getParameterValue(currentNode, "LABEL");
        if (label == null || label.equals("_UNIQUE_NAME_")) {
            label = UtilTool.getParameterValue(currentNode, "UNIQUE_NAME");
        }
        label += "_ConnectionFacotry";

        UtilTool.addParameterType(nodeType, "TEXT", "LABEL", label, null);
        UtilTool.addParameterType(nodeType, "TEXT", "UNIQUE_NAME", label, null);

        // Don't forget set the Connection for cJMS
        UtilTool.addParameterType(currentNode, "TEXT", "CONNECTION_FACOTRY_LABEL", label, null);
        UtilTool.addParameterType(currentNode, "TEXT", "CONNECTION_FACOTRY", label, null);

        for (Object e : currentNode.getElementParameter()) {
            ElementParameterType p = (ElementParameterType) e;
            if (!("UNIQUE_NAME".equals(p.getName()) || "LABEL".equals(p.getName()) || "TYPE".equals(p.getName()) || "DESTINATION"
                    .equals(p.getName()))) {

                UtilTool.addParameterType(nodeType, p.getField(), p.getName(), p.getValue(), p.getElementValue());
            }
        }

        return nodeType;
    }

    @Override
    public ExecutionResult execute(CamelProcessItem item) {
        try {
            updateJMSComponent(item);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    /**
     * Get maximum Y position of the process.
     * 
     * @param processType
     * @return
     */
    private int getMaxY(ProcessType processType) {
        int max = 0;
        for (Object o : processType.getNode()) {
            if (o instanceof NodeType) {
                NodeType currentNode = (NodeType) o;
                if (currentNode.getPosY() > max) {
                    max = currentNode.getPosY();
                }
            }
        }
        return max;
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 2, 16, 10, 00, 00);
        return gc.getTime();
    }

    public ProcessType getProcessType(Item item) {
        if (item instanceof ProcessItem) {
            return ((ProcessItem) item).getProcess();
        }
        return null;
    }

    /**
     * Compute the location of cMQConnectionFactory Node
     * 
     * @param processType
     * @param nodes
     */
    private void locateNodes(ProcessType processType, List<NodeType> nodes) {

        int maxY = getMaxY(processType);
        int index = 1;
        for (NodeType node : nodes) {
            node.setPosX(100 * index);
            node.setPosY(maxY + 100);
            index++;
        }
    }

    /**
     * Update cJMS, add cMQConnectionFactory.
     * 
     * @param item
     * @throws PersistenceException
     */
    @SuppressWarnings("unchecked")
    private void updateJMSComponent(Item item) throws PersistenceException {

        ProcessType processType = getProcessType(item);
        if (processType == null) {
            return;
        }
        boolean modified = false;

        List<NodeType> nodes = new ArrayList<NodeType>();

        for (Object o : processType.getNode()) {
            if (o instanceof NodeType) {
                NodeType currentNode = (NodeType) o;
                if ("cJMS".equals(currentNode.getComponentName())) {
                    modified = true;
                    NodeType cfNode = createConnectionFactoryNode(currentNode);
                    nodes.add(cfNode);
                }
            }
        }

        if (modified) {
            locateNodes(processType, nodes);

            processType.getNode().addAll(nodes);
            REPO_FACTORY.save(item, true);
        }
    }

}
