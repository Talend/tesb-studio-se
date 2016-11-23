package org.talend.camel.designer.codegen.argument;

import org.talend.camel.model.IRouteProcess;
import org.talend.designer.codegen.config.NodesTree;

public abstract class ArgumentBuilderHolder {

    protected final CodeGeneratorArgumentBuilder argumentBuilder;

    protected final IRouteProcess process;

    protected final NodesTree processTree;

    public ArgumentBuilderHolder(CodeGeneratorArgumentBuilder argumentBuilder) {
        this.argumentBuilder = argumentBuilder;
        process = argumentBuilder.getProcess();
        processTree = argumentBuilder.getProcessTree();
    }

    public CodeGeneratorArgumentBuilder getArgumentBuilder() {
        return argumentBuilder;
    }
}
