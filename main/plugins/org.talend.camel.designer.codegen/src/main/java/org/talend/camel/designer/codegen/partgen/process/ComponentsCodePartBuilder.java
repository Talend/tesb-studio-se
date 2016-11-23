package org.talend.camel.designer.codegen.partgen.process;

import java.util.Arrays;
import java.util.Vector;

import org.talend.camel.designer.codegen.config.ECamelTemplate;
import org.talend.camel.designer.codegen.partgen.PartGeneratorManager;
import org.talend.camel.designer.codegen.util.NodeUtil;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.INode;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.codegen.config.NodesSubTree;
import org.talend.designer.codegen.exception.CodeGeneratorException;

public class ComponentsCodePartBuilder extends AbstractProcessPartBuilder {

    private final NodesSubTree subProcess;

    private final INode node;

    private final String incomingName;

    private final ECodePart part;

    public ComponentsCodePartBuilder(PartGeneratorManager generatorManager, NodesSubTree subProcess, INode node, ECodePart part,
            String incomingName) {
        super(generatorManager);
        this.subProcess = subProcess;
        this.node = node;
        this.part = part;
        this.incomingName = incomingName;
    }

    @Override
    public AbstractProcessPartBuilder appendContent() throws CodeGeneratorException {
        append(generateComponentsCode(part));
        return this;
    }

    private CharSequence generateComponentsCode(ECodePart part) throws CodeGeneratorException {
        Boolean isMarked = subProcess.isMarkedNode(node, part);
        if (isMarked == null || isMarked) {
            return "";
        }

        StringBuilder codeComponent = new StringBuilder();

        switch (part) {
        case BEGIN:
            codeComponent.append(generatesTreeCode(ECodePart.BEGIN));
            codeComponent.append(generateComponentCode(ECodePart.BEGIN));
            break;
        case MAIN:
            boolean isIterate = NodeUtil.isSpecifyInputNode(node, incomingName, EConnectionType.ITERATE);
            if (isIterate) {
                codeComponent.append(generateTypedComponentCode(ECamelTemplate.ITERATE_SUBPROCESS_HEADER, ECodePart.BEGIN));
                codeComponent.append(generatesTreeCode(ECodePart.BEGIN));
                codeComponent.append(generateComponentCode(ECodePart.BEGIN));

                codeComponent.append(generateComponentCode(ECodePart.MAIN));
                codeComponent.append(generatesTreeCode(ECodePart.MAIN));

                codeComponent.append(generateComponentCode(ECodePart.END));
                codeComponent.append(generatesTreeCode(ECodePart.END));

                Vector<Object> iterate_Argument = wrapToVector(node, generateComponentsCode(ECodePart.FINALLY).toString());

                codeComponent.append(
                        generateTypedComponentCode(ECamelTemplate.ITERATE_SUBPROCESS_FOOTER, iterate_Argument, ECodePart.END));
            } else {
                if (node.getIncomingConnections() != null && node.getIncomingConnections().size() > 0) {
                    if (!(node.getIncomingConnections().get(0).getLineStyle().equals(EConnectionType.ROUTE))) {
                        codeComponent.append(generateTypedComponentCode(ECamelTemplate.CAMEL_SPECIALLINKS));
                    } else {
                        // TESB-16270
                        if ("cLoadBalancer".equals(node.getIncomingConnections().get(0).getSource().getComponent().getName())) {
                            codeComponent.append(".pipeline()");
                        }
                    }
                }
                codeComponent.append(generateComponentCode(ECodePart.MAIN));
                codeComponent.append(manager.generateEndpointId(node));

                // TESB-16270
                if (node.getIncomingConnections() != null && node.getIncomingConnections().size() > 0
                        && (node.getOutgoingConnections() == null || node.getOutgoingConnections().size() == 0)) {
                    INode sourceNode = node.getIncomingConnections().get(0).getSource();
                    INode currentNode = node;
                    boolean hasLoadBalance = false;
                    while (sourceNode.getIncomingConnections() != null && sourceNode.getIncomingConnections().size() > 0) {
                        if (currentNode.getIncomingConnections().get(0).getLineStyle().equals(EConnectionType.ROUTE)
                                && "cLoadBalancer".equals(
                                        currentNode.getIncomingConnections().get(0).getSource().getComponent().getName())) {
                            hasLoadBalance = true;
                            break;
                        }
                        currentNode = sourceNode;
                        sourceNode = sourceNode.getIncomingConnections().get(0).getSource();
                    }
                    if (hasLoadBalance) {
                        codeComponent.append(".end()");
                    }
                }

                codeComponent.append(generatesTreeCode(ECodePart.MAIN));
            }
            break;
        case END:
            boolean isOnRowsEnd = NodeUtil.isSpecifyInputNode(node, incomingName, EConnectionType.ON_ROWS_END);
            if (isOnRowsEnd) {

                codeComponent.append(generatesTreeCode(ECodePart.BEGIN));
                codeComponent.append(generateComponentCode(ECodePart.BEGIN));

                codeComponent.append(generateComponentCode(ECodePart.MAIN));
                codeComponent.append(generatesTreeCode(ECodePart.MAIN));

                codeComponent.append(generateComponentCode(ECodePart.END));
                codeComponent.append(generatesTreeCode(ECodePart.END));

            } else {
                codeComponent.append(generateComponentCode(ECodePart.END));
                codeComponent.append(generatesTreeCode(part));
            }
            break;
        case FINALLY:
            codeComponent.append(generateComponentCode(ECodePart.FINALLY));
            codeComponent.append(generatesTreeCode(ECodePart.FINALLY));
            break;
        default:
            // do nothing
        }
        subProcess.markNode(node, part);

        return codeComponent;
    }

    private CharSequence generatesTreeCode(ECodePart part) throws CodeGeneratorException {
        return manager.generateTreeCode(subProcess, node, part);
    }

    @Override
    public AbstractProcessPartBuilder appendTyped(ECamelTemplate template, Object... params) throws CodeGeneratorException {
        switch (template) {
        case CAMEL_SPECIALLINKS:
            return super.appendTyped(template, node);
        default:
            break;
        }
        return super.appendTyped(template, params);
    }

    private CharSequence generateTypedComponentCode(ECamelTemplate template, ECodePart part) throws CodeGeneratorException {
        return manager.genTemplatePart(template, node, part, incomingName, subProcess);
    }

    private CharSequence generateTypedComponentCode(ECamelTemplate template, Vector<Object> arg, ECodePart part)
            throws CodeGeneratorException {
        return manager.genTemplatePart(template, arg, part, incomingName, subProcess);
    }

    private CharSequence generateTypedComponentCode(ECamelTemplate template) throws CodeGeneratorException {
        if (template == ECamelTemplate.CAMEL_SPECIALLINKS) {
            return manager.genTemplatePart(template, node);
        }
        return manager.genTemplatePart(template);
    }

    private CharSequence generateComponentCode(ECodePart part) throws CodeGeneratorException {
        return manager.createNodePartBuilder().setSubTree(subProcess).setNode(node).setPart(part).setIncomingName(incomingName)
                .appendContent().build();
    }

    private static Vector<Object> wrapToVector(Object... objs) {
        return new Vector<>(Arrays.asList(objs));
    }
}
