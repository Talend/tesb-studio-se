package org.talend.camel.designer.codegen.partgen.process;

import org.talend.camel.designer.codegen.config.ECamelTemplate;
import org.talend.camel.designer.codegen.partgen.PartGeneratorManager;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.codegen.config.NodesSubTree;
import org.talend.designer.codegen.exception.CodeGeneratorException;

public abstract class AbstractProcessPartBuilder {

    protected final PartGeneratorManager manager;

    private final StringBuilder sb = new StringBuilder();

    public AbstractProcessPartBuilder(PartGeneratorManager generatorManager) {
        this.manager = generatorManager;
    }

    public AbstractProcessPartBuilder appendTyped(ECamelTemplate template, Object... params) throws CodeGeneratorException {
        switch (template) {
        case PROCESSINFO:
            return append(manager.genTemplatePart(template, sb.length()));
        default:
            return append(manager.genTemplatePart(template, params));
        }
    }

    public AbstractProcessPartBuilder appendSubTree(NodesSubTree subTree) throws CodeGeneratorException {
        append(manager.generateComponentsCode(subTree, subTree.getRootNode(), ECodePart.MAIN, null));
        append(';');
        return this;
    }

    public CharSequence build() {
        return sb;
    }

    public abstract AbstractProcessPartBuilder appendContent() throws CodeGeneratorException;

    protected AbstractProcessPartBuilder append(CharSequence charSequence) {
        if (charSequence != null) {
            sb.append(charSequence);
        }
        return this;
    }

    protected AbstractProcessPartBuilder append(char c) {
        sb.append(c);
        return this;
    }

}
