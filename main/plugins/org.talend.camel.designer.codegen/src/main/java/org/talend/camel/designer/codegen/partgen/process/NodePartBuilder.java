package org.talend.camel.designer.codegen.partgen.process;

import org.talend.camel.designer.codegen.config.ECamelTemplate;
import org.talend.camel.designer.codegen.jet.JetUtil;
import org.talend.camel.designer.codegen.partgen.PartGeneratorManager;
import org.talend.core.model.process.INode;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.codegen.config.CodeGeneratorArgument;
import org.talend.designer.codegen.config.NodesSubTree;
import org.talend.designer.codegen.exception.CodeGeneratorException;

public class NodePartBuilder extends AbstractProcessPartBuilder {

    public NodePartBuilder(PartGeneratorManager generatorManager) {
        super(generatorManager);
    }

    private INode node;

    private ECodePart part;

    private NodesSubTree subTree;

    private String incomingName;

    @Override
    public NodePartBuilder appendContent() throws CodeGeneratorException {
        CodeGeneratorArgument argument = manager.getArgumentBuilder().build();
        argument.setNode(node);
        if (subTree != null) {
            argument.setAllMainSubTreeConnections(subTree.getAllMainSubTreeConnections());
            argument.setSubTree(subTree);
        }
        argument.setCodePart(part);
        argument.setIncomingName(incomingName);
        append(JetUtil.jetGenerate(argument));
        return this;
    }

    public NodePartBuilder append(ECamelTemplate template) throws CodeGeneratorException {
        switch (template) {
        case PART_HEADER:
        case PART_FOOTER:
            appendTyped(template, node, part);
            break;
        default:
            super.appendTyped(template);
            break;
        }
        return this;
    }

    public NodePartBuilder setNode(INode node) {
        this.node = node;
        return this;
    }

    public NodePartBuilder setPart(ECodePart part) {
        this.part = part;
        return this;
    }

    public NodePartBuilder setSubTree(NodesSubTree subTree) {
        this.subTree = subTree;
        return this;
    }

    public NodePartBuilder setIncomingName(String incomingName) {
        this.incomingName = incomingName;
        return this;
    }
}
