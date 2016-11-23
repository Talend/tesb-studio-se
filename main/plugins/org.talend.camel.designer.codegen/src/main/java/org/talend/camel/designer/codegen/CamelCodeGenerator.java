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
package org.talend.camel.designer.codegen;

import org.talend.camel.designer.codegen.config.ECamelTemplate;
import org.talend.camel.designer.codegen.i18n.Messages;
import org.talend.camel.designer.codegen.jet.JetUtil;
import org.talend.camel.designer.codegen.partgen.PartGeneratorManager;
import org.talend.camel.designer.codegen.util.NodeUtil;
import org.talend.camel.model.IRouteProcess;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.INode;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.codegen.IAloneProcessNodeConfigurer;
import org.talend.designer.codegen.ICodeGenerator;
import org.talend.designer.codegen.exception.CodeGeneratorException;

public class CamelCodeGenerator implements ICodeGenerator {

    private static final boolean DEBUG = false;

    private final PartGeneratorManager partGeneratorManager;

    /**
     * Constructor : use the process and language to initialize internal components.
     */
    public CamelCodeGenerator(IRouteProcess process, boolean statistics, boolean trace, String... options) {
        if (DEBUG) {
            // TODO Missing messages
            System.out.println(Messages.getString("CamelCodeGenerator.getGraphicalNode1")); //$NON-NLS-1$
            NodeUtil.printForDebug(process.getGraphicalNodes());
            System.out.println(Messages.getString("CamelCodeGenerator.getGraphicalNode2")); //$NON-NLS-1$
            NodeUtil.printForDebug(process.getGeneratingNodes());
        }
        partGeneratorManager = new PartGeneratorManager(process, statistics, trace, options);
    }

    /**
     * Generate the code for the process given to the constructor.
     * 
     * @return the generated code
     * @throws CodeGeneratorException if an error occurs during Code Generation
     */
    @Override
    public String generateProcessCode() throws CodeGeneratorException {
        JetUtil.checkEmittersPoolFactoryIsReady();
        return partGeneratorManager.generateProcessCode();
    }

    /**
     * Generate Part Code for a given Component.
     * 
     * @param node the component
     * @param part the component's part
     * @return the generated code
     * @throws CodeGeneratorException if an error occurs during Code Generation
     */
    @Override
    public String generateComponentCode(INode node, ECodePart part) throws CodeGeneratorException {
        return partGeneratorManager.createNodePartBuilder().setNode(node).setPart(part).append(ECamelTemplate.PART_HEADER)
                .appendContent().append(ECamelTemplate.PART_FOOTER).build().toString();
    }

    /**
     * Parse Process, and generate Code for Context Variables.
     * 
     * @param designerContext the context to generate code from
     * @return the generated code
     * @throws CodeGeneratorException if an error occurs during Code Generation
     */
    @Override
    public String generateContextCode(IContext designerContext) throws CodeGeneratorException {
        return partGeneratorManager.generateContextCode(designerContext);
    }

    @Override
    public String generateComponentCodeWithRows(String nodeName, IAloneProcessNodeConfigurer nodeConfigurer) {
        // TODO do more check if it invoked for route. Need do more checks.
        throw new IllegalAccessError("method not support for route job. ");
    }

    @Override
    public void setContextName(String contextName) {
        partGeneratorManager.getArgumentBuilder().setContextName(contextName);
    }
}
