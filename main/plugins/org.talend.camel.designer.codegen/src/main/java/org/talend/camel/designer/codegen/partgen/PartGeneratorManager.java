package org.talend.camel.designer.codegen.partgen;

import org.talend.camel.designer.codegen.argument.ArgumentBuilderHolder;
import org.talend.camel.designer.codegen.argument.CodeGeneratorArgumentBuilder;
import org.talend.camel.designer.codegen.config.ECamelTemplate;
import org.talend.camel.designer.codegen.partgen.process.AbstractProcessPartBuilder;
import org.talend.camel.designer.codegen.partgen.process.ComponentsCodePartBuilder;
import org.talend.camel.designer.codegen.partgen.process.NodePartBuilder;
import org.talend.camel.designer.codegen.partgen.process.ProcessPartBuilder;
import org.talend.camel.designer.codegen.partgen.process.TreeCodePartBuilder;
import org.talend.camel.model.IRouteProcess;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.INode;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.codegen.config.NodesSubTree;
import org.talend.designer.codegen.exception.CodeGeneratorException;

public class PartGeneratorManager extends ArgumentBuilderHolder {

    private PartGenerator<ECamelTemplate> templateGenerator;

    private PartGenerator<IContext> contextGenerator;

    private PartGenerator<INode> endpointIdGenerator;

    public PartGeneratorManager(IRouteProcess process, boolean statistics, boolean trace, String[] options) {
        super(new CodeGeneratorArgumentBuilder(process, statistics, trace, options));
        templateGenerator = new TemplatePartGenerator(argumentBuilder);
        contextGenerator = new ContextPartGenerator(argumentBuilder);
        endpointIdGenerator = new EndpointIdGenerator();
    }

    public PartGenerator<ECamelTemplate> getTemplateGenerator() {
        return templateGenerator;
    }

    public NodePartBuilder createNodePartBuilder() {
        NodePartBuilder builder = new NodePartBuilder(this);
        return builder;
    }

    public CharSequence genTemplatePart(ECamelTemplate template, Object... params) throws CodeGeneratorException {
        return templateGenerator.generatePart(template, params);
    }

    public String generateContextCode(IContext designerContext) throws CodeGeneratorException {
        return contextGenerator.generatePart(designerContext).toString();
    }

    public String generateProcessCode() throws CodeGeneratorException {
        AbstractProcessPartBuilder builder = new ProcessPartBuilder(this);
        builder.appendContent();
        return builder.build().toString();
    }

    public CharSequence generateComponentsCode(NodesSubTree subProcess, INode node, ECodePart part, String incomingName)
            throws CodeGeneratorException {
        return new ComponentsCodePartBuilder(this, subProcess, node, part, incomingName).appendContent().build();
    }

    public CharSequence generateTreeCode(NodesSubTree subProcess, INode node, ECodePart part) throws CodeGeneratorException {
        if (node == null) {
            return "";
        }
        return new TreeCodePartBuilder(this, subProcess, node, part).appendContent().build();
    }

    public CharSequence generateEndpointId(INode node) throws CodeGeneratorException {
        return endpointIdGenerator.generatePart(node);
    }
}
