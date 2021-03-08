// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.codegen.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.camel.designer.codegen.i18n.Messages;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentFileNaming;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.codegen.config.TemplateUtil;

public class NodeUtil {

    public static String getTemplateURI(INode node, ECodePart part) {
        return getTemplateURI(node.getComponent(), part);
    }

    public static String getTemplateURI(IComponent component, ECodePart part) {
        String bundle = component.getPathSource();
        String path = component.getName();

        IComponentFileNaming fileNaming = ComponentsFactoryProvider.getFileNamingInstance();
        String file = fileNaming.getJetFileName(component, ProcessUtil.getCodeLanguageExtension(), part);

        return getTemplateURI(bundle, path, file);
    }

    /**
     * Gets the template uri.
     *
     * @param paths the paths starts from bundle id.
     * @return the template uri
     */
    public static String getTemplateURI(String... paths) {
        return StringUtils.join(paths, TemplateUtil.DIR_SEP);
    };

    /**
     * DOC xtan for debug
     * <p>
     * output the nodes info to console to check the intial input info from design level.
     * </p>
     */
    public static void printForDebug(List<? extends INode> nodes) {
        // get unique name
        List<String> nameList = new ArrayList<String>(nodes.size());
        for (INode node : nodes) {
            nameList.add(node.getUniqueName());
        }

        // sort in nameList, in order to keep the intial node inder in nodes.
        Collections.sort(nameList);

        for (String string : nameList) {
            for (INode node : nodes) {
                if (string.equals(node.getUniqueName())) {
                    // output the node info
                    System.out.println(node);
                    break;
                }
            }
        }

        System.out.println(Messages.getString("NodeUtil.newLine")); //$NON-NLS-1$
    }

    /**
     * Return Type of Node to correctly sort the encapsulated code.
     *
     * @param node the node to check
     * @return true if the node is an iterate node
     */
    public static boolean isSpecifyInputNode(INode node, String incomingName, EConnectionType connectionType) {
        // it means the first node without any income connection
        if (node == null || incomingName == null || connectionType == null) {
            return false;
        }
        List<? extends IConnection> inComingIterateConnection = node.getIncomingConnections(connectionType);
        if (inComingIterateConnection == null) {
            return false;
        }
        for (IConnection connection : inComingIterateConnection) {
            if (connection.getName().equals(incomingName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isConfigComponentNode(INode subProcessStartNode) {
        String startNodeName = subProcessStartNode.getComponent().getName();
        if ("cConfig".equals(startNodeName)) {
            // Customized remove the cConfig routeId codes.
            // TESB-2825 LiXP 20110823
            return true;
        }
        return false;
    }

    public static boolean isStartNode(INode node) {
        return node.getIncomingConnections().size() < 1 && node.isStart();
    }
}
